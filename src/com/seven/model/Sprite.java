package com.seven.model;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Represents a single sprite either in a sprite archive or file directory.
 * 
 * @author Seven
 */
public final class Sprite {      

      /**
       * The current index of this sprite.
       */
      private int index = 0; 
      
      /**
       * The array of pixels that make up this image.
       */
      private byte[] data;

      /**
       * The name of this image.
       */
      private String name = "None";
      
      /**
       * The off set x position.
       */
      private int drawOffsetX = 0;
      
      /**
       * The off set y position.
       */
      private int drawOffsetY = 0;
      
      /**
       * Creates a new {@link Sprite} from a sprite archive.
       */
      public Sprite() {
            
      }
      
      /**
       * Creates a new {@link Sprite} from a file directory.
       *    
       * @param data
       *    The array of pixels that make up the image.    
       *    
       */
      public Sprite(byte[] data) {
            this.data = data;
      }
      
      /**
       * Decodes a {@link Sprite} from a given sprite archive.
       * 
       * @param idx
       *    The idx file associated with the archive.
       *    
       * @param dat
       *    The dat file associated with the archive.
       */
      public void decode(DataInputStream idx, DataInputStream dat) throws IOException {            
            while (true) {
                  
                  byte opcode = dat.readByte();
                  
                  if (opcode == 0) {
                        return;
                  }

                  if (opcode == 1) {
                        this.index = dat.readShort();
                  } else if (opcode == 2) {
                        this.name = dat.readUTF();
                  } else if (opcode == 3) {
                        this.drawOffsetX = dat.readShort();
                  } else if (opcode == 4) {
                        this.drawOffsetY = dat.readShort();
                  } else if (opcode == 5) {
                        int indexLength = idx.readInt();
                        byte[] dataread = new byte[indexLength];
                        dat.readFully(dataread);
                        this.data = dataread;
                  }
            }
      }

      /**
       * Gets the array of pixels that make up this sprite.
       * 
       * @return The data or pixels.
       */
      public byte[] getData() {
         return this.data;
      }
      
      public void setData(byte[] data) {
    	  this.data = data;
      }
      
      public void setIndex(int index) {
    	  this.index = index;
      }

      /**
       * The index of this sprite.
       * 
       * @return The index.
       */
      public int getIndex() {
         return this.index;
      }
      
      /**
       * Gets the name of this sprite.
       * 
       * @return The name.
       */
      public String getName() {
            return this.name;
      }

      /**
       * Sets the name of this sprite.
       * 
       * @param name
       *    The name to set.
       */
      public void setName(String name) {
            this.name = name;
      }

      /**
       * Gets the sprite draw offset x position.
       * 
       * @return The sprite x off set position.
       */
      public int getDrawOffsetX() {
            return this.drawOffsetX;
      }

      /**
       * Sets the draw x offset position of this sprite.
       * 
       * @param drawOffsetX
       *    The value to set.
       */
      public void setDrawOffsetX(int drawOffsetX) {
            this.drawOffsetX = drawOffsetX;
      }

      /**
       * Gets the sprite draw offset y position.
       * 
       * @return The sprite y off set position.
       */
      public int getDrawOffsetY() {
            return this.drawOffsetY;
      }

      /**
       * Sets the draw y offset position of this sprite.
       * 
       * @param drawOffsetY
       *    The value to set.
       */
      public void setDrawOffsetY(int drawOffsetY) {
            this.drawOffsetY = drawOffsetY;
      }
      
   }
