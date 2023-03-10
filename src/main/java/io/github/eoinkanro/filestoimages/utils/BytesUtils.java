package io.github.eoinkanro.filestoimages.utils;

import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class BytesUtils {

    public static final int ONE = Color.BLACK.getRGB();
    public static final int ONE_MIN = ONE / 2 - 1;
    public static final int ZERO = Color.white.getRGB();

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
        return "0".repeat(additionalZeros) +
                bits;
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
     * @return - bit
     */
    public int pixelToBit(int pixel) {
        return pixel > ONE_MIN ? 0 : 1;
    }
}
