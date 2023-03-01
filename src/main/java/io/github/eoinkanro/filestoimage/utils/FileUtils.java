package io.github.eoinkanro.filestoimage.utils;

import io.github.eoinkanro.filestoimage.conf.InputCLIArgumentsHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static io.github.eoinkanro.filestoimage.conf.InputCLIArguments.FILES_PATH;

@Component
public class FileUtils {

    public static final String INDEX_SEPARATOR = "-";

    @Autowired
    private InputCLIArgumentsHolder inputCLIArgumentsHolder;

    /**
     * Get result file when transform files to images
     *
     * @param original - file
     * @param imageIndex - index of image
     * @param indexSize - size of index in names of file.
     *                    example: size 5, index 1, result 00001
     * @return - result file
     */
    public File getResultFileForFilesToImages(File original, long imageIndex, int indexSize) throws IOException {
        String originalAbsolutePath = original.getAbsolutePath();
        StringBuilder resultBuilder = new StringBuilder();

        resultBuilder.append(getResultPathForImages());
        resultBuilder.append(File.separator);

        if (!originalAbsolutePath.contains(getCurrentPath())) {
            resultBuilder.append(originalAbsolutePath.substring(originalAbsolutePath.indexOf(File.separator)));
        } else {
            String pathWithoutBeginning = originalAbsolutePath.substring(getAbsolutePath(inputCLIArgumentsHolder.getArgument(FILES_PATH)).length());

            if (pathWithoutBeginning.isBlank()) {
                pathWithoutBeginning = File.separator + original.getName();
            } else if (!pathWithoutBeginning.substring(0, 1).equals(File.separator)) {
                pathWithoutBeginning = File.separator + pathWithoutBeginning;
            }

            resultBuilder.append(pathWithoutBeginning);
        }

        int repeatsZeros = indexSize - String.valueOf(imageIndex).length();
        String calculatedImageIndex = "0".repeat(repeatsZeros) + imageIndex;

        resultBuilder.append(INDEX_SEPARATOR);
        resultBuilder.append(calculatedImageIndex);
        resultBuilder.append(".png");

        File result = new File(resultBuilder.toString());
        createFile(result);
        return result;
    }

    /**
     * Get result file when transform images to files
     *
     * @param originalPath - original path of file {@link #getOriginalNameOfImage}
     * @return - result file
     * @throws IOException - if cant create result file
     */
    public File getResultFileForImagesToFiles(String originalPath) throws IOException {
        File result = new File(getResultPathForFiles()  + originalPath);
        createFile(result);
        return result;
    }

    /**
     * Get result file when transform images to videos
     *
     * @param originalPath - original path of file {@link #getOriginalNameOfImage}
     * @return - result file
     * @throws IOException - if cant create result file
     */
    public File getResultFileForImagesToVideos(String originalPath) throws IOException {
        File result = new File(getResultPathForVideos()  + originalPath + ".mp4");
        createFile(result);
        return result;
    }

    /**
     * Get original name of file without index and path before file name
     *
     * @param original - image
     * @param startPath - path where searching was started
     * @return - original name of file
     */
    public String getOriginalNameOfImage(File original, String startPath) {
        String result = original.getAbsolutePath().substring(getAbsolutePath(startPath).length());

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
     * Create file if it doesn't exist
     *
     * @param file - file
     * @throws IOException - if can't create file
     */
    private void createFile(File file) throws IOException {
        if (!file.exists() && !file.getParentFile().mkdirs() && !file.createNewFile()) {
            throw new FileException("Cant create file " + file);
        }
    }

    /**
     * {@link #getResultPath(String)}
     */
    public String getResultPathForImages() {
        return getResultPath("resultImages");
    }

    /**
     * {@link #getResultPath(String)}
     */
    public String getResultPathForFiles() {
        return getResultPath("resultFiles");
    }

    /**
     * {@link #getResultPath(String)}
     */
    public String getResultPathForVideos() {
        return getResultPath("resultVideos");
    }

    /**
     * Get result path for folder with files that will be generated
     *
     * @param resultFolderName - folder name for generated files
     * @return - path to folder
     */
    private String getResultPath(String resultFolderName) {
        return getCurrentPath() + File.separator + resultFolderName;
    }

    /**
     * Get current path of launched app
     *
     * @return - current path
     */
    public String getCurrentPath() {
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
}
