package io.github.eoinkanro.filestovideosconverter.utils.concurrent;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Phaser;

@Log4j2
public abstract class TransformerTask implements Runnable {

    @Setter(AccessLevel.PACKAGE)
    private Phaser phaser;

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            throw new TransformerTaskException("Error during task", e);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    protected abstract void process();

}
