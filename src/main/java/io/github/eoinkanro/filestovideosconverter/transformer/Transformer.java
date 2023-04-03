package io.github.eoinkanro.filestovideosconverter.transformer;

import io.github.eoinkanro.filestovideosconverter.conf.ConfigException;
import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgument;
import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestovideosconverter.utils.BytesUtils;
import io.github.eoinkanro.filestovideosconverter.utils.CommonUtils;
import io.github.eoinkanro.filestovideosconverter.utils.FileUtils;
import io.github.eoinkanro.filestovideosconverter.utils.concurrent.TransformerTaskExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

@Log4j2
@RequiredArgsConstructor
public abstract class Transformer {

    protected static final String COMMON_EXCEPTION_DESCRIPTION = "Something went wrong";

    private final InputCLIArgument<Boolean> activeTransformerArgument;
    protected final InputCLIArgument<String> pathToFileArgument;

    @Autowired
    protected InputCLIArgumentsHolder inputCLIArgumentsHolder;
    @Autowired
    protected FileUtils fileUtils;
    @Autowired
    protected BytesUtils bytesUtils;
    @Autowired
    protected CommonUtils commonUtils;
    @Autowired
    protected TransformerTaskExecutor transformerTaskExecutor;

    public final void transform() {
        if (Boolean.FALSE.equals(inputCLIArgumentsHolder.getArgument(activeTransformerArgument))) {
            return;
        }

        prepareConfiguration();
        process();
        transformerTaskExecutor.awaitExecutor();
    }

    protected abstract void process();

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

}
