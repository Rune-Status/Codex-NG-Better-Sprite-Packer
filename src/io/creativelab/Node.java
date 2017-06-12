package io.creativelab;

import com.creativelab.sprite.SpriteBase;

public final class Node implements Comparable<Node> {
	
	private int id;	
	
	private SpriteBase sprite;
	
	public Node(int id, SpriteBase sprite) {		
		this.id = id;
		this.sprite = sprite;
	}
	
	public Node copy() {
		return new Node(id, sprite);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {		
		this.id = id;
	}

	public String getName() {
		return sprite.getName().equalsIgnoreCase("Unknown") || sprite.getName().equalsIgnoreCase("None") ? Integer.toString(id) : sprite.getName();
	}

	public SpriteBase getSprite() {
		return sprite;
	}

	public void setSprite(SpriteBase sprite) {
		this.sprite = sprite;
	}

	@Override
	public int compareTo(Node other) {
		if (getName().equalsIgnoreCase(other.getName())) {
			return 0;
		} else return id > other.getId() ? 1 : -1;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
