package io.github.eoinkanro.filestovideosconverter.utils;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestovideosconverter.transformer.TransformerTask;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.THREADS;

@Component
public class TransformerTaskExecutor {

    @Autowired
    private InputCLIArgumentsHolder inputCLIArgumentsHolder;

    private ExecutorService executorService;

    @Getter
    private Phaser phaser;

    public void init() {
        phaser = new Phaser(1);
        executorService = Executors.newFixedThreadPool(inputCLIArgumentsHolder.getArgument(THREADS));
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void submitTask(TransformerTask task) {
        phaser.register();
        executorService.submit(task);
    }

    public void awaitExecutor() {
        phaser.arriveAndAwaitAdvance();
    }

}
