package io.creativelabs.node;

public final class Sprite {

	private int index = 0;

	private int[] pixels;

	private String name = "None";

	private int drawOffsetX = 0;

	private int drawOffsetY = 0;

	private int width;

	private int height;

	public Sprite() {

	}

	public Sprite(int index) {
		this.index = index;
	}

	public int[] getData() {
		return this.pixels;
	}

	public void setData(int[] data) {
		this.pixels = data;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDrawOffsetX() {
		return this.drawOffsetX;
	}

	public void setDrawOffsetX(int drawOffsetX) {
		this.drawOffsetX = drawOffsetX;
	}

	public int getDrawOffsetY() {
		return this.drawOffsetY;
	}

	public void setDrawOffsetY(int drawOffsetY) {
		this.drawOffsetY = drawOffsetY;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}
