package io.github.eoinkanro.filestoimages.transformer;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Phaser;

@Log4j2
@AllArgsConstructor
public abstract class TransformerTask implements Runnable {

    private final Phaser phaser;

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            log.error("Error during work", e);
        } finally {
            phaser.arriveAndDeregister();
        }
    }

    protected abstract void process();

}
