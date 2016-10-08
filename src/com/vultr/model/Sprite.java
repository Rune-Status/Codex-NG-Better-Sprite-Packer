package com.vultr.model;

/**
 * Represents a single sprite either in a sprite archive or file directory.
 * 
 * @author Vult-R
 */
public final class Sprite {      

      /**
       * The current index of this sprite.
       */
      private int index = 0; 
      
      /**
       * The array of pixels that make up this image.
       */
      private int[] pixels;      

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
      
      private int width;
      
      private int height;
      
      public Sprite() {
    	  
      }
      
      public Sprite(int index) {
    	  this.index = index;
      }

      /**
       * Gets the array of pixels that make up this sprite.
       * 
       * @return The data or pixels.
       */
      public int[] getData() {
         return this.pixels;
      }
      
      public void setData(int[] data) {
    	  this.pixels = data;
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
