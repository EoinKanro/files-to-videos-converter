package io.github.eoinkanro.filestovideosconverter.transformer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagesToFilesModel {

    private String currentOriginalFile;
    private String currentResultFile;

    private StringBuilder byteBuilder;
    private long zeroBytesCount;

    private int[] pixels;
    private int pixelsLastIndex;

    public void incrementZeroBytesCount() {
        zeroBytesCount++;
    }

}
