package io.github.eoinkanro.filestovideosconverter.transformer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilesToImagesModel {

    private int[] pixels;
    private int pixelIndex;
    private int sizeOfIndex;
    private int lastZeroBytesCount;

    private int[] tempRow;
    private int tempRowIndex;

    public void incrementPixelIndex() {
        pixelIndex++;
    }

    public void incrementTempRowIndex() {
        tempRowIndex++;
    }

}
