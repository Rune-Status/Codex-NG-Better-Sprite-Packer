package io.nshusa.bsp;

import java.awt.image.BufferedImage;

public class BufferedImageWrapper {

    private final BufferedImage bimage;

    private final int format;

    public BufferedImageWrapper(BufferedImage bimage, int format) {
        this.bimage = bimage;
        this.format = format;
    }

    public BufferedImage getBimage() {
        return bimage;
    }

    public int getFormat() {
        return format;
    }

}
