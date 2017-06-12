package io.creativelab.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import javax.imageio.ImageIO;

import io.creativelab.util.msg.ExceptionMessage;

public final class Misc {
	
	private Misc() {
		
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

    	return Misc.imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(im.getSource(), filter)));
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

      public static void launchURL(String url) {
            String osName = System.getProperty("os.name");
            try {
                  if (osName.startsWith("Mac OS")) {
                        Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                        Method openURL = fileMgr.getDeclaredMethod("openURL",
                                    new Class[] {String.class});
                        openURL.invoke(null, new Object[] {url});
                  } else if (osName.startsWith("Windows"))
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                  else {
                        String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla",
                                    "netscape", "safari"};
                        String browser = null;
                        for (int count = 0; count < browsers.length && browser == null; count++)
                              if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]})
                                          .waitFor() == 0)
                                    browser = browsers[count];
                        if (browser == null) {
                              throw new Exception("Could not find web browser");
                        } else
                              Runtime.getRuntime().exec(new String[] {browser, url});
                  }
            } catch (Exception ex) {
                  new ExceptionMessage("Failed to open URL.", ex);
            }
      }
      
  	public static byte[] readFile(String name) throws Exception {
		try (RandomAccessFile raf = new RandomAccessFile(name, "r")) {			
			MappedByteBuffer buffer = raf.getChannel().map(MapMode.READ_ONLY, 0L, raf.length());

			byte[] data = new byte[buffer.remaining()];

			buffer.get(data);
			
			return data;
		}
	}

	public static BufferedImage byteArrayToImage(byte[] data) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		BufferedImage image = ImageIO.read(in);
		return image;
	}

	public static int calculateLargestImageIndex(File[] files) {

		int largest = 0;

		for (int index = 0; index < files.length; index++) {

			File file = files[index];
			
			if (file == null) {
				continue;
			}

			if (!Misc.isValidImage(file.getName())) {
				continue;
			}

			final String modifiedName = file.getName().replace(file.getName().substring(file.getName().lastIndexOf(".")), "");			

			try {
				int imageIndex = Integer.parseInt(modifiedName);

				if (largest < imageIndex) {
					largest = imageIndex;
				}
			} catch (NumberFormatException ex) {
				if (index > largest) {
					largest = index;
				}
				continue;
			}
		}
		return largest;
	}
	
	public static File[] sortImages(File[] files) {
		
		File[] sorted = new File[Misc.calculateLargestImageIndex(files) + 1];
		
		for (int index = 0; index < files.length; index++) {
			
			File file = files[index];
			
			if (file == null) {
				continue;
			}
			
			if (!Misc.isValidImage(file.getName())) {
				continue;
			}			
			
			final String modifiedName = file.getName().replace(file.getName().substring(file.getName().lastIndexOf(".")), "");			
			
			try {
				int imageIndex = Integer.parseInt(modifiedName);

				sorted[imageIndex] = file;
			} catch (NumberFormatException ex) {
				sorted[Misc.findFreeIndex(sorted)] = file;
			}
			
		}
		
		return sorted;
	}
	
	public static <T> int findFreeIndex(T[] array) {
		for (int index = 0; index < array.length; index++) {
			if (array[index] == null) {
				return index;
			}
		}
		return -1;
	}

	public static boolean isValidImage(String name) {
		name = name.toLowerCase();
		if (name.endsWith(".png") || name.endsWith("gif") || name.endsWith("jpg") || name.endsWith("jpeg")) {
			return true;
		}

		return false;
	}

}
