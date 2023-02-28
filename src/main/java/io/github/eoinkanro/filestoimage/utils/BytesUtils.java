package io.github.eoinkanro.filestoimage.utils;

import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class BytesUtils {

    public static final int ONE = Color.BLACK.getRGB();
    public static final int ZERO = Color.white.getRGB();
    public static final int SPACE = Color.GRAY.getRGB();

    /**
     * Transform byte to bits string
     * example: 00000001
     *
     * @param aByte - byte
     * @return - bits string
     */
    public String byteToBits(int aByte) {
        String bits = Integer.toBinaryString(aByte);
        int additionalZeros = 8 - bits.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < additionalZeros; i++) {
            result.append("0");
        }
        result.append(bits);
        return result.toString();
    }

    /**
     * Transform bit pixel
     *
     * @param bit - bit
     * @return - pixel
     */
    public int bitToPixel(int bit) {
        if (bit == 1) {
            return ONE;
        }
        return ZERO;
    }

    /**
     * Transform pixel to bit
     *
     * @param pixel - pixel
     * @return - bit or -1 if pixel is unknown
     */
    public int pixelToBit(int pixel) {
        //TODO for bad quality of pixel
        if (pixel == ONE) {
            return 1;
        } else if (pixel == ZERO) {
            return 0;
        }
        return -1;
    }
}
