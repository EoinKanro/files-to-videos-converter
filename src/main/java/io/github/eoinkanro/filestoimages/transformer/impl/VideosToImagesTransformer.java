package io.github.eoinkanro.filestoimages.transformer.impl;

import io.github.eoinkanro.filestoimages.conf.InputCLIArgument;
import io.github.eoinkanro.filestoimages.transformer.TransformException;
import io.github.eoinkanro.filestoimages.transformer.Transformer;
import io.github.eoinkanro.filestoimages.utils.CommandLineExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

import static io.github.eoinkanro.filestoimages.conf.InputCLIArguments.*;
import static io.github.eoinkanro.filestoimages.conf.OutputCLIArguments.*;

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

    private void processFile(File file) {
        try {
            log.info("Processing {}...", file);
            String imagesPattern = fileUtils.getResultFilePatternForVideosToImages(file, fileUtils.getResultPathForVideos());
            boolean isWritten = commandLineExecutor.execute(
                    FFMPEG.getValue(),
                    DEFAULT_YES.getValue(),
                    INPUT.getValue(),
                    BRACKETS_PATTERN.formatValue(file.getAbsolutePath()),
                    BRACKETS_PATTERN.formatValue(imagesPattern),
                    HIDE_BANNER.getValue()
            );

            if (!isWritten) {
                log.error("Error while writing {}", imagesPattern);
            }
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }
}
