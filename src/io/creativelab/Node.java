package io.creativelab;

public final class Node {
	
	private String name;
	
	private String spriteName;
	
	private int drawOffsetX;
	
	private int drawOffsetY;
	
	public Node(String name) {
		this.name = name;
	}

	public Node copy() {
		Node node = new Node(name);
		node.spriteName = spriteName;
		node.drawOffsetX = drawOffsetX;
		node.drawOffsetY = drawOffsetY;
		return node;
	}
	
	public String getName() {
		return name;
	}

	public Node setName(String name) {
		this.name = name;
		return this;
	}
	
	public int getDrawOffsetX() {
		return drawOffsetX;
	}

	public Node setDrawOffsetX(int drawOffsetX) {
		this.drawOffsetX = drawOffsetX;
		return this;
	}

	public int getDrawOffsetY() {
		return drawOffsetY;
	}

	public Node setDrawOffsetY(int drawOffsetY) {
		this.drawOffsetY = drawOffsetY;
		return this;
	}	

	public String getSpriteName() {
		return spriteName;
	}

	public Node setSpriteName(String spriteName) {
		this.spriteName = spriteName;
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

}
