package io.github.eoinkanro.filestoimage.transformer;

import io.github.eoinkanro.filestoimage.conf.ConfigException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static io.github.eoinkanro.filestoimage.conf.CommandLineArguments.*;

@Component
@Log4j2
public class ImageToFileTransformer extends Transformer {

    private String currentOriginalFile = null;
    private String currentResultFile = null;

    @Override
    public void transform() {
        if (Boolean.FALSE.equals(commandLineArgumentsHolder.getArgument(IMAGES_TO_FILE))) {
            return;
        }
        checkConfiguration();
        process();
    }

    private void checkConfiguration() {
        if (StringUtils.isBlank(commandLineArgumentsHolder.getArgument(IMAGES_PATH))) {
            throw new ConfigException("Target path for transforming images to file is empty");
        }
        if (!new File(commandLineArgumentsHolder.getArgument(IMAGES_PATH)).exists()) {
            throw new ConfigException("Target path for transforming images to file doesn't exist");
        }
    }

    private void process() {
        File file = new File(commandLineArgumentsHolder.getArgument(IMAGES_PATH));
        if (file.isDirectory()) {
            processFolder(file.listFiles());
        } else {
            processFiles(file);
        }
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

            String originalFileName = fileUtils.getOriginalNameOfImage(file);
            fileAndImages.computeIfAbsent(originalFileName, k -> new ArrayList<>()).add(file);
        }

        for (Map.Entry<String, List<File>> entry : fileAndImages.entrySet()) {
            currentOriginalFile = entry.getKey();
            List<File> images = entry.getValue();
            sortImagesByIndexes(images);

            File[] imagesFiles = new File[images.size()];
            images.toArray(imagesFiles);

            try {
                processFiles(imagesFiles);
            } catch (Exception e) {
                log.error("Failed to process {}", currentOriginalFile, e);
            }
        }

        for (File file : folders) {
            processFolder(file.listFiles());
        }
    }

    private void sortImagesByIndexes(List<File> images) {
        images.sort((o1, o2) -> {
            long o1Index = fileUtils.getImageIndex(o1);
            long o2Index = fileUtils.getImageIndex(o2);

            return Long.compare(o2Index, o1Index);
        });
    }

    /**
     * Process images of one file
     *
     * @param files - images of one file
     */
    private void processFiles(File... files) {
        //TODO multi-thread
        if (currentOriginalFile == null) {
            currentOriginalFile = fileUtils.getOriginalNameOfImage(files[0]);
        }

        File resultFile;
        try {
            resultFile = fileUtils.getResultFileForImageToFile(currentOriginalFile);
            currentResultFile = resultFile.getAbsolutePath();
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }

        try (OutputStream outputStream = new FileOutputStream(resultFile)) {
            for (File file : files) {
                processFile(file, outputStream);
            }
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }

    /**
     * Write bits from pixels of image to file
     *
     * @param file - image
     * @param outputStream - result file
     * @throws IOException - if something goes wrong with writing file
     */
    private void processFile(File file, OutputStream outputStream) throws IOException {
        log.info("Processing {} to {}...", file, currentResultFile);

        BufferedImage image = ImageIO.read(file);
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(),
                null, 0, image.getWidth());

        StringBuilder byteBuilder = new StringBuilder();
        for (int pixel : pixels) {
            int bit = bytesUtils.pixelToBit(pixel);

            if (bit >= 0) {
                byteBuilder.append(bit);
            }
            if (byteBuilder.length() >= 8) {
                int b = Integer.parseInt(byteBuilder.toString(), 2);
                byteBuilder = new StringBuilder();
                outputStream.write(b);
            }
            if (bit < 0) {
                break;
            }
        }
    }

}
