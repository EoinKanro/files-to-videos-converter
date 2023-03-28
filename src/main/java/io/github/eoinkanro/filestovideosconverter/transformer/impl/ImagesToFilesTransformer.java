package io.github.eoinkanro.filestovideosconverter.transformer.impl;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgument;
import io.github.eoinkanro.filestovideosconverter.transformer.ImagesTransformer;
import io.github.eoinkanro.filestovideosconverter.transformer.TransformException;
import io.github.eoinkanro.filestovideosconverter.utils.concurrent.TransformerTask;
import io.github.eoinkanro.filestovideosconverter.transformer.model.ImagesToFilesModel;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.*;

@Log4j2
public class ImagesToFilesTransformer extends ImagesTransformer {

    public ImagesToFilesTransformer(InputCLIArgument<Boolean> activeTransformerArgument, InputCLIArgument<String> pathToFileArgument) {
        super(activeTransformerArgument, pathToFileArgument);
    }

    @Override
    protected void prepareConfiguration() {
        super.prepareConfiguration();

        ImageIO.setUseCache(false);
    }

    @Override
    protected void process() {
        File file = new File(fileUtils.getResultPathForImages());
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            processFolder(file.listFiles());
        } else {
            ImagesToFilesModel context = new ImagesToFilesModel();
            transformerTaskExecutor.submitTask(new ImagesToFilesTransformerTask(context, file));
        }
        deleteImages(file);
    }

    /**
     * Process folder with images
     *
     * @param folder - folder
     */
    private void processFolder(File[] folder) {
        if (folder == null) {
            return;
        }

        List<File> folders = new ArrayList<>();
        Map<String, List<File>> fileAndImages = new HashMap<>();

        for (File file : folder) {
            if (file.isDirectory()) {
                folders.add(file);
                continue;
            }

            String originalFileName = fileUtils.getOriginalNameOfImage(file, inputCLIArgumentsHolder.getArgument(IMAGES_PATH));
            fileAndImages.computeIfAbsent(originalFileName, k -> new ArrayList<>()).add(file);
        }

        for (Map.Entry<String, List<File>> entry : fileAndImages.entrySet()) {
            ImagesToFilesModel context = new ImagesToFilesModel();

            context.setCurrentOriginalFile(entry.getKey());
            List<File> images = entry.getValue();
            sortImagesByIndexes(images);

            File[] imagesFiles = new File[images.size()];
            images.toArray(imagesFiles);

            try {
                transformerTaskExecutor.submitTask(new ImagesToFilesTransformerTask(context, imagesFiles));
            } catch (Exception e) {
                log.error("Failed to process {}", context.getCurrentOriginalFile(), e);
            }
        }

        for (File file : folders) {
            processFolder(file.listFiles());
        }

        for (File file : folder) {
            deleteImages(file);
        }
    }

    private void sortImagesByIndexes(List<File> images) {
        images.sort((o1, o2) -> {
            int o1Index = fileUtils.getImageIndex(o1.getAbsolutePath());
            int o2Index = fileUtils.getImageIndex(o2.getAbsolutePath());

            return Integer.compare(o1Index, o2Index);
        });
    }

    private class ImagesToFilesTransformerTask extends TransformerTask {

        private final ImagesToFilesModel context;
        private final File[] images;

        public ImagesToFilesTransformerTask(ImagesToFilesModel context, File... images) {
            this.context = context;
            this.images = images;
        }

        @Override
        protected void process() {
            processFiles(context, images);
        }

        /**
         * Process images of one file
         *
         * @param images - images of one file
         */
        private void processFiles(ImagesToFilesModel context, File... images) {
            if (context.getCurrentOriginalFile() == null) {
                context.setCurrentOriginalFile(fileUtils.getOriginalNameOfImage(images[0], inputCLIArgumentsHolder.getArgument(IMAGES_PATH)));
            }

            File resultFile;
            try {
                resultFile = fileUtils.getImagesToFilesResultFile(context.getCurrentOriginalFile());
                context.setCurrentResultFile(resultFile.getAbsolutePath());
            } catch (Exception e) {
                throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
            }

            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(resultFile))) {
                for (File file : images) {
                    processFile(context, file, outputStream);
                }

                int lastZeroBytesCount = fileUtils.getImageLastZeroBytesCount(images[0].getAbsolutePath());
                for (int i = 0; i < lastZeroBytesCount; i++) {
                    outputStream.write(0);
                }
            } catch (Exception e) {
                throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
            }
        }

        /**
         * Write bits from pixels of image to file
         *
         * @param context      - context of file
         * @param file         - image
         * @param outputStream - result file
         * @throws IOException - if something goes wrong with writing file
         */
        private void processFile(ImagesToFilesModel context, File file, OutputStream outputStream) throws IOException {
            log.info("Processing {} to {}...", file, context.getCurrentResultFile());

            BufferedImage image;
            try(BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                image = ImageIO.read(bufferedInputStream);
            } catch (Exception e) {
                throw new IOException(e);
            }

            context.setPixels(image.getRGB(0, 0, image.getWidth(), image.getHeight(),
                    null, 0, image.getWidth()));

            int duplicateFactor = fileUtils.getImageDuplicateFactor(file.getAbsolutePath());
            int pixelsIterations = context.getPixels().length / duplicateFactor / image.getWidth();
            clearContextTempVariables(context);

            for (int i = 0; i < pixelsIterations; i++) {
                int[][] copiedRows = copyRows(context, image.getWidth(), duplicateFactor);

                int[] bitsRow = transformToBitRow(copiedRows, duplicateFactor);
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
        private void clearContextTempVariables(ImagesToFilesModel context) {
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
        private int[][] copyRows(ImagesToFilesModel context, int width, int duplicateFactor) {
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

}
