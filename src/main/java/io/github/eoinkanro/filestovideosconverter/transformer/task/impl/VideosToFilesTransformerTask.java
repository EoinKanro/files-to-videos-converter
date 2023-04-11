package io.github.eoinkanro.filestovideosconverter.transformer.task.impl;

import io.github.eoinkanro.filestovideosconverter.transformer.TransformException;
import io.github.eoinkanro.filestovideosconverter.transformer.model.VideosToFilesModel;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTask;
import lombok.extern.log4j.Log4j2;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.*;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.VIDEOS_PATH;

@Log4j2
public class VideosToFilesTransformerTask extends TransformerTask {

    public VideosToFilesTransformerTask(File processData) {
        super(processData);
    }

    @Override
    protected void process() {
        log.info("Processing {}...", processData);
        VideosToFilesModel context = new VideosToFilesModel();
        context.setCurrentOriginalFile(fileUtils.getOriginalNameOfFile(processData, inputCLIArgumentsHolder.getArgument(VIDEOS_PATH)));

        File resultFile;
        try {
            resultFile = fileUtils.getVideosToFilesResultFile(context.getCurrentOriginalFile());
            context.setCurrentResultFile(resultFile.getAbsolutePath());
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(resultFile))) {
            context.setDuplicateFactor(fileUtils.getImageDuplicateFactor(processData.getAbsolutePath()));
            processFile(context, processData, outputStream);

            int lastZeroBytesCount = fileUtils.getImageLastZeroBytesCount(processData.getAbsolutePath());
            for (int i = 0; i < lastZeroBytesCount; i++) {
                outputStream.write(0);
            }
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }

    /**
     * Write bits from pixels of images from video to file
     *
     * @param context      - context of file
     * @param video        - video
     * @param outputStream - result file
     * @throws IOException - if something goes wrong with writing file
     */
    private void processFile(VideosToFilesModel context, File video, OutputStream outputStream) throws IOException {
        int frameNumber = 0;
        try(FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video);
            Java2DFrameConverter converter = new Java2DFrameConverter()) {
            grabber.start();

            Frame frame = null;
            while ((frame = grabber.grabFrame()) != null) {
                BufferedImage image = converter.convert(frame);

                context.setImageWidth(image.getWidth());
                context.setImageHeight(image.getHeight());
                context.setPixels(image.getRGB(0, 0, context.getImageWidth(), context.getImageHeight(),
                        null, 0, context.getImageWidth()));

                processImage(context, outputStream);

                //TODO statistics
                frameNumber++;
            }
        } finally {
            log.info("Frames: {}", frameNumber);
        }
    }

    /**
     * Process one image from video
     *
     * @param context      - context of file
     * @param outputStream - result file
     * @throws IOException - if bytes can't be written to result file
     */
    private void processImage(VideosToFilesModel context, OutputStream outputStream) throws IOException {
        int pixelsIterations = context.getPixels().length / context.getImageWidth() / context.getDuplicateFactor() ;
        clearContextTempVariables(context);

        for (int i = 0; i < pixelsIterations; i++) {
            int[][] copiedRows = copyPixelRowsFromImage(context);

            int[] bitsRow = transformToBitRow(copiedRows, context.getDuplicateFactor());
            for (int bit : bitsRow) {
                if (bit >= 0) {
                    context.getByteBuilder().append(bit);
                }

                if (context.getByteBuilder().length() >= 8) {
                    int aByte = Integer.parseInt(context.getByteBuilder().toString(), 2);
                    if (aByte == 0) {
                        context.incrementZeroBytesCount();
                        context.setByteBuilder(new StringBuilder());
                        continue;
                    }

                    writeZeroBytes(context.getZeroBytesCount(), outputStream);
                    context.setZeroBytesCount(0);

                    context.setByteBuilder(new StringBuilder());
                    outputStream.write(aByte);
                }
            }
        }
    }

    /**
     * Clear temp variables of context
     *
     * @param context - context of file
     */
    private void clearContextTempVariables(VideosToFilesModel context) {
        context.setByteBuilder(new StringBuilder());
        context.setPixelsLastIndex(0);
    }

    /**
     * Copy several rows of image pixels using duplicate factor
     *
     * @param context - context of file
     * @return - several image rows
     */
    private int[][] copyPixelRowsFromImage(VideosToFilesModel context) {
        int[][] result = new int[context.getDuplicateFactor()][];

        for (int i = 0; i < result.length; i++) {
            result[i] = copyPixelRowFromImage(context.getPixels(), context.getPixelsLastIndex(), context.getImageWidth());
            context.setPixelsLastIndex(context.getPixelsLastIndex() + context.getImageWidth());
        }
        return result;
    }

    /**
     * Copy one row of image
     *
     * @param width - width of image
     * @return - one image row
     */
    private int[] copyPixelRowFromImage(int[] pixels, int pixelsLastIndex, int width) {
        int[] result = new int[width];
        int copyIndex = 0;

        for (int i = pixelsLastIndex; i < pixelsLastIndex + width; i++) {
            result[copyIndex] = pixels[i];
            copyIndex++;
        }
        return result;
    }

    /**
     * Transform several rows of image pixels to one row of bits
     * using duplicate factor
     *
     * @param copiedRows      - several rows of image
     * @param duplicateFactor - duplicate factor of image
     * @return - row of bits
     */
    private int[] transformToBitRow(int[][] copiedRows, int duplicateFactor) {
        int[] result = new int[copiedRows[0].length / duplicateFactor];

        int pixelSum = 0;
        int duplicateFactorIterations = 0;
        int resultIndex = 0;

        for (int i = 0; i < copiedRows[0].length; i++) {
            if (duplicateFactorIterations >= duplicateFactor) {
                result[resultIndex] = bytesUtils.pixelToBit(pixelSum, duplicateFactor);
                resultIndex++;
                duplicateFactorIterations = 0;
                pixelSum = 0;
            }

            for (int[] row : copiedRows) {
                pixelSum += row[i];
            }
            duplicateFactorIterations++;
        }

        if (duplicateFactorIterations >= duplicateFactor) {
            result[resultIndex] = bytesUtils.pixelToBit(pixelSum, duplicateFactor);
        }
        return result;
    }

    /**
     * Write count of zero bytes to file
     *
     * @param zeroBytesCount - count of zero bytes
     * @param outputStream - result file
     * @throws IOException - if bytes can't be written
     */
    private void writeZeroBytes(long zeroBytesCount, OutputStream outputStream) throws IOException {
        for (long i = 0; i < zeroBytesCount; i++) {
            outputStream.write(0);
        }
    }
}
