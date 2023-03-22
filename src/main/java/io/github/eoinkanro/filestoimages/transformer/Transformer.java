package io.github.eoinkanro.filestoimages.transformer;

import io.github.eoinkanro.filestoimages.conf.InputCLIArgument;
import io.github.eoinkanro.filestoimages.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestoimages.utils.BytesUtils;
import io.github.eoinkanro.filestoimages.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
public abstract class Transformer {

    protected static final String COMMON_EXCEPTION_DESCRIPTION = "Something went wrong";

    private final InputCLIArgument<Boolean> activeTransformerArgument;

    @Autowired
    protected InputCLIArgumentsHolder inputCLIArgumentsHolder;
    @Autowired
    protected FileUtils fileUtils;
    @Autowired
    protected BytesUtils bytesUtils;

    public final void transform() {
        if (Boolean.FALSE.equals(inputCLIArgumentsHolder.getArgument(activeTransformerArgument))) {
            return;
        }
        checkConfiguration();
        process();
    }

    protected abstract void process();

    protected void checkConfiguration() {
        //override
    }

}
