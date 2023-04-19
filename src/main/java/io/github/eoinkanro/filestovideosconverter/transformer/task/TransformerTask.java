package io.github.eoinkanro.filestovideosconverter.transformer.task;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestovideosconverter.utils.BytesUtils;
import io.github.eoinkanro.filestovideosconverter.utils.CommonUtils;
import io.github.eoinkanro.filestovideosconverter.utils.FileUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.concurrent.Phaser;

@RequiredArgsConstructor
public abstract class TransformerTask implements Runnable {

    protected static final String COMMON_EXCEPTION_DESCRIPTION = "Something went wrong";

    protected final File processData;

    @Setter(AccessLevel.PACKAGE)
    private Phaser phaser;

    @Autowired
    protected FileUtils fileUtils;
    @Autowired
    protected BytesUtils bytesUtils;
    @Autowired
    protected CommonUtils commonUtils;
    @Autowired
    protected InputCLIArgumentsHolder inputCLIArgumentsHolder;
    @Autowired
    protected TaskStatistics taskStatistics;

    @Override
    public void run() {
        try {
            phaser.register();
            process();
        } catch (Exception e) {
            throw new TransformerTaskException("Error during task", e);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    protected abstract void process();

}
