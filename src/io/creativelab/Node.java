package io.creativelab;

public class Node implements Comparable<Node> {
	
	protected int id;
	
	protected String displayName;
	
	public Node(int id, String dispayName) {
		this.id = id;
		this.displayName = dispayName;
	}

	public Node copy() {
		Node node = new Node(id, displayName);
		return node;
	}
	
	public boolean isSpriteNode() {
		return this instanceof SpriteNode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}

	@Override
	public int compareTo(Node o) {
		return id > o.getId() ? 1 : id < o.getId() ? -1 : 0;
	}

}
