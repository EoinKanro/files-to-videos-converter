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
        processFiles(processData);
    }

    /**
     * Process images of one file
     *
     * @param video - images of one file
     */
    private void processFiles(File video) {
        VideosToFilesModel context = new VideosToFilesModel();
        context.setCurrentOriginalFile(fileUtils.getOriginalNameOfFile(video, inputCLIArgumentsHolder.getArgument(VIDEOS_PATH)));

        File resultFile;
        try {
            resultFile = fileUtils.getVideosToFilesResultFile(context.getCurrentOriginalFile());
            context.setCurrentResultFile(resultFile.getAbsolutePath());
        } catch (Exception e) {
            throw new TransformException("asd", e);
        }

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(resultFile))) {
            processFile(context, video, outputStream);

            int lastZeroBytesCount = fileUtils.getImageLastZeroBytesCount(video.getAbsolutePath());
            for (int i = 0; i < lastZeroBytesCount; i++) {
                outputStream.write(0);
            }
        } catch (Exception e) {
            throw new TransformException("asd", e);
        }
    }

    /**
     * Write bits from pixels of image to file
     *
     * @param context      - context of file
     * @param video         - image
     * @param outputStream - result file
     * @throws IOException - if something goes wrong with writing file
     */
    private void processFile(VideosToFilesModel context, File video, OutputStream outputStream) throws IOException {
        log.info("Processing {} to {}...", video, context.getCurrentResultFile());
        context.setDuplicateFactor(fileUtils.getImageDuplicateFactor(video.getAbsolutePath()));

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video);
        int frameNumber = 0;
        try {
            grabber.start();

            Frame frame = null;

            while ((frame = grabber.grabFrame()) != null) {
                // extract the image from the frame
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage image = converter.convert(frame);

                // save the image to disk
                processImage(image, context, outputStream);

                // increment the frame number
                frameNumber++;
            }
        } finally {
            grabber.stop();
            log.info("Frames: {}", frameNumber);
        }
    }

    private void processImage(BufferedImage image, VideosToFilesModel context, OutputStream outputStream) throws IOException {
        context.setPixels(image.getRGB(0, 0, image.getWidth(), image.getHeight(),
                null, 0, image.getWidth()));

        int pixelsIterations = context.getPixels().length / context.getDuplicateFactor() / image.getWidth();
        clearContextTempVariables(context);

        for (int i = 0; i < pixelsIterations; i++) {
            int[][] copiedRows = copyRows(context, image.getWidth(), context.getDuplicateFactor());

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
     * Copy several rows of image using duplicate factor
     *
     * @param width           - width of image
     * @param duplicateFactor - duplicate factor of image
     * @return - several image rows
     */
    private int[][] copyRows(VideosToFilesModel context, int width, int duplicateFactor) {
        int[][] result = new int[duplicateFactor][];

        for (int i = 0; i < result.length; i++) {
            result[i] = copyRow(context.getPixels(), context.getPixelsLastIndex(), width);
            context.setPixelsLastIndex(context.getPixelsLastIndex() + width);
        }
        return result;
    }

    /**
     * Copy one row of image
     *
     * @param width - width of image
     * @return - one image row
     */
    private int[] copyRow(int[] pixels, int pixelsLastIndex, int width) {
        int[] result = new int[width];
        int copyIndex = 0;

        for (int i = pixelsLastIndex; i < pixelsLastIndex + width; i++) {
            result[copyIndex] = pixels[i];
            copyIndex++;
        }
        return result;
    }

    /**
     * Transform several rows of image to one row of bits
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

    private void writeZeroBytes(long zeroBytesCount, OutputStream outputStream) throws IOException {
        for (long i = 0; i < zeroBytesCount; i++) {
            outputStream.write(0);
        }
    }
}
