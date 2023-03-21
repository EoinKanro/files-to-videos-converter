package io.github.eoinkanro.filestoimages.transformer;

import io.github.eoinkanro.filestoimages.conf.ConfigException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.github.eoinkanro.filestoimages.conf.InputCLIArguments.*;
import static io.github.eoinkanro.filestoimages.utils.BytesUtils.ZERO;

@Component
@Log4j2
public class FilesToImagesTransformer extends Transformer {

    private BufferedImage bufferedImage;
    private int[] pixels;
    private int pixelIndex;
    private int sizeOfIndex;

    private int[] tempRow;
    private int tempRowIndex;

    @Override
    public void transform() {
        if (Boolean.FALSE.equals(inputCLIArgumentsHolder.getArgument(FILES_TO_IMAGES))) {
            return;
        }
        checkConfiguration();
        process();
    }

    private void checkConfiguration() {
        //TODO do the same
        //MB create abstract methods
        if (StringUtils.isBlank(inputCLIArgumentsHolder.getArgument(FILES_PATH))) {
            throw new ConfigException("Target path for transforming files to images is empty");
        }
        //TODO
        String input = fileUtils.getAbsolutePath(inputCLIArgumentsHolder.getArgument(FILES_PATH));
        log.info(input);
        if (!new File(input).exists()) {
            throw new ConfigException("Target path for transforming files to images doesn't exist");
        }

        if (inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) % inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) > 0 ||
            inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT) % inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) > 0) {
            throw new ConfigException("Can't use duplicate factor " + inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) +
                                      ". Image width and height should be divided by it without remainder");
        }
    }

    private void process() {
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
        calculateSizeOfIndex(file);

        try (InputStream inputStream = new FileInputStream(file)) {
            long imageIndex = 0;
            int aByte;
            initImage();
            initTempRow();

            while ((aByte = inputStream.read()) >= 0) {
                String bits = bytesUtils.byteToBits(aByte);

                for (int i = 0; i < bits.length(); i++) {
                    if (tempRowIndex >= tempRow.length) {
                        writeDuplicateTempRow();
                        initTempRow();
                    }

                    if (pixelIndex >= pixels.length) {
                        writeImage(file, imageIndex);
                        imageIndex++;
                        initImage();
                    }

                    int pixel = bytesUtils.bitToPixel(Integer.parseInt(String.valueOf(bits.charAt(i))));
                    tempRow[tempRowIndex] = pixel;
                    tempRowIndex++;
                }
            }

            processLastPixels(file, imageIndex);
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }

    /**
     * Calculate size of index
     * {@link io.github.eoinkanro.filestoimages.utils.FileUtils#getResultFileForFilesToImages}
     *
     * @param file - image file
     */
    private void calculateSizeOfIndex(File file) {
        long totalPixels = file.length() * 8 * (long) Math.pow(inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR), 2);
        int pixelsInOneImage = inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) * inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT);
        sizeOfIndex = String.valueOf(Math.round(totalPixels / (double) pixelsInOneImage) + 1).length();
    }

    /**
     * Init necessary parameters for new image
     */
    private void initImage() {
        bufferedImage = new BufferedImage(inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH), inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT), BufferedImage.TYPE_INT_RGB);
        pixels = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(),
                null, 0, bufferedImage.getWidth());
        pixelIndex = 0;
    }

    /**
     * Init temp row for pixels without duplicate factor
     */
    private void initTempRow() {
        tempRow = new int[inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) / inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR)];
        tempRowIndex = 0;
    }

    /**
     * Write several temp rows to result image using duplicate factor
     */
    private void writeDuplicateTempRow() {
        for (int i = 0; i < inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR); i++) {
            writeTempRow();
        }
    }

    /**
     * Write one temp row with pixels to result image using duplicate factor
     */
    private void writeTempRow() {
        for (int pixel : tempRow) {
            for (int i = 0; i < inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR); i++) {
                pixels[pixelIndex] = pixel;
                pixelIndex++;
            }
        }
    }

    /**
     * Write image
     *
     * @param original - original file
     * @param imageIndex - index of image
     * @throws IOException - if something wrong on save image
     */
    private void writeImage(File original, long imageIndex) throws IOException {
        File file = fileUtils.getResultFileForFilesToImages(original, imageIndex, sizeOfIndex);
        log.info("Writing {}...", file);

        bufferedImage.setRGB(0, 0, inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH), inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT),
                pixels, 0, inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH));
        ImageIO.write(bufferedImage, "PNG", file);
    }

    /**
     * Set last pixels of image to {@link io.github.eoinkanro.filestoimages.utils.BytesUtils#ZERO}
     * And save image
     *
     * @param original - original file
     * @param imageIndex - index of image
     * @throws IOException - if something wrong on save image
     */
    private void processLastPixels(File original, long imageIndex) throws IOException {
        if (tempRowIndex < tempRow.length) {
            for (int i = tempRowIndex; i < tempRow.length; i++) {
                tempRow[i] = ZERO;
            }
            writeDuplicateTempRow();
        }

        if (pixelIndex < pixels.length) {
            for (int i = pixelIndex; i < pixels.length; i++) {
                pixels[i] = ZERO;
            }
            writeImage(original, imageIndex);
        }
    }

}
