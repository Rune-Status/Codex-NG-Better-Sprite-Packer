package io.creativelab.node;

import com.creativelab.util.SpritePackerUtils;

public final class ArchiveNode extends Node {
	
	private int hash;

	public ArchiveNode(int id, String dispayName) {
		super(id, dispayName);
		try {			
			this.hash = Integer.parseInt(displayName);
		} catch (Exception ex) {
			this.hash = SpritePackerUtils.nameToHash(displayName);
		}
	}

	public int getHash() {
		return hash;
	}
	
	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		
		try {			
			this.hash = Integer.parseInt(displayName);
		} catch (Exception ex) {
			this.hash = SpritePackerUtils.nameToHash(displayName);
		}
		
	}
	
}
