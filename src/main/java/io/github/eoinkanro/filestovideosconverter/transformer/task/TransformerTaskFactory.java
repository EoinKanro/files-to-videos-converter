package io.github.eoinkanro.filestovideosconverter.transformer.task;


import java.io.File;

public interface TransformerTaskFactory<T extends TransformerTask> {

    T createModel(File processData);

}
