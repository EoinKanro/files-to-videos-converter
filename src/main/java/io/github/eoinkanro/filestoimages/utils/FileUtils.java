package io.github.eoinkanro.filestoimages.utils;

import io.github.eoinkanro.filestoimages.conf.InputCLIArgumentsHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static io.github.eoinkanro.filestoimages.conf.InputCLIArguments.*;

@Component
public class FileUtils {

    public static final String INDEX_SEPARATOR = "-i";
    public static final String DUPLICATE_FACTOR_SEPARATOR = "-d";

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
            resultBuilder.append(originalAbsolutePath.substring(originalAbsolutePath.indexOf(File.separator) + 1));
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
        resultBuilder.append(DUPLICATE_FACTOR_SEPARATOR);
        resultBuilder.append(inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR));
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
     * @param image - image
     * @param startPath - start path of founded image {@link #getOriginalNameOfImage(File, String)}
     * @return - result file
     * @throws IOException - if cant create result file
     */
    public File getResultFileForImagesToVideos(File image, String startPath) throws IOException {
        String originalPath = getOriginalNameOfImage(image, startPath);
        int indexSize = getImageIndexSize(image.getAbsolutePath());
        int duplicateFactor = getImageDuplicateFactor(image.getAbsolutePath());
        File result = new File(getResultPathForVideos()
                + originalPath
                + INDEX_SEPARATOR
                + indexSize
                + DUPLICATE_FACTOR_SEPARATOR
                + duplicateFactor
                + ".mp4");

        createFile(result);
        return result;
    }

    /**
     * Get result file pattern for images that will be got from video
     *
     * @param video - video
     * @param startPath - start path of founded videos {@link #getOriginalNameOfImage(File, String)}
     * @return - images pattern
     */
    public String getResultFilePatternForVideosToImages(File video, String startPath) {
        String originalPath = getOriginalNameOfImage(video, startPath);
        int indexSize = getImageIndexSize(video.getAbsolutePath());
        int duplicateFactor = getImageDuplicateFactor(video.getAbsolutePath());
        File resultPathDir = new File(getResultPathForImages() + originalPath).getParentFile();
        createFolder(resultPathDir);

        return getResultPathForImages()
                + originalPath
                + INDEX_SEPARATOR
                + "%"
                + indexSize
                + "d"
                + DUPLICATE_FACTOR_SEPARATOR
                + duplicateFactor
                +".png";
    }

    /**
     * Get original name of file without index and path before file name
     * Example: image - C:/images/1/2/image-01.png
     *          startPath - C:/images
     *          result - /1/2/image.png
     *
     *          video - C:/video/1/video.png-5.mp4
     *          startPath - C:/video
     *          result - /1/video.png
     *
     * @param file - image
     * @param startPath - path where searching was started
     * @return - original name of file without start path
     */
    public String getOriginalNameOfImage(File file, String startPath) {
        String result = file.getAbsolutePath().substring(getAbsolutePath(startPath).length());

        if (result.isBlank()) {
            result = file.getName();
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
            throw new FileException("Can't create file " + file);
        }
    }

    /**
     * Create folder if it doesn't exist
     *
     * @param folder - folder
     */
    private void createFolder(File folder) {
        if (!folder.exists() && !folder.mkdirs()) {
            throw new FileException("Can't create folders " + folder);
        }
    }

    /**
     * {@link #getResultPath(String)}
     */
    public String getResultPathForImages() {
        return getResultPath(inputCLIArgumentsHolder.getArgument(IMAGES_PATH));
    }

    /**
     * {@link #getResultPath(String)}
     */
    public String getResultPathForFiles() {
        return getResultPath(inputCLIArgumentsHolder.getArgument(FILES_PATH));
    }

    /**
     * {@link #getResultPath(String)}
     */
    public String getResultPathForVideos() {
        return getResultPath(inputCLIArgumentsHolder.getArgument(VIDEOS_PATH));
    }

    /**
     * Get result path for folder with files that will be generated
     *
     * @param resultFolderName - folder name for generated files
     * @return - path to folder
     */
    private String getResultPath(String resultFolderName) {
        if (resultFolderName.contains(File.separator)) {
            resultFolderName = resultFolderName.substring(resultFolderName.indexOf(File.separator) + 1);
        }
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
     * @param filePath - image file path
     * @return - index
     */
    public int getImageIndex(String filePath) {
        return parseInt(getImageIndexString(filePath));
    }

    /**
     * Get string value if index in file name
     *
     * @param filePath - file path
     * @return - index
     */
    private String getImageIndexString(String filePath) {
        if (filePath.contains(INDEX_SEPARATOR)) {
            return filePath.substring(filePath.lastIndexOf(INDEX_SEPARATOR) + 2, filePath.lastIndexOf(DUPLICATE_FACTOR_SEPARATOR));
        }
        return "";
    }

    /**
     * Get duplicate factor of image
     *
     * @param filePath - image file path
     * @return - duplicate factor
     */
    public int getImageDuplicateFactor(String filePath) {
        return parseInt(getImageDuplicateFactorString(filePath));
    }

    /**
     * Get string value if index in file name
     *
     * @param filePath - file path
     * @return - index
     */
    private String getImageDuplicateFactorString(String filePath) {
        if (filePath.contains(DUPLICATE_FACTOR_SEPARATOR)) {
            return filePath.substring(filePath.lastIndexOf(DUPLICATE_FACTOR_SEPARATOR) + 2, filePath.lastIndexOf("."));
        }
        return "";
    }

    private int parseInt(String anIntString) {
        if (!StringUtils.isBlank(anIntString)) {
            return Integer.parseInt(anIntString);
        }
        return 0;
    }

    /**
     * Get size of index
     *
     * @param filePath - path to image
     * @return - size of index
     */
    public int getImageIndexSize(String filePath) {
        return getImageIndexString(filePath).length();
    }

    /**
     * Get pattern for images that will be transformed to video
     *
     * @param imageAbsolutePath - absolute path of one of images
     * @return - pattern
     */
    public String getFFmpegImagesPattern(String imageAbsolutePath) {
        int indexSize = getImageIndexSize(imageAbsolutePath);
        int duplicateFactor = getImageDuplicateFactor(imageAbsolutePath);
        return imageAbsolutePath.substring(0, imageAbsolutePath.lastIndexOf(INDEX_SEPARATOR))
                + "-i%"
                + indexSize
                + "d"
                + DUPLICATE_FACTOR_SEPARATOR
                + duplicateFactor
                +".png";
    }
}
