package io.github.eoinkanro.filestoimages.transformer;

import io.github.eoinkanro.filestoimages.conf.InputCLIArgument;
import io.github.eoinkanro.filestoimages.conf.InputCLIArguments;
import io.github.eoinkanro.filestoimages.utils.CommandLineExecutor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;

import static io.github.eoinkanro.filestoimages.conf.OutputCLIArguments.*;

@Log4j2
public class ImagesToVideosTransformer extends Transformer {

    @Autowired
    private CommandLineExecutor commandLineExecutor;

    public ImagesToVideosTransformer(InputCLIArgument<Boolean> activeTransformerArgument) {
        super(activeTransformerArgument);
    }

    @Override
    protected void process() {
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
    }

    private void processFile(File exampleImage) {
        try {
            File resultFile = fileUtils.getResultFileForImagesToVideos(exampleImage, fileUtils.getResultPathForImages());
            log.info("Writing {}...", resultFile);

            int indexSize = fileUtils.getImageIndexSize(exampleImage.getAbsolutePath());
            String findPattern = fileUtils.getFFmpegImagesPattern(exampleImage.getAbsolutePath());

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
                    MOV_FLAGS.getValue(),
                    FAST_START.getValue(),
                    CRF.getValue(),
                    CRF_18.getValue(),
                    PIXEL_FORMAT.getValue(),
                    GRAY.getValue(),
                    PRESET.getValue(),
                    SLOW.getValue(),
                    BRACKETS_PATTERN.formatValue(resultFile.getAbsolutePath()));

            if (!isWritten) {
                log.error("Error while writing {}", resultFile);
            }
        } catch (Exception e) {
            throw new TransformException(COMMON_EXCEPTION_DESCRIPTION, e);
        }
    }

}
