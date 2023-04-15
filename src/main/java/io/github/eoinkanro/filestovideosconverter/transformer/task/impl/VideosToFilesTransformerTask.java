package io.github.eoinkanro.filestovideosconverter.transformer.task.impl;

import io.github.eoinkanro.filestovideosconverter.transformer.TransformException;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTask;
import lombok.extern.log4j.Log4j2;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.*;
import java.nio.ByteBuffer;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.VIDEOS_PATH;

@Log4j2
public class VideosToFilesTransformerTask extends TransformerTask {

    private int duplicateFactor;
    private int imageWidth;

    private StringBuilder byteBuilder;
    private long zeroBytesCount;

    private byte[] pixels;
    private int pixelsLastIndex;

    public VideosToFilesTransformerTask(File processData) {
        super(processData);
    }

    @Override
    protected void process() {
        log.info("Processing {}...", processData);
        taskStatistics.setFilePath(processData.getAbsolutePath());

        File resultFile;
        try {
            String currentOriginalFile = fileUtils.getOriginalNameOfFile(processData, inputCLIArgumentsHolder.getArgument(VIDEOS_PATH));
            resultFile = fileUtils.getVideosToFilesResultFile(currentOriginalFile);
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(resultFile))) {
            duplicateFactor = fileUtils.getImageDuplicateFactor(processData.getAbsolutePath());
            processFile(processData, outputStream);

            int lastZeroBytesCount = fileUtils.getImageLastZeroBytesCount(processData.getAbsolutePath());
            for (int i = 0; i < lastZeroBytesCount; i++) {
                outputStream.write(0);
            }
        } catch (Exception e) {
            log.error(COMMON_EXCEPTION_DESCRIPTION, e);
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }

        taskStatistics.logResult();
        log.info("File {} was processed successfully", processData);
    }

    /**
     * Write bits from pixels of images from video to file
     *
     * @param video        - video
     * @param outputStream - result file
     * @throws IOException - if something goes wrong with writing file
     */
    private void processFile(File video, OutputStream outputStream) throws IOException {
        try(FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video)) {
            grabber.start();

            try(FFmpegFrameFilter filter = new FFmpegFrameFilter("format=gray", grabber.getImageWidth(), grabber.getImageHeight())) {
                filter.start();

                processFile(grabber, filter, outputStream);
            }
        }
    }

    private void processFile(FFmpegFrameGrabber grabber, FFmpegFrameFilter filter, OutputStream outputStream) throws IOException {
        Frame frame = null;
        while ((frame = grabber.grabFrame()) != null) {
            imageWidth = frame.imageWidth;

            filter.push(frame);
            frame = filter.pull();

            if (frame.type != null) {
                continue;
            }

            pixels = new byte[frame.imageHeight * frame.imageWidth];
            ((ByteBuffer) frame.image[0]).get(pixels);

            processImage(outputStream);

            taskStatistics.poll();
        }
    }

    /**
     * Process one image from video
     *
     * @param outputStream - result file
     * @throws IOException - if bytes can't be written to result file
     */
    private void processImage(OutputStream outputStream) throws IOException {
        int pixelsIterations = pixels.length / imageWidth / duplicateFactor;
        clearContextTempVariables();

        for (int i = 0; i < pixelsIterations; i++) {
            byte[][] copiedRows = copyPixelRowsFromImage();

            int[] bitsRow = transformToBitRow(copiedRows, duplicateFactor);
            for (int bit : bitsRow) {
                if (bit >= 0) {
                    byteBuilder.append(bit);
                }

                if (byteBuilder.length() >= 8) {
                    int aByte = Integer.parseInt(byteBuilder.toString(), 2);
                    if (aByte == 0) {
                        zeroBytesCount++;
                        byteBuilder = new StringBuilder();
                        continue;
                    }

                    writeZeroBytes(zeroBytesCount, outputStream);
                    outputStream.write(aByte);

                    zeroBytesCount = 0;
                    byteBuilder = new StringBuilder();
                }
            }
        }
    }

    /**
     * Clear temp variables of context
     */
    private void clearContextTempVariables() {
        byteBuilder = new StringBuilder();
        pixelsLastIndex = 0;
    }

    /**
     * Copy several rows of image pixels using duplicate factor
     *
     * @return - several image rows
     */
    private byte[][] copyPixelRowsFromImage() {
        byte[][] result = new byte[duplicateFactor][];

        for (int i = 0; i < result.length; i++) {
            result[i] = copyPixelRowFromImage(pixels, pixelsLastIndex, imageWidth);
            pixelsLastIndex = pixelsLastIndex + imageWidth;
        }
        return result;
    }

    /**
     * Copy one row of image
     *
     * @param width - width of image
     * @return - one image row
     */
    private byte[] copyPixelRowFromImage(byte[] pixels, int pixelsLastIndex, int width) {
        byte[] result = new byte[width];
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
    private int[] transformToBitRow(byte[][] copiedRows, int duplicateFactor) {
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

            for (byte[] row : copiedRows) {
                pixelSum += bytesUtils.grayByteToRgb(row[i]);
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
