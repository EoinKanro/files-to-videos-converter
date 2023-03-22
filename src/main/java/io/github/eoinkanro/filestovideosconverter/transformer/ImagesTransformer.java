package io.github.eoinkanro.filestovideosconverter.transformer;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgument;

import java.io.File;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.DELETE_IMAGES_IN_PROGRESS;

public abstract class ImagesTransformer extends Transformer {

    protected boolean allIsFine = true;

    protected ImagesTransformer(InputCLIArgument<Boolean> activeTransformerArgument, InputCLIArgument<String> pathToFileArgument) {
        super(activeTransformerArgument, pathToFileArgument);
    }

    /**
     * Delete file if feature is on
     *
     * @param file - image or folder to delete
     */
    protected void deleteImages(File file) {
        transformerTaskExecutor.awaitExecutor();

        if (Boolean.TRUE.equals(inputCLIArgumentsHolder.getArgument(DELETE_IMAGES_IN_PROGRESS)) && allIsFine) {
            fileUtils.deleteFile(file);
        }
    }

}
