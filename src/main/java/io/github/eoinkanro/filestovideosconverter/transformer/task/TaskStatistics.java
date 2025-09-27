package io.github.eoinkanro.filestovideosconverter.transformer.task;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * It counts number of processed frames and logs this info
 */
@Log4j2
public class TaskStatistics {

    private static final long SECOND = 1000;

    @Setter
    private String filePath;
    private long beginMillis;

    private long framesPerSecond;
    private long totalFrames;

    public void poll() {
        if (beginMillis == 0) {
            beginMillis = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - beginMillis >= SECOND) {
            logResult();
            beginMillis = System.currentTimeMillis();
            framesPerSecond = 0;
        }

        totalFrames++;
        framesPerSecond++;
    }

    public void logResult() {
        log.info("{} | FPS: {} | Total frames: {}", filePath, framesPerSecond, totalFrames);
    }

}
