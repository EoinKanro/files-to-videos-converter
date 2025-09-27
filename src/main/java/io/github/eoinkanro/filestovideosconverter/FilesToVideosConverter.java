package io.github.eoinkanro.filestovideosconverter;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestovideosconverter.transformer.Transformer;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTaskExecutor;
import io.github.eoinkanro.filestovideosconverter.transformer.task.impl.FilesToVideosTransformerTask;
import io.github.eoinkanro.filestovideosconverter.transformer.task.impl.VideosToFilesTransformerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilesToVideosConverter implements CommandLineRunner {

    @Autowired
    private InputCLIArgumentsHolder inputCLIArgumentsHolder;
    @Autowired
    private TransformerTaskExecutor transformerTaskExecutor;

    @Autowired
    private Transformer<FilesToVideosTransformerTask> filesToVideosTransformer;
    @Autowired
    private Transformer<VideosToFilesTransformerTask> videosToFilesTransformer;

    public static void main(String[] args) {
        SpringApplication.run(FilesToVideosConverter.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!inputCLIArgumentsHolder.init(args)) {
            return;
        }

        transformerTaskExecutor.init();
        try {
            runTransformers();
        } finally {
            transformerTaskExecutor.shutdown();
        }
    }

    private void runTransformers() {
        filesToVideosTransformer.transform();
        videosToFilesTransformer.transform();
    }
}