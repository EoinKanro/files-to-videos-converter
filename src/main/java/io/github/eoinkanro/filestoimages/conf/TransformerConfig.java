package io.github.eoinkanro.filestoimages.conf;

import io.github.eoinkanro.filestoimages.transformer.FilesToImagesTransformer;
import io.github.eoinkanro.filestoimages.transformer.ImagesToFilesTransformer;
import io.github.eoinkanro.filestoimages.transformer.ImagesToVideosTransformer;
import io.github.eoinkanro.filestoimages.transformer.VideosToImagesTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.eoinkanro.filestoimages.conf.InputCLIArguments.*;

@Configuration
public class TransformerConfig {

    @Bean
    public FilesToImagesTransformer filesToImagesTransformerBean() {
        return new FilesToImagesTransformer(FILES_TO_IMAGES, FILES_PATH);
    }

    @Bean
    public ImagesToVideosTransformer imagesToVideosTransformerBean() {
        return new ImagesToVideosTransformer(IMAGES_TO_VIDEOS, IMAGES_PATH);
    }

    @Bean
    public VideosToImagesTransformer videosToImagesTransformerBean() {
        return new VideosToImagesTransformer(VIDEOS_TO_IMAGES, VIDEOS_PATH);
    }

    @Bean
    public ImagesToFilesTransformer imagesToFilesTransformerBean() {
        return new ImagesToFilesTransformer(IMAGES_TO_FILES, IMAGES_PATH);
    }
}
