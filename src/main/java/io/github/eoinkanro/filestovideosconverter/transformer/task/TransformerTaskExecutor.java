package io.github.eoinkanro.filestovideosconverter.transformer.task;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.THREADS;

/**
 * It runs tasks and waits until they are finished
 */
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

    /**
     * Submit task to executor
     *
     * @param task - task to perform
     */
    public void submitTask(TransformerTask task) {
        task.setPhaser(phaser);
        executorService.submit(task);
    }

    /**
     * Wait until all tasks are finished
     * If some task is failed then it throws exception
     */
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

    /**
     * Shutdown executor
     */
    public void shutdown() {
        executorService.shutdown();
    }

}
