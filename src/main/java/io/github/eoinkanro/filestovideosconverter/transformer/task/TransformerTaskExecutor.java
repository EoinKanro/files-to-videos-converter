package io.github.eoinkanro.filestovideosconverter.transformer.task;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.THREADS;

@Component
public class TransformerTaskExecutor {

    private static final long AWAIT_MS = 1000;

    @Autowired
    private InputCLIArgumentsHolder inputCLIArgumentsHolder;

    private HandledRunnableExecutorService executorService;

    @Getter
    private Phaser phaser;

    public void init() {
        phaser = new Phaser(1);
        int threads = inputCLIArgumentsHolder.getArgument(THREADS);
        executorService = new HandledRunnableExecutorService(threads, threads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public void submitTask(TransformerTask task) {
        task.setPhaser(phaser);

        phaser.register();
        executorService.submit(task);
    }

    public void awaitExecutor() {
        do {
            try {
                phaser.awaitAdvanceInterruptibly(phaser.arrive(), AWAIT_MS, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                //do nothing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (executorService.getCaughtThrowable() != null) {
                throw new TransformerTaskException("Error during task", executorService.getCaughtThrowable());
            }
        } while (phaser.getRegisteredParties() > 1);
    }

    public void shutdown() {
        executorService.shutdown();
    }


}
