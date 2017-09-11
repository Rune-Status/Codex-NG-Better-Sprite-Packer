package io.nshusa.bsp;

public class Meta {

    private int id;

    private int offsetX;

    private int offsetY;

    private int resizeX;

    private int resizeY;

    private int format;

    public Meta(int id, int offsetX, int offsetY, int resizeX, int resizeY, int format) {
        this.id = id;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.resizeX = resizeX;
        this.resizeY = resizeY;
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

    public int getResizeX() {
        return resizeX;
    }

    public void setResizeX(int resizeX) {
        this.resizeX = resizeX;
    }

    public int getResizeY() {
        return resizeY;
    }

    public void setResizeY(int resizeY) {
        this.resizeY = resizeY;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return String.format("id=%d offsetX=%d offsetY=%d resizeX=%d resizeY=%d format=%d", id, offsetX, offsetY, resizeX, resizeY, format);
    }

}
