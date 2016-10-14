package com.vultr.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;

public final class ImageUtils {
	
	private ImageUtils() {
		
	}
	
	public static BufferedImage convert(BufferedImage src, int bufImgType) {
	    BufferedImage img= new BufferedImage(src.getWidth(), src.getHeight(), bufImgType);
	    Graphics2D g2d= img.createGraphics();
	    g2d.drawImage(src, 0, 0, null);
	    g2d.dispose();
	    return img;
	}
	
    public static BufferedImage makeColorTransparent(BufferedImage im, final Color color) {
    	ImageFilter filter = new RGBImageFilter() {

    		public int markerRGB = color.getRGB() | 0xFF000000;

    		public final int filterRGB(int x, int y, int rgb) {
    			if ((rgb | 0xFF000000) == markerRGB) {
    				return 0x00FFFFFF & rgb;
    			} else {
    				return rgb;
    			}
    		}
    	};

    	return ImageUtils.imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(im.getSource(), filter)));
    }
    
    public static javafx.scene.paint.Color swingToFXColor(java.awt.Color color) {
    	int r = color.getRed();
    	int g = color.getGreen();
    	int b = color.getBlue();
    	int a = color.getAlpha();
    	double opacity = a / 255.0 ;
    	
    	return javafx.scene.paint.Color.rgb(r, g, b, opacity);
    }
    
    public static BufferedImage createColoredBackground(BufferedImage image, java.awt.Color color) {
    	BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g2d = copy.createGraphics();
    	g2d.setColor(color); // Or what ever fill color you want...
    	g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
    	g2d.drawImage(image, 0, 0, null);
    	g2d.dispose();    	
    	return copy;
    }
    
    public static java.awt.Color fxColorToAWTColor(javafx.scene.paint.Color fxColor) {
    	return new java.awt.Color((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue(), (float) fxColor.getOpacity());
    }

    public static BufferedImage imageToBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

}
