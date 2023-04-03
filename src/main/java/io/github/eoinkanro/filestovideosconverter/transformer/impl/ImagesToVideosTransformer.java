package io.github.eoinkanro.filestovideosconverter.transformer.impl;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgument;
import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments;
import io.github.eoinkanro.filestovideosconverter.transformer.ImagesTransformer;
import io.github.eoinkanro.filestovideosconverter.transformer.TransformException;
import io.github.eoinkanro.filestovideosconverter.utils.CommandLineExecutor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;

import static io.github.eoinkanro.filestovideosconverter.conf.OutputCLIArguments.*;

@Log4j2
public class ImagesToVideosTransformer extends ImagesTransformer {

    @Autowired
    private CommandLineExecutor commandLineExecutor;

    private String os;

    public ImagesToVideosTransformer(InputCLIArgument<Boolean> activeTransformerArgument, InputCLIArgument<String> pathToFileArgument) {
        super(activeTransformerArgument, pathToFileArgument);
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
            processFile(file);
        }
        deleteImages(file);
    }

    private void processFolder(File[] folder) {
        if (folder == null) {
            return;
        }

        List<File> folders = new ArrayList<>();
        Map<String, File> originalNamesExampleImage = new HashMap<>();

        for (File file : folder) {
            if (file.isDirectory()) {
                folders.add(file);
                continue;
            }

            String originalFileName = fileUtils.getOriginalNameOfImage(file, fileUtils.getResultPathForImages());
            originalNamesExampleImage.putIfAbsent(originalFileName, file);
        }

        for (File image : originalNamesExampleImage.values()) {
            processFile(image);
        }
        for (File file : folders) {
            processFolder(file.listFiles());
        }

        for (File file : folder) {
            deleteImages(file);
        }
    }

    private void processFile(File exampleImage) {
        try {
            File resultFile = fileUtils.getImagesToVideosResultFile(exampleImage, fileUtils.getResultPathForImages());
            log.info("Writing {}...", resultFile);

            int indexSize = fileUtils.getImageIndexSize(exampleImage.getAbsolutePath());
            String findPattern = fileUtils.getFFmpegImagesToVideosPattern(exampleImage.getAbsolutePath());

            boolean isWritten = commandLineExecutor.execute(
                    getFFmpegArgumentsBasedOnOS(findPattern, resultFile.getAbsolutePath(), indexSize));

            if (!isWritten) {
                throw new TransformException("Error while writing " + resultFile);
            }
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }

    private String[] getFFmpegArgumentsBasedOnOS(String findPattern, String resultFilePath, int indexSize) {
        String os = getOs();

        String ffmpeg;
        if (os.startsWith("windows")) {
            ffmpeg = FFMPEG_EXE.getValue();
            findPattern = BRACKETS_PATTERN.formatValue(findPattern);
            resultFilePath = BRACKETS_PATTERN.formatValue(resultFilePath);
        } else {
            ffmpeg = FFMPEG.getValue();
        }

        return getFFmpegArguments(ffmpeg, findPattern, resultFilePath, indexSize);
    }

    private String[] getFFmpegArguments(String ffmpeg, String findPattern, String resultFilePath, int indexSize) {
        return new String[] {
                ffmpeg,
                DEFAULT_YES.getValue(),
                FRAMERATE.getValue(),
                inputCLIArgumentsHolder.getArgument(InputCLIArguments.FRAMERATE),
                PATTERN_TYPE.getValue(),
                SEQUENCE.getValue(),
                START_NUMBER.getValue(),
                "0".repeat(indexSize),
                INPUT.getValue(),
                findPattern,
                CODEC_VIDEO.getValue(),
                LIBX264.getValue(),
                MOV_FLAGS.getValue(),
                FAST_START.getValue(),
                CRF.getValue(),
                CRF_18.getValue(),
                PIXEL_FORMAT.getValue(),
                GRAY.getValue(),
                PRESET.getValue(),
                SLOW.getValue(),
                resultFilePath
        };
    }

    private String getOs() {
        if (StringUtils.isEmpty(os)) {
            os = System.getProperty("os.name").toLowerCase();
        }
        return os;
    }

}
