package io.github.eoinkanro.filestovideosconverter.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Log4j2
public class CommandLineExecutor {

    /**
     * Execute command in command line
     *
     * @param args - arguments
     * @return - is executed fine
     */
    public boolean execute(String... args) {
        if (args.length == 0) {
            return false;
        }
        log.info("Running {}", Arrays.toString(args));

        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            builder.inheritIO();

            Process process = builder.start();

            return process.waitFor() == 0;
        } catch (InterruptedException e) {
            logException(e, args);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logException(e, args);
        }
        return false;
    }

    private void logException(Exception e, String... args) {
        log.error("Error while executing command {}", args, e);
    }

}
