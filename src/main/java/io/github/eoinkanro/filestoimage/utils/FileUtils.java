package io.github.eoinkanro.filestoimage.utils;

import io.github.eoinkanro.filestoimage.conf.CommandLineArgumentsHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static io.github.eoinkanro.filestoimage.conf.CommandLineArguments.FILES_PATH;
import static io.github.eoinkanro.filestoimage.conf.CommandLineArguments.IMAGES_PATH;

@Component
public class FileUtils {

    private static final String INDEX_SEPARATOR = "-";

    @Autowired
    private CommandLineArgumentsHolder commandLineArgumentsHolder;

    /**
     * Get result file when transform file to images
     *
     * @param original - file
     * @param imageIndex - index of image
     * @return - result file
     */
    public File getResultFileForFileToImage(File original, long imageIndex) {
        String originalAbsolutePath = original.getAbsolutePath();
        StringBuilder resultBuilder = new StringBuilder();

        resultBuilder.append(getResultFolderForImages());
        resultBuilder.append(File.separator);

        if (!originalAbsolutePath.contains(getCurrentPath())) {
            resultBuilder.append(originalAbsolutePath.substring(originalAbsolutePath.indexOf(File.separator)));
        } else {
            String pathWithoutBeginning = originalAbsolutePath.substring(getAbsolutePath(commandLineArgumentsHolder.getArgument(FILES_PATH)).length());

            if (pathWithoutBeginning.isBlank()) {
                pathWithoutBeginning = File.separator + original.getName();
            } else if (!pathWithoutBeginning.substring(0, 1).equals(File.separator)) {
                pathWithoutBeginning = File.separator + pathWithoutBeginning;
            }

            resultBuilder.append(pathWithoutBeginning);
        }

        resultBuilder.append(INDEX_SEPARATOR);
        resultBuilder.append(imageIndex);
        resultBuilder.append(".png");

        File result = new File(resultBuilder.toString());
        if (!result.getParentFile().exists() && !result.getParentFile().mkdirs()) {
            throw new FileException("Cant create file " + result);
        }
        return result;
    }

    /**
     * Get original name of file without index and path before file name
     *
     * @param original - image
     * @return - original name of file
     */
    public String getOriginalNameOfImage(File original) {
        String result = original.getAbsolutePath().substring(getAbsolutePath(commandLineArgumentsHolder.getArgument(IMAGES_PATH)).length());

        if (result.isBlank()) {
            result = original.getName();
        }

        if (result.contains(INDEX_SEPARATOR)) {
            result = result.substring(0, result.lastIndexOf(INDEX_SEPARATOR));
        }

        if (!result.substring(0, 1).equals(File.separator)) {
            result = File.separator + result;
        }

        return result;
    }

    /**
     * Get result file when transform images to file
     *
     * @param originalPath - original path of file {@link #getOriginalNameOfImage}
     * @return - result file
     * @throws IOException - if cant create result file
     */
    public File getResultFileForImageToFile(String originalPath) throws IOException {
        File result = new File(getResultFolderForFiles()  + originalPath);
        if (!result.exists() && (!result.getParentFile().mkdirs() || !result.createNewFile())) {
            throw new FileException("Cant create file " + result);
        }
        return result;
    }

    /**
     * Get index of image
     *
     * @param file - image
     * @return - index
     */
    public long getImageIndex(File file) {
        String fileName = file.getName();
        if (fileName.contains(INDEX_SEPARATOR)) {
            fileName = fileName.substring(fileName.lastIndexOf(INDEX_SEPARATOR), fileName.lastIndexOf("."));
            return Long.parseLong(fileName);
        }
        return 0;
    }

    /**
     * Get result folder for images that will be created
     *
     * @return - result folder
     */
    private String getResultFolderForImages() {
        return getResultFolder("resultImages");
    }

    /**
     * Get result folder for files that will be created
     *
     * @return - result folder
     */
    private String getResultFolderForFiles() {
        return getResultFolder("resultFiles");
    }

    private String getResultFolder(String resultFolderName) {
        return getCurrentPath() + File.separator + resultFolderName;
    }

    /**
     * Get current path of launched app
     *
     * @return - current path
     */
    private String getCurrentPath() {
        return Path.of("").toAbsolutePath().toString();
    }

    /**
     * Transform file name to full path to file if it's necessary
     */
    public String getAbsolutePath(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }

        File file = new File(path);
        if (!file.isAbsolute()) {
            return getCurrentPath()
                    + File.separator
                    + path;
        }
        return path;
    }

}
