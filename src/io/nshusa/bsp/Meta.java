package io.nshusa.bsp;

public class Meta {

    private int id;

    private int offsetX;

    private int offsetY;

    private int resizeWidth;

    private int resizeHeight;

    private int format;

    public Meta(int id, int offsetX, int offsetY, int resizeWidth, int resizeHeight, int format) {
        this.id = id;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.resizeWidth = resizeWidth;
        this.resizeHeight = resizeHeight;
        this.format = format;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getResizeWidth() {
        return resizeWidth;
    }

    public void setResizeWidth(int resizeWidth) {
        this.resizeWidth = resizeWidth;
    }

    public int getResizeHeight() {
        return resizeHeight;
    }

    public void setResizeHeight(int resizeHeight) {
        this.resizeHeight = resizeHeight;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return String.format("id=%d offsetX=%d offsetY=%d resizeX=%d resizeY=%d format=%d", id, offsetX, offsetY, resizeWidth, resizeHeight, format);
    }

}
