package io.github.eoinkanro.filestovideosconverter.transformer.task.impl;

import io.github.eoinkanro.filestovideosconverter.transformer.TransformException;
import io.github.eoinkanro.filestovideosconverter.transformer.model.FilesToVideosModel;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTask;
import lombok.extern.log4j.Log4j2;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.*;
import static io.github.eoinkanro.filestovideosconverter.utils.BytesUtils.ZERO;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_RGB32_1;

@Log4j2
public class FilesToVideosTransformerTask extends TransformerTask {
    int frames = 0;

    public FilesToVideosTransformerTask(File processData) {
        super(processData);
    }

    @Override
    protected void process() {
        processFile(processData);
    }

    /**
     * Process one file
     *
     * @param file - file
     */
    private void processFile(File file) {
        log.info("Processing {}...", file);
        FilesToVideosModel context = prepareContext(file);

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
             FFmpegFrameRecorder videoRecorder = new FFmpegFrameRecorder(fileUtils.getFilesToImagesResultFile(file, context.getLastZeroBytesCount()),
                     inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH),
                     inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT));
             Java2DFrameConverter imageConverter = new Java2DFrameConverter()) {

            prepareVideoRecorder(videoRecorder);
            videoRecorder.start();

            int aByte;
            while ((aByte = inputStream.read()) >= 0) {
                String bits = bytesUtils.byteToBits(aByte);

                for (int i = 0; i < bits.length(); i++) {
                    if (context.getTempRowIndex() >= context.getTempRow().length) {
                        writeDuplicateTempRow(context);
                        initTempRow(context);
                    }

                    if (context.getPixelIndex() >= context.getPixels().length) {
                        writeFrame(context, videoRecorder, imageConverter);
                        initPixels(context);
                    }

                    int pixel = bytesUtils.bitToPixel(Integer.parseInt(String.valueOf(bits.charAt(i))));
                    context.getTempRow()[context.getTempRowIndex()] = pixel;
                    context.incrementTempRowIndex();
                }
            }

            processLastPixels(context, videoRecorder, imageConverter);
        } catch (Exception e) {
            //TODO
            throw new TransformException("COMMON_EXCEPTION_DESCRIPTION", e);
        } finally {
            //TODO statistics
            log.info("Frames: {}", frames );
        }
    }

    /**
     * Prepare main information and objects for converting
     *
     * @param file - file to convert
     * @return - context with main information
     */
    private FilesToVideosModel prepareContext(File file) {
        FilesToVideosModel context = new FilesToVideosModel();
        context.setLastZeroBytesCount(fileUtils.calculateLastZeroBytesAmount(file));
        initPixels(context);
        initTempRow(context);
        return context;
    }

    /**
     * Init necessary parameters for new image
     *
     * @param context - context of file
     */
    private void initPixels(FilesToVideosModel context) {
        context.setPixels(new int[inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) * inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT)]);
        context.setPixelIndex(0);
    }

    /**
     * Init temp row for pixels that contains pixels without duplicate factor
     *
     * @param context - context of file
     */
    private void initTempRow(FilesToVideosModel context) {
        context.setTempRow(new int[inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) / inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR)]);
        context.setTempRowIndex(0);
    }

    private void prepareVideoRecorder(FFmpegFrameRecorder videoRecorder) {
        videoRecorder.setFrameRate(inputCLIArgumentsHolder.getArgument(FRAMERATE));
        videoRecorder.setVideoCodecName("libx264");
        videoRecorder.setFormat("mp4");
        videoRecorder.setOption("crf", "18");
        videoRecorder.setOption("movflags", "+faststart");
        videoRecorder.setOption("preset", "slow");
    }

    /**
     * Write several temp rows to result image using duplicate factor
     *
     * @param context - context of file
     */
    private void writeDuplicateTempRow(FilesToVideosModel context) {
        for (int i = 0; i < inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR); i++) {
            writeTempRow(context);
        }
    }

    /**
     * Write one temp row with pixels to result image using duplicate factor
     *
     * @param context - context of file
     */
    private void writeTempRow(FilesToVideosModel context) {
        for (int pixel : context.getTempRow()) {
            int[] pixels = context.getPixels();

            for (int i = 0; i < inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR); i++) {
                pixels[context.getPixelIndex()] = pixel;
                context.incrementPixelIndex();
            }
        }
    }

    /**
     * Write frame to video
     *
     * @param context - context of file
     */
    private void writeFrame(FilesToVideosModel context, FFmpegFrameRecorder videoRecorder, Java2DFrameConverter imageConverter) throws FFmpegFrameRecorder.Exception {
        //TODO statistics

        BufferedImage bufferedImage = new BufferedImage(inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH), inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT), BufferedImage.TYPE_INT_RGB);
        bufferedImage.setRGB(0, 0, inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH), inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT),
                context.getPixels(), 0, inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH));

        videoRecorder.record(imageConverter.convert(bufferedImage), AV_PIX_FMT_RGB32_1);

        frames++;
    }

    /**
     * Set last pixels of image to {@link io.github.eoinkanro.filestovideosconverter.utils.BytesUtils#ZERO}
     * And save image
     *
     * @param context - context of file
     * @throws IOException - if something wrong on save image
     */
    private void processLastPixels(FilesToVideosModel context, FFmpegFrameRecorder videoRecorder, Java2DFrameConverter imageConverter) throws IOException {
        int[] tempRow = context.getTempRow();
        if (context.getTempRowIndex() < tempRow.length) {
            for (int i = context.getTempRowIndex(); i < tempRow.length; i++) {
                tempRow[i] = ZERO;
            }
            writeDuplicateTempRow(context);
        }

        int[] pixels = context.getPixels();
        if (context.getPixelIndex() < pixels.length) {
            for (int i = context.getPixelIndex(); i < pixels.length; i++) {
                pixels[i] = ZERO;
            }
            writeFrame(context, videoRecorder, imageConverter);
        }
    }
}
