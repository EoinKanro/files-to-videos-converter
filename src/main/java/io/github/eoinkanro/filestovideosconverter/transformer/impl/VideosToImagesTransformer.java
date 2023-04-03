package io.github.eoinkanro.filestovideosconverter.transformer.impl;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgument;
import io.github.eoinkanro.filestovideosconverter.transformer.TransformException;
import io.github.eoinkanro.filestovideosconverter.transformer.Transformer;
import io.github.eoinkanro.filestovideosconverter.utils.CommandLineExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.*;
import static io.github.eoinkanro.filestovideosconverter.conf.OutputCLIArguments.*;

@Log4j2
public class VideosToImagesTransformer extends Transformer {

    @Autowired
    private CommandLineExecutor commandLineExecutor;

    public VideosToImagesTransformer(InputCLIArgument<Boolean> activeTransformerArgument, InputCLIArgument<String> pathToFileArgument) {
        super(activeTransformerArgument, pathToFileArgument);
    }

    @Override
    protected void process() {
        File file = new File(inputCLIArgumentsHolder.getArgument(VIDEOS_PATH));
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            processFolder(file.listFiles());
        } else {
            processFile(file);
        }
    }

    private void processFolder(File[] files) {
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processFolder(file.listFiles());
            } else {
                processFile(file);
            }
        }
    }

    private void processFile(File videoFile) {
        try {
            log.info("Processing {}...", videoFile);
            String imagesPattern = fileUtils.getFFmpegVideosToImagesPattern(videoFile, fileUtils.getResultPathForVideos());
            boolean isWritten = commandLineExecutor.execute(
                    getFFmpegArgumentsBasedOnOS(fileUtils.getAbsolutePath(videoFile.getAbsolutePath()), imagesPattern)
            );

            if (!isWritten) {
                throw new TransformException("Error while writing " + imagesPattern);
            }
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }

    private String[] getFFmpegArgumentsBasedOnOS(String videoFilePath, String imagesPattern) {
        String ffmpeg;
        if (commonUtils.isWindows()) {
            ffmpeg = FFMPEG_EXE.getValue();
            videoFilePath = BRACKETS_PATTERN.formatValue(videoFilePath);
            imagesPattern = BRACKETS_PATTERN.formatValue(imagesPattern);
        } else {
            ffmpeg = FFMPEG.getValue();
        }

        return getFFmpegArguments(ffmpeg, videoFilePath, imagesPattern);
    }

    private String[] getFFmpegArguments(String ffmpeg, String videoFilePath, String imagesPattern) {
        return new String[] {
                ffmpeg,
                DEFAULT_YES.getValue(),
                INPUT.getValue(),
                videoFilePath,
                imagesPattern,
                HIDE_BANNER.getValue()
        };
    }

}
