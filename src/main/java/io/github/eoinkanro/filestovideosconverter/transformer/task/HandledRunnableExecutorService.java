package io.github.eoinkanro.filestovideosconverter.transformer.task;

import lombok.Getter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HandledRunnableExecutorService extends ThreadPoolExecutor {

    @Getter
    private Throwable caughtThrowable;

    public HandledRunnableExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
            caughtThrowable = t;
            return;
        }

        if (r instanceof Future<?>) {
            try {
                ((Future<?>) r).get();

            } catch (InterruptedException e) {
                caughtThrowable = e;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                caughtThrowable = e;
            }
        }
    }

}
