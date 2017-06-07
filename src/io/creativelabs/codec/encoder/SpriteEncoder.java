package io.creativelabs.codec.encoder;

import java.io.DataOutputStream;
import java.io.IOException;

import io.creativelabs.node.Sprite;

/**
 * Packs sprites into a file by encoding them.
 * 
 * @author Chad Adams
 */
public final class SpriteEncoder {
	
	private SpriteEncoder() {
		
	}
	
	public static void encode(DataOutputStream dat, Sprite sprite) throws IOException {		
		if (sprite.getIndex() != -1) {
			dat.writeByte(1);
			dat.writeShort(sprite.getIndex());
		}

		if (sprite.getName() != null && !sprite.getName().equals("None")) {
			dat.writeByte(2);
			dat.writeUTF(sprite.getName());
		}
		
		if (sprite.getWidth() != 0) {
			dat.writeByte(3);
			dat.writeShort(sprite.getWidth());
		}

		if (sprite.getHeight() != 0) {
			dat.writeByte(4);
			dat.writeShort(sprite.getHeight());
		}

		if (sprite.getDrawOffsetX() != 0) {
			dat.writeByte(5);
			dat.writeShort(sprite.getDrawOffsetX());
		}

		if (sprite.getDrawOffsetY() != 0) {
			dat.writeByte(6);
			dat.writeShort(sprite.getDrawOffsetY());
		}

		if (sprite.getData() != null) {
			dat.writeByte(7);
			
			dat.writeInt(sprite.getData().length);			
			
			for(int i = 0; i < sprite.getData().length; i++) {				
				dat.writeInt(sprite.getData()[i]);
			}
			
		}
		
		dat.writeByte(0);
		
	}

}
