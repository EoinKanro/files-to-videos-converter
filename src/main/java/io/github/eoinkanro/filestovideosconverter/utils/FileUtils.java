package io.github.eoinkanro.filestovideosconverter.utils;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.*;

@Log4j2
@Component
public class FileUtils {

    public static final String INDEX_SIZE_SEPARATOR_SUPPORT = "-i";
    public static final String DUPLICATE_FACTOR_SEPARATOR = "-d";
    public static final String LAST_ZERO_BYTES_COUNT_SEPARATOR = "-z";

    @Autowired
    private InputCLIArgumentsHolder inputCLIArgumentsHolder;
    @Autowired
    private CommonUtils commonUtils;


    //--------------- Result files -------------------

    /**
     * Get result file when transform files to images
     *
     * @param original - file
     * @return - result file
     */
    public File getFilesToVideosResultFile(File original, int lastZeroBytesCount) throws IOException {
        String originalAbsolutePath = original.getAbsolutePath();
        StringBuilder resultBuilder = new StringBuilder();

        resultBuilder.append(getResultPathForVideos());
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

        resultBuilder.append(DUPLICATE_FACTOR_SEPARATOR);
        resultBuilder.append(inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR));
        resultBuilder.append(LAST_ZERO_BYTES_COUNT_SEPARATOR);
        resultBuilder.append(lastZeroBytesCount);
        resultBuilder.append(".mp4");

        File result = new File(resultBuilder.toString());
        createFile(result);
        return result;
    }

    /**
     * Get result file when transform images to files
     *
     * @param originalPath - original path of file {@link #getOriginalNameOfFile}
     * @return - result file
     * @throws IOException - if cant create result file
     */
    public File getVideosToFilesResultFile(String originalPath) throws IOException {
        File result = new File(getResultPathForFiles()  + originalPath);
        createFile(result);
        return result;
    }

    //---------------- Result Folder Paths --------------------

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

    //------------------- Metadata -------------------

    /**
     * Calculate amount of zero bytes at the end of file
     *
     * @param file - file
     * @return - amount of zero bytes
     */
    public int calculateLastZeroBytesAmount(File file) {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            boolean done = false;
            int lastZeroBytesCount = 0;
            int index = 1;

            while (!done) {
                randomAccessFile.seek(file.length() - index);
                if (randomAccessFile.read() == 0) {
                    lastZeroBytesCount++;
                    index++;
                } else {
                    done = true;
                }
            }

            return lastZeroBytesCount;
        } catch (Exception e) {
            throw new FileException("Error during reading last bytes of file " + file, e);
        }
    }

    /**
     * Get duplicate factor of image
     *
     * @param filePath - image file path
     * @return - duplicate factor
     */
    public int getImageDuplicateFactor(String filePath) {
        return commonUtils.parseInt(getStringMetadata(filePath, DUPLICATE_FACTOR_SEPARATOR, LAST_ZERO_BYTES_COUNT_SEPARATOR));
    }

    /**
     * Get count of last zero bytes of file
     *
     * @param filePath - file path
     * @return - count
     */
    public int getImageLastZeroBytesCount(String filePath) {
        return commonUtils.parseInt(getStringMetadata(filePath, LAST_ZERO_BYTES_COUNT_SEPARATOR, "."));
    }

    /**
     * Get metadata between tag and symbol after metadata
     *
     * @param filePath - path of file
     * @param tag - tag
     * @param lastSymbol - last symbol after tag
     * @return - metadata. Example: /test.png-d2-z0.mp4
     *                         tag: {@link #DUPLICATE_FACTOR_SEPARATOR}
     *                  lastSymbol: {@link #LAST_ZERO_BYTES_COUNT_SEPARATOR}
     *                      result: 2
     */
    private String getStringMetadata(String filePath, String tag, String lastSymbol) {
        if (filePath.contains(tag)) {
            return filePath.substring(filePath.lastIndexOf(tag) + 2, filePath.lastIndexOf(lastSymbol));
        }
        return "";
    }

    /**
     * Get original name of file without metadata and start path
     * Example: video - C:/video/1/video.png-d2-z2.mp4
     *          startPath - C:/video
     *          result - /1/video.png
     *
     * @param file - file with metadata
     * @param startPath - path where searching was started
     * @return - original name of file without metadata and start path
     */
    public String getOriginalNameOfFile(File file, String startPath) {
        String result = file.getAbsolutePath().substring(getAbsolutePath(startPath).length());

        if (result.isBlank()) {
            result = file.getName();
        }

        if (result.contains(INDEX_SIZE_SEPARATOR_SUPPORT)) {
            result = result.substring(0, result.lastIndexOf(INDEX_SIZE_SEPARATOR_SUPPORT));
        } else if (result.contains(DUPLICATE_FACTOR_SEPARATOR)) {
            result = result.substring(0, result.lastIndexOf(DUPLICATE_FACTOR_SEPARATOR));
        }

        if (!result.substring(0, 1).equals(File.separator)) {
            result = File.separator + result;
        }

        return result;
    }

    //----------------- Create file ---------------------

    /**
     * Create file if it doesn't exist
     *
     * @param file - file
     * @throws IOException - if file can't be created
     */
    private void createFile(File file) throws IOException {
        if (!file.exists() && !file.getParentFile().mkdirs() && !file.createNewFile()) {
            throw new IOException("Can't create file " + file);
        }
    }

}
