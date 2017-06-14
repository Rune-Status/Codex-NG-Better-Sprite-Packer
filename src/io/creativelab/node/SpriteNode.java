package io.creativelab;

import java.awt.image.BufferedImage;

public final class SpriteNode extends Node {
	
	private String name;
	
	private int offsetX;
	
	private int offsetY;
	
	private BufferedImage bImage;

	public SpriteNode(int id, String dispayName) {
		super(id, dispayName);
	}

	public String getName() {
		return name;
	}

	public SpriteNode setName(String name) {
		this.name = name;
		return this;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public SpriteNode setOffsetX(int offsetX) {
		this.offsetX = offsetX;
		return this;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public SpriteNode setOffsetY(int offsetY) {
		this.offsetY = offsetY;
		return this;
	}

	public BufferedImage getbImage() {
		return bImage;
	}

	public SpriteNode setbImage(BufferedImage bImage) {
		this.bImage = bImage;
		return this;
	}

}
