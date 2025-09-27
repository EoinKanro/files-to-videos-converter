package io.github.eoinkanro.filestovideosconverter.conf;

import io.github.eoinkanro.filestovideosconverter.transformer.Transformer;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TaskStatistics;
import io.github.eoinkanro.filestovideosconverter.transformer.task.TransformerTaskFactory;
import io.github.eoinkanro.filestovideosconverter.transformer.task.impl.FilesToVideosTransformerTask;
import io.github.eoinkanro.filestovideosconverter.transformer.task.impl.VideosToFilesTransformerTask;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.File;

import static io.github.eoinkanro.filestovideosconverter.conf.InputCLIArguments.*;

/**
 * It configures transformer beans
 */
@Configuration
public class TransformerConfig {

    //--------------------------- Files to videos ---------------------------
    @Bean
    public Transformer<FilesToVideosTransformerTask> filesToImagesTransformerBean() {
        return new Transformer<>(FILES_TO_VIDEOS, FILES_PATH, filesToVideosTaskFactoryBean()) {
            @Override
            protected void prepareConfiguration() {
                super.prepareConfiguration();

                if (inputCLIArgumentsHolder.getArgument(IMAGE_WIDTH) % inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) > 0 ||
                        inputCLIArgumentsHolder.getArgument(IMAGE_HEIGHT) % inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) > 0) {
                    throw new ConfigException("Can't use duplicate factor " + inputCLIArgumentsHolder.getArgument(DUPLICATE_FACTOR) +
                            ". Video width and height should be divided by it without remainder");
                }
            }
        };
    }

    @Bean
    public TransformerTaskFactory<FilesToVideosTransformerTask> filesToVideosTaskFactoryBean() {
        return this::filesToVideosTransformerTaskBean;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    FilesToVideosTransformerTask filesToVideosTransformerTaskBean(File file) {
        return new FilesToVideosTransformerTask(file);
    }


    //--------------------------- Videos to Files ---------------------------
    @Bean
    public Transformer<VideosToFilesTransformerTask> imagesToFilesTransformerBean() {
        return new Transformer<>(VIDEOS_TO_FILES, VIDEOS_PATH, videosToFilesTaskFactoryBean()) {};
    }

    @Bean
    public TransformerTaskFactory<VideosToFilesTransformerTask> videosToFilesTaskFactoryBean() {
        return this::videosToFilesTransformerTaskBean;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    VideosToFilesTransformerTask videosToFilesTransformerTaskBean(File file) {
        return new VideosToFilesTransformerTask(file);
    }

    //----------------------------- Statistics -----------------------------
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TaskStatistics taskStatisticsBean() {
        return new TaskStatistics();
    }

}
