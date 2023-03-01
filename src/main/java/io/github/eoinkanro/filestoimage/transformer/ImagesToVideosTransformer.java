package io.github.eoinkanro.filestoimage.transformer;

import io.github.eoinkanro.filestoimage.conf.InputCLIArguments;
import io.github.eoinkanro.filestoimage.utils.CommandLineExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

import static io.github.eoinkanro.filestoimage.conf.InputCLIArguments.IMAGES_TO_VIDEOS;
import static io.github.eoinkanro.filestoimage.conf.OutputCLIArguments.*;
import static io.github.eoinkanro.filestoimage.utils.FileUtils.INDEX_SEPARATOR;

@Component
@Log4j2
public class ImagesToVideosTransformer extends Transformer {

    @Autowired
    private CommandLineExecutor commandLineExecutor;

    @Override
    public void transform() {
        if (Boolean.FALSE.equals(inputCLIArgumentsHolder.getArgument(IMAGES_TO_VIDEOS))) {
            return;
        }

        File folder = new File(fileUtils.getResultPathForImages());
        if (folder.exists()) {
            processFolder(folder.listFiles());
        }
    }

    private void processFolder(File[] folder) {
        if (folder == null) {
            return;
        }

        List<File> folders = new ArrayList<>();
        Map<String, String> originalNamesExampleImage = new HashMap<>();

        for (File file : folder) {
            if (file.isDirectory()) {
                folders.add(file);
                continue;
            }

            String originalFileName = fileUtils.getOriginalNameOfImage(file, fileUtils.getResultPathForImages());
            originalNamesExampleImage.computeIfAbsent(originalFileName, k -> file.getAbsolutePath());
        }

        for (Map.Entry<String, String> entry : originalNamesExampleImage.entrySet()) {
            processFile(entry.getKey(), entry.getValue());
        }
        for (File file : folders) {
            processFolder(file.listFiles());
        }
    }

    private void processFile(String originalName, String exampleImagePath) {
        try {
            File resultFile = fileUtils.getResultFileForImagesToVideos(originalName);
            log.info("Writing {}...", resultFile);

            int indexSize = exampleImagePath.substring(exampleImagePath.lastIndexOf(INDEX_SEPARATOR) + 1,
                    exampleImagePath.lastIndexOf(".")).length();
            String findPattern = exampleImagePath.substring(0, exampleImagePath.lastIndexOf(INDEX_SEPARATOR))
                    + "-%"
                    + indexSize
                    + "d.png";

            boolean isWritten = commandLineExecutor.execute(
                    FFMPEG.getValue(),
                    DEFAULT_YES.getValue(),
                    FRAMERATE.getValue(),
                    inputCLIArgumentsHolder.getArgument(InputCLIArguments.FRAMERATE),
                    PATTERN_TYPE.getValue(),
                    SEQUENCE.getValue(),
                    START_NUMBER.getValue(),
                    "0".repeat(indexSize),
                    INPUT.getValue(),
                    BRACKETS_PATTERN.formatValue(findPattern),
                    CODEC_VIDEO.getValue(),
                    LIBX264.getValue(),
                    BRACKETS_PATTERN.formatValue(resultFile.getAbsolutePath()));

            if (!isWritten) {
                log.error("Error while writing {}", resultFile);
            }
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }

}
