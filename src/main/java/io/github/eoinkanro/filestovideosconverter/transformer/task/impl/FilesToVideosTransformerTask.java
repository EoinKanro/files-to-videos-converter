package io.github.eoinkanro.filestovideosconverter.transformer.task.impl;

import io.github.eoinkanro.filestovideosconverter.transformer.TransformException;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTask;
import lombok.extern.log4j.Log4j2;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.*;
import static io.github.eoinkanro.filestovideosconverter.utils.BytesUtils.ZERO;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_RGB32_1;

@Log4j2
public class FilesToVideosTransformerTask extends TransformerTask {

    private int lastZeroBytesCount;

    private int[] pixels;
    private int pixelIndex;

    private int[] tempRow;
    private int tempRowIndex;

    public FilesToVideosTransformerTask(File processData) {
        super(processData);
    }

    @Override
    protected void process() {
        log.info("Processing {}...", processData);
        init(processData);

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(processData));
             FFmpegFrameRecorder videoRecorder = new FFmpegFrameRecorder(fileUtils.getFilesToVideosResultFile(processData, lastZeroBytesCount),
                                                                         inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH),
                                                                         inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT))) {

            initVideoRecorder(videoRecorder);
            int aByte;

            while ((aByte = inputStream.read()) >= 0) {
                String bits = bytesUtils.byteToBits(aByte);

                for (int i = 0; i < bits.length(); i++) {
                    if (tempRowIndex >= tempRow.length) {
                        writeTempRowIntoImage();
                        initTempRow();
                    }

                    if (pixelIndex >= pixels.length) {
                        writeImageIntoVideo(videoRecorder);
                        initPixels();
                    }

                    int pixel = bytesUtils.bitToPixel(Character.getNumericValue(bits.charAt(i)));
                    tempRow[tempRowIndex] = pixel;
                    tempRowIndex++;
                }
            }

            processLastPixels(videoRecorder);
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }

        taskStatistics.logResult();
        log.info("File {} was processed successfully", processData);
    }

    /**
     * Prepare main information and objects
     *
     * @param file - file to convert
     */
    private void init(File file) {
        lastZeroBytesCount = fileUtils.calculateLastZeroBytesAmount(file);
        initPixels();
        initTempRow();

        taskStatistics.setFilePath(file.getAbsolutePath());
    }

    /**
     * Init new array with pixels of result image and index of processed pixel
     */
    private void initPixels() {
        pixels = new int[inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) * inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT)];
        pixelIndex = 0;
    }

    /**
     * Init temp row for pixels that contains pixels without duplicate factor
     */
    private void initTempRow() {
        tempRow = new int[inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) / inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR)];
        tempRowIndex = 0;
    }

    /**
     * Init ffmpeg recorder that creates result video
     *
     * @param videoRecorder - ffmpeg recorder
     * @throws FFmpegFrameRecorder.Exception - if recorder can't be started
     */
    private void initVideoRecorder(FFmpegFrameRecorder videoRecorder) throws FFmpegFrameRecorder.Exception {
        videoRecorder.setFormat("mp4");
        videoRecorder.setFrameRate(inputCLIArgumentsHolder.getArgument(FRAMERATE));
        videoRecorder.setVideoCodecName("libx264");
        videoRecorder.setVideoOption("pix_fmt", "gray");
        videoRecorder.setOption("crf", "18");
        videoRecorder.setOption("preset", "slow");
        videoRecorder.setOption("movflags", "+faststart");

        videoRecorder.start();
    }

    /**
     * Write temp row into several rows of result image using duplicate factor
     */
    private void writeTempRowIntoImage() {
        for (int i = 0; i < inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR); i++) {
            writeTempRowIntoOneRowOfImage();
        }
    }

    /**
     * Write temp row into one row of result image using duplicate factor
     */
    private void writeTempRowIntoOneRowOfImage() {
        for (int pixel : tempRow) {
            for (int i = 0; i < inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR); i++) {
                pixels[pixelIndex] = pixel;
                pixelIndex++;
            }
        }
    }

    /**
     * Write frame to video
     */
    private void writeImageIntoVideo(FFmpegFrameRecorder videoRecorder) throws FFmpegFrameRecorder.Exception {
        ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);
        IntBuffer intBuffer = buffer.asIntBuffer();
        intBuffer.put(pixels);

        Frame frame = new Frame(inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH), inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT), Frame.DEPTH_UBYTE, 4);
        frame.image[0].position(0);
        ((ByteBuffer) frame.image[0]).put(buffer);

        videoRecorder.record(frame, AV_PIX_FMT_RGB32_1);

        taskStatistics.poll();
    }

    /**
     * Set last pixels of image to {@link io.github.eoinkanro.filestovideosconverter.utils.BytesUtils#ZERO}
     * And save image
     *
     * @throws FFmpegFrameRecorder.Exception - if image can't be written into video
     */
    private void processLastPixels(FFmpegFrameRecorder videoRecorder) throws FFmpegFrameRecorder.Exception {
        if (tempRowIndex < tempRow.length) {
            for (int i = tempRowIndex; i < tempRow.length; i++) {
                tempRow[i] = ZERO;
            }
            writeTempRowIntoImage();
        }

        if (pixelIndex < pixels.length) {
            for (int i = pixelIndex; i < pixels.length; i++) {
                pixels[i] = ZERO;
            }
            writeImageIntoVideo(videoRecorder);
        }
    }
}
