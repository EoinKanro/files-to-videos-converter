package io.github.eoinkanro.filestovideosconverter;

import io.github.eoinkanro.filestovideosconverter.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestovideosconverter.transformer.impl.FilesToImagesTransformer;
import io.github.eoinkanro.filestovideosconverter.transformer.impl.ImagesToFilesTransformer;
import io.github.eoinkanro.filestovideosconverter.transformer.impl.ImagesToVideosTransformer;
import io.github.eoinkanro.filestovideosconverter.transformer.impl.VideosToImagesTransformer;
import io.github.eoinkanro.filestovideosconverter.utils.TransformerTaskExecutor;
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
    private FilesToImagesTransformer filesToImagesTransformer;
    @Autowired
    private ImagesToVideosTransformer imagesToVideosTransformer;

    @Autowired
    private VideosToImagesTransformer videosToImagesTransformer;
    @Autowired
    private ImagesToFilesTransformer imagesToFilesTransformer;

    public static void main(String[] args) {
        SpringApplication.run(FilesToVideosConverter.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!inputCLIArgumentsHolder.init(args)) {
            return;
        }

        transformerTaskExecutor.init();
        runTransformers();
        transformerTaskExecutor.shutdown();
    }

    private void runTransformers() {
        filesToImagesTransformer.transform();
        imagesToVideosTransformer.transform();

        videosToImagesTransformer.transform();
        imagesToFilesTransformer.transform();
    }
}