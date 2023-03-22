package io.github.eoinkanro.filestovideosconverter.utils;

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
     * @param duplicateFactor - duplicate factor of pixels per bit.
     *                          example: 2, it means that one bit is a square of pixels with size 2 x 2
     * @return - bit
     */
    public int pixelToBit(int pixel, int duplicateFactor) {
        long duplicateFactorPixels = (long) duplicateFactor * duplicateFactor;
        long oneMin = duplicateFactorPixels * ONE_MIN;
        return pixel > oneMin ? 0 : 1;
    }
}
