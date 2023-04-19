package io.github.eoinkanro.filestovideosconverter.transformer;

import io.github.eoinkanro.filestovideosconverter.conf.ConfigException;
import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgument;
import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTask;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTaskFactory;
import io.github.eoinkanro.filestovideosconverter.utils.FileUtils;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTaskExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

@Log4j2
@RequiredArgsConstructor
public abstract class Transformer<T extends TransformerTask> {

    private final InputCLIArgument<Boolean> activeTransformerArgument;
    private final InputCLIArgument<String> pathToFileArgument;
    private final TransformerTaskFactory<T> transformerTaskFactory;

    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private TransformerTaskExecutor transformerTaskExecutor;

    @Autowired
    protected InputCLIArgumentsHolder inputCLIArgumentsHolder;

    public final void transform() {
        if (Boolean.FALSE.equals(inputCLIArgumentsHolder.getArgument(activeTransformerArgument))) {
            return;
        }

        prepareConfiguration();
        process();
        transformerTaskExecutor.awaitExecutor();
    }

    /**
     * Prepare configuration of transformer
     * Check and prepare required params
     */
    protected void prepareConfiguration() {
        if (StringUtils.isBlank(inputCLIArgumentsHolder.getArgument(pathToFileArgument))) {
            throw new ConfigException("Target path for transforming files to images is empty");
        }

        String input = fileUtils.getAbsolutePath(inputCLIArgumentsHolder.getArgument(pathToFileArgument));
        if (!new File(input).exists()) {
            throw new ConfigException("Target path \"" + input + "\" doesn't exist");
        }
    }


    private void process() {
        File file = new File(inputCLIArgumentsHolder.getArgument(pathToFileArgument));
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            processFolder(file.listFiles());
        } else {
            transformerTaskExecutor.submitTask(transformerTaskFactory.createModel(file));
        }
    }

    /**
     * Process folder with files
     *
     * @param folder - folder
     */
    private void processFolder(File[] folder) {
        if (folder == null) {
            return;
        }

        for (File file : folder) {
            if (file.isDirectory()) {
                processFolder(file.listFiles());
            } else {
                transformerTaskExecutor.submitTask(transformerTaskFactory.createModel(file));
            }
        }
    }

}
