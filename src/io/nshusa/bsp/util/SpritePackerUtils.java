package io.nshusa.bsp.util;

import io.nshusa.rsam.util.ColorQuantizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class SpritePackerUtils {
	
	private SpritePackerUtils() {
		
	}

    public static String getPrefix(File input) {
        return input.getName().substring(0, input.getName().lastIndexOf(".") != -1 ? input.getName().lastIndexOf(".") : input.getName().length());
    }

    public static String getExtension(File input) {
        return input.getName().substring(input.getName().lastIndexOf(".") != -1 ? input.getName().lastIndexOf(".") : input.getName().length(), input.getName().length());
    }

    public static BufferedImage draw(BufferedImage src, Color trans) {
        BufferedImage fill = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics2D g = fill.createGraphics();

        g.setPaint (trans);
        g.fillRect ( 0, 0, fill.getWidth(), fill.getHeight() );
        g.drawImage(src, null, 0, 0);
        return fill;
    }

    private static BufferedImage createPlaceholder() {
        BufferedImage bImage = new BufferedImage(16, 16, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = bImage.createGraphics();

        graphics.setPaint (Color.BLACK);
        graphics.fillRect ( 0, 0, bImage.getWidth(), bImage.getHeight() );
        return bImage;
    }

    public static boolean moveFile(File fileToMove) {
	    try {
            File imageArchiveDir = fileToMove.getParentFile();

            if (imageArchiveDir == null) {
                return false;
            }

            File mediaDir = imageArchiveDir.getParentFile();

            if (mediaDir == null) {
                return false;
            }

            File outputRootDir = new File(mediaDir.getParentFile(), mediaDir.getName() + "_output");

            if (!outputRootDir.exists()) {
                outputRootDir.mkdirs();
            }

            File outputDir = new File(outputRootDir, imageArchiveDir.getName());

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File target = new File(outputDir, fileToMove.getName());

            Files.move(fileToMove.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception ex) {
	        ex.printStackTrace();
	        return false;
        }
    }

    public static Optional<File> calculateNextDirectory(File root, String prefix , int did) {
        int count = 0;

        if (did != -1) {
            prefix = prefix.substring(0, prefix.lastIndexOf(Integer.toString(did)));
        } else {
            did = 1;
        }

        for (;;) {
            File next = new File(root, prefix + (did + count));

            if (next.exists()) {

                File[] imageFiles = next.listFiles();

                sortImages(imageFiles);

                try {
                    Optional<File> result = validateArchiveColorLimit(imageFiles);

                    if (!result.isPresent()) {
                        return Optional.of(next);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                return Optional.of(next);
            }

            count++;
        }
    }



    public static Optional<File> validateArchiveColorLimit(File[] imageFiles) throws IOException {
        final Set<Integer> set = new HashSet<>();

        for (File imageFile : imageFiles) {

            if (!imageFile.exists() || imageFile.isDirectory()) {
                continue;
            }

            final BufferedImage bImage = ImageIO.read(imageFile);

            for (int x = 0; x < bImage.getWidth(); x++) {
                for (int y = 0; y < bImage.getHeight(); y++) {
                    set.add(bImage.getRGB(x, y));

                    if (set.size() > 256) {
                        System.out.println(String.format("image=%s exceeds archive limit of 256 colors", imageFile.getParentFile().getName() + "/" + imageFile.getName()));
                        return Optional.of(imageFile);
                    }
                }
            }

        }

        return Optional.empty();
    }

	public static boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

    private static BufferedImage imageToBufferedImage(Image image) {

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return bufferedImage;

    }

    public static BufferedImage setColorTransparent(BufferedImage im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {

            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return imageToBufferedImage(Toolkit.getDefaultToolkit().createImage(ip));
    }

	public static BufferedImage convertToGIF(BufferedImage bimage) throws IOException {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ImageIO.write(bimage, "gif", bos);
	    try(InputStream is = new ByteArrayInputStream(bos.toByteArray())) {
	        return ImageIO.read(is);
        }
    }

	public static boolean isValidImage(File file) {
        return file.getName().endsWith(".png") || file.getName().endsWith("gif");
    }

    public static void sortImages(File[] imageFiles) {
        Arrays.sort(imageFiles, (first, second) -> {
            final String ffid = first.getName().substring(0, first.getName().lastIndexOf(".") != -1 ? first.getName().lastIndexOf(".") : first.getName().length());
            final String sfid = second.getName().substring(0, second.getName().lastIndexOf(".") != -1 ? second.getName().lastIndexOf(".") : second.getName().length());

            int fid = -1;
            try {
                fid = Integer.parseInt(ffid);
            } catch (Exception ex) {

            }

            int sid = -1;

            try {
                sid = Integer.parseInt(sfid);
            } catch (Exception ex) {

            }

            return Integer.compare(fid, sid);
        });
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
                  Dialogue.showException("Failed to open URL.", ex).showAndWait();
            }
      }

}
