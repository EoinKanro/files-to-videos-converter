package io.github.eoinkanro.filestoimages.transformer.impl;

import io.github.eoinkanro.filestoimages.conf.ConfigException;
import io.github.eoinkanro.filestoimages.conf.InputCLIArgument;
import io.github.eoinkanro.filestoimages.transformer.TransformException;
import io.github.eoinkanro.filestoimages.transformer.Transformer;
import io.github.eoinkanro.filestoimages.transformer.model.FilesToImagesModel;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.github.eoinkanro.filestoimages.conf.InputCLIArguments.*;
import static io.github.eoinkanro.filestoimages.utils.BytesUtils.ZERO;

@Log4j2
public class FilesToImagesTransformer extends Transformer {

    public FilesToImagesTransformer(InputCLIArgument<Boolean> activeTransformerArgument, InputCLIArgument<String> pathToFileArgument) {
        super(activeTransformerArgument, pathToFileArgument);
    }

    @Override
    protected void checkConfiguration() {
        super.checkConfiguration();

        if (inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) % inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) > 0 ||
            inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT) % inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) > 0) {
            throw new ConfigException("Can't use duplicate factor " + inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) +
                                      ". Image width and height should be divided by it without remainder");
        }
    }

    @Override
    protected void process() {
        File file = new File(inputCLIArgumentsHolder.getArgument(FILES_PATH));
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            processFolder(file.listFiles());
        } else {
            processFile(file);
        }
    }

    /**
     * Process folder with files
     *
     * @param folder - folder
     */
    private void processFolder(File[] folder) {
        if (folder == null) {
            return;
        }

        //TODO multi-thread
        for (File file : folder) {
            if (file.isDirectory()) {
                processFolder(file.listFiles());
            } else {
                processFile(file);
            }
        }
    }

    /**
     * Process one file
     *
     * @param file - file
     */
    private void processFile(File file) {
        log.info("Processing {}...", file);
        FilesToImagesModel context = new FilesToImagesModel();

        calculateSizeOfIndex(context, file);

        try (InputStream inputStream = new FileInputStream(file)) {
            long imageIndex = 0;
            int aByte;
            initImage(context);
            initTempRow(context);

            while ((aByte = inputStream.read()) >= 0) {
                String bits = bytesUtils.byteToBits(aByte);

                for (int i = 0; i < bits.length(); i++) {
                    if (context.getTempRowIndex() >= context.getTempRow().length) {
                        writeDuplicateTempRow(context);
                        initTempRow(context);
                    }

                    if (context.getPixelIndex() >= context.getPixels().length) {
                        writeImage(context, file, imageIndex);
                        imageIndex++;
                        initImage(context);
                    }

                    int pixel = bytesUtils.bitToPixel(Integer.parseInt(String.valueOf(bits.charAt(i))));
                    context.getTempRow()[context.getTempRowIndex()] = pixel;
                    context.incrementTempRowIndex();
                }
            }

            processLastPixels(context, file, imageIndex);
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }

    /**
     * Calculate size of index
     * {@link io.github.eoinkanro.filestoimages.utils.FileUtils#getResultFileForFilesToImages}
     *
     * @param file - image file
     * @param context - context of file
     */
    private void calculateSizeOfIndex(FilesToImagesModel context, File file) {
        long totalPixels = file.length() * 8 * (long) Math.pow(inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR), 2);
        int pixelsInOneImage = inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) * inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT);

        context.setSizeOfIndex(String.valueOf(Math.round(totalPixels / (double) pixelsInOneImage) + 1).length());
    }

    /**
     * Init necessary parameters for new image
     *
     * @param context - context of file
     */
    private void initImage(FilesToImagesModel context) {
        BufferedImage bufferedImage = new BufferedImage(inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH), inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT), BufferedImage.TYPE_INT_RGB);
        context.setBufferedImage(bufferedImage);
        context.setPixels(bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(),
                null, 0, bufferedImage.getWidth()));
        context.setPixelIndex(0);
    }

    /**
     * Init temp row for pixels without duplicate factor
     *
     * @param context - context of file
     */
    private void initTempRow(FilesToImagesModel context) {
        context.setTempRow(new int[inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) / inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR)]);
        context.setTempRowIndex(0);
    }

    /**
     * Write several temp rows to result image using duplicate factor
     *
     * @param context - context of file
     */
    private void writeDuplicateTempRow(FilesToImagesModel context) {
        for (int i = 0; i < inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR); i++) {
            writeTempRow(context);
        }
    }

    /**
     * Write one temp row with pixels to result image using duplicate factor
     *
     * @param context - context of file
     */
    private void writeTempRow(FilesToImagesModel context) {
        for (int pixel : context.getTempRow()) {
            int[] pixels = context.getPixels();

            for (int i = 0; i < inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR); i++) {
                pixels[context.getPixelIndex()] = pixel;
                context.incrementPixelIndex();
            }
        }
    }

    /**
     * Write image
     *
     * @param context - context of file
     * @param original - original file
     * @param imageIndex - index of image
     * @throws IOException - if something wrong on save image
     */
    private void writeImage(FilesToImagesModel context, File original, long imageIndex) throws IOException {
        File file = fileUtils.getResultFileForFilesToImages(original, imageIndex, context.getSizeOfIndex());
        log.info("Writing {}...", file);

        context.getBufferedImage().setRGB(0, 0, inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH), inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT),
                context.getPixels(), 0, inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH));
        ImageIO.write(context.getBufferedImage(), "PNG", file);
    }

    /**
     * Set last pixels of image to {@link io.github.eoinkanro.filestoimages.utils.BytesUtils#ZERO}
     * And save image
     *
     * @param context - context of file
     * @param original - original file
     * @param imageIndex - index of image
     * @throws IOException - if something wrong on save image
     */
    private void processLastPixels(FilesToImagesModel context, File original, long imageIndex) throws IOException {
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
            writeImage(context, original, imageIndex);
        }
    }

}