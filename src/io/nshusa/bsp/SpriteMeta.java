package io.nshusa.bsp;

public class SpriteMeta {

    private int x;

    private int y;

    private int format;

    public SpriteMeta(int x, int y, int format) {
        this.x = x;
        this.y = y;
        this.format = format;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int type) {
        this.format = type;
    }

    @Override
    public String toString() {
        return x + ":" + y + ":" + format;
    }

}
