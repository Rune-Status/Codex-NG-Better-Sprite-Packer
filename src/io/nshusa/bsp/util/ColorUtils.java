package io.nshusa.bsp.util;

public class ColorUtils {

    private ColorUtils() {

    }

    public static int getRGB(int red, int green, int blue) {
        return getRGB(0xFF, red, green, blue);
    }

    public static int getRGB(int alpha, int red, int green, int blue) {
        return alpha << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF) << 0;
    }

    public static int getAlpha(int rgb) {
        return (rgb >> 24) & 0xFF;
    }

    public static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    public static int stripAlpha(int rgb) {
        return rgb & 0xFFFFFF;
    }

}
