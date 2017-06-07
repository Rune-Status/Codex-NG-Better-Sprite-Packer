package io.creativelabs.node;

public final class Entry implements Comparable<Entry> {
	
	private int index;
	
	private Sprite sprite;
	
	public Entry(int index, Sprite sprite) {
		this.index = index;
		this.sprite = sprite;
	}
	
	public Entry copy() {
		return new Entry(index, sprite);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return sprite.getName().equalsIgnoreCase("Unknown") || sprite.getName().equalsIgnoreCase("None") ? Integer.toString(index) : sprite.getName();
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	@Override
	public int compareTo(Entry other) {
		if (getName().equalsIgnoreCase(other.getName())) {
			return 0;
		} else return index > other.getIndex() ? 1 : -1;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
