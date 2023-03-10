package io.github.eoinkanro.filestoimages.transformer;

import io.github.eoinkanro.filestoimages.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestoimages.utils.BytesUtils;
import io.github.eoinkanro.filestoimages.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class Transformer {

    protected static final String COMMON_EXCEPTION_DESCRIPTION = "Something went wrong";

    @Autowired
    protected InputCLIArgumentsHolder inputCLIArgumentsHolder;
    @Autowired
    protected FileUtils fileUtils;
    @Autowired
    protected BytesUtils bytesUtils;

    public abstract void transform();

}
