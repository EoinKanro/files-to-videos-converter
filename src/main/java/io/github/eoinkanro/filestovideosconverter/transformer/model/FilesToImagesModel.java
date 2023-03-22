package io.github.eoinkanro.filestovideosconverter.transformer.model;

import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

@Getter
@Setter
public class FilesToImagesModel {

    private BufferedImage bufferedImage;
    private int[] pixels;
    private int pixelIndex;
    private int sizeOfIndex;

    private int[] tempRow;
    private int tempRowIndex;

    public void incrementPixelIndex() {
        pixelIndex++;
    }

    public void incrementTempRowIndex() {
        tempRowIndex++;
    }

}
