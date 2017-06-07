package io.creativelabs.codec.decoder;

import java.io.DataInputStream;
import java.io.IOException;

import io.creativelabs.node.Sprite;

/**
 * Decodes sprites packed into a file back into sprites.
 * 
 * @author Chad Adams
 */
public final class SpriteDecoder {
	
	private SpriteDecoder() {
		
	}
    
    public static Sprite decode(DataInputStream dat) throws IOException {
    	
    	Sprite sprite = new Sprite();
    	
          while (true) {
                
                byte opcode = dat.readByte();
                
                if (opcode == 0) {
                      return sprite;
                } else if (opcode == 1) {
                	sprite.setIndex(dat.readShort());
                } else if (opcode == 2) {
                	sprite.setName(dat.readUTF());
                } else if (opcode == 3) {
                	sprite.setWidth(dat.readShort());
                } else if (opcode == 4) {
                	sprite.setHeight(dat.readShort());                	
                } else if (opcode == 5) {
                	sprite.setDrawOffsetX(dat.readShort());
                } else if (opcode == 6) {
                	sprite.setDrawOffsetY(dat.readShort());
                } else if (opcode == 7) {
                	
                      int indexLength = dat.readInt();
                      
                      int[] pixels = new int[indexLength];                      
                      
                      for (int i = 0; i < pixels.length; i++) {
                    	  
                    	  int color = dat.readInt();
                    	  
                    	  pixels[i] = color;
                      }
                      
                      sprite.setData(pixels);
                }
          }
    }

}
