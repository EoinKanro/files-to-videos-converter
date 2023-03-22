package io.github.eoinkanro.filestoimages;

import io.github.eoinkanro.filestoimages.conf.InputCLIArgumentsHolder;
import io.github.eoinkanro.filestoimages.transformer.impl.FilesToImagesTransformer;
import io.github.eoinkanro.filestoimages.transformer.impl.ImagesToFilesTransformer;
import io.github.eoinkanro.filestoimages.transformer.impl.ImagesToVideosTransformer;
import io.github.eoinkanro.filestoimages.transformer.impl.VideosToImagesTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilesToImages implements CommandLineRunner {

    @Autowired
    private InputCLIArgumentsHolder inputCLIArgumentsHolder;

    @Autowired
    private FilesToImagesTransformer filesToImagesTransformer;
    @Autowired
    private ImagesToVideosTransformer imagesToVideosTransformer;

    @Autowired
    private VideosToImagesTransformer videosToImagesTransformer;
    @Autowired
    private ImagesToFilesTransformer imagesToFilesTransformer;

    public static void main(String[] args) {
        SpringApplication.run(FilesToImages.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (!inputCLIArgumentsHolder.init(args)) {
            return;
        }

        //TODO Multi-thread, stack with continue() error()
        filesToImagesTransformer.transform();
        imagesToVideosTransformer.transform();

        videosToImagesTransformer.transform();
        imagesToFilesTransformer.transform();
    }
}