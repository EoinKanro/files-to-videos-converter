package io.github.eoinkanro.filestoimages.transformer;

import io.github.eoinkanro.filestoimages.conf.InputCLIArgument;

import java.io.File;

import static io.github.eoinkanro.filestoimages.conf.InputCLIArguments.DELETE_IMAGES_IN_PROGRESS;

public abstract class ImagesTransformer extends Transformer {

    protected boolean allIsFine = true;

    public ImagesTransformer(InputCLIArgument<Boolean> activeTransformerArgument, InputCLIArgument<String> pathToFileArgument) {
        super(activeTransformerArgument, pathToFileArgument);
    }

    /**
     * Delete file if feature is on
     *
     * @param file - image or folder to delete
     */
    protected void deleteImages(File file) {
        if (Boolean.TRUE.equals(inputCLIArgumentsHolder.getArgument(DELETE_IMAGES_IN_PROGRESS)) && allIsFine) {
            fileUtils.deleteFile(file);
        }
    }

}
