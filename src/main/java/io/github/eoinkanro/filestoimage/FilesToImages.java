package io.github.eoinkanro.filestoimage;

import io.github.eoinkanro.filestoimage.conf.CommandLineArgumentsHolder;
import io.github.eoinkanro.filestoimage.transformer.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilesToImages implements CommandLineRunner {

    @Autowired
    private CommandLineArgumentsHolder commandLineArgumentsHolder;

    @Autowired
    private Transformer[] transformers;

    public static void main(String[] args) {
        SpringApplication.run(FilesToImages.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!commandLineArgumentsHolder.init(args)) {
            return;
        }

        for (Transformer transformer : transformers) {
            transformer.transform();
        }
    }
}