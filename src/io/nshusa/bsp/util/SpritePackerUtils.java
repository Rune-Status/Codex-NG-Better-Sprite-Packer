package io.nshusa.bsp.util;

import io.nshusa.rsam.util.ColorQuantizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
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

    public static void calculateNextArchive(File[] imageFiles, File bust) {
        final File parentBust = bust.getParentFile();

        if (parentBust == null) {
            System.out.println("parentBust is null");
            return;
        }

	    try {
            BufferedImage quantized = ColorQuantizer.quantize(SpritePackerUtils.setColorTransparent(ImageIO.read(bust), Color.MAGENTA), new Color(0, 0, 1));

            Set<Integer> set = new HashSet<>();

            for (File imageFile : imageFiles) {

                if (imageFile.getCanonicalPath().equals(bust.getCanonicalPath())) {
                    continue;
                }

                BufferedImage image = ImageIO.read(imageFile);

                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getWidth(); y++) {
                        set.add(image.getRGB(x, y));
                    }
                }

            }

            for (int x = 0; x < quantized.getWidth(); x++) {
                for (int y = 0; y < quantized.getWidth(); y++) {
                    set.add(quantized.getRGB(x, y));
                }
            }

            if (set.size() <= 256) {
                //System.out.println("rewriting file: " + bust.getParentFile().getName() + "/" + bust.getName());
                ImageIO.write(SpritePackerUtils.draw(quantized, Color.MAGENTA), "png", bust);
            } else {
                final String prefix = getPrefix(parentBust);

                // make sure the archive is not a hashed archive because we can't rebuild if it is
                if (prefix.charAt(0) == '-' || Character.isDigit(prefix.charAt(0))) {
                    System.out.println(String.format("Can't rebuild archive from a hashed archive=%s", parentBust.getName()));
                    return;
                }

                final char[] array = prefix.toCharArray();

                // determine if image archive is hashed
                int index;
                for (index = 0; index < array.length; index++) {
                    if (Character.isDigit(array[index])) {
                        break;
                    }
                }

                final Optional<File> result = calculateNextDirectory(parentBust.getParentFile(), prefix, index == array.length ? -1 : Integer.parseInt(prefix.substring(index, prefix.length())));

                if (result.isPresent()) {
                    moveFile(parentBust, result.get(), bust);
                }
            }

        } catch (IOException ex) {
	        ex.printStackTrace();
        }

    }

    private static BufferedImage createPlaceholder() {
        BufferedImage bImage = new BufferedImage(16, 16, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = bImage.createGraphics();

        graphics.setPaint (Color.BLACK);
        graphics.fillRect ( 0, 0, bImage.getWidth(), bImage.getHeight() );
        return bImage;
    }

    private static void moveFile(File prevDir, File nextDir, File fileToMove) {
        if (!nextDir.exists()) {
            nextDir.mkdirs();
        }

        final String prefix = getPrefix(fileToMove);
        final String ext = getExtension(fileToMove);

        final File target = new File(nextDir, nextDir.listFiles().length + ext);

        int id = -1;

        try {
            id = Integer.parseInt(prefix);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (id == -1) {
            System.out.println(String.format("id shouldn't be -1 for %s", fileToMove.getName()));
            return;
        }

        // move file
        try {
            //System.out.println("moving file: " + fileToMove.getParentFile().getName() + "/" + fileToMove.getName());
            ImageIO.write(ColorQuantizer.quantize(SpritePackerUtils.setColorTransparent(ImageIO.read(fileToMove), Color.MAGENTA), new Color(0, 0, 1)), "png", target);
            fileToMove.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create placeholder
        BufferedImage placeholder = createPlaceholder();

        try {
            ImageIO.write(placeholder, "png", new File(prevDir, prefix + ext));
        } catch (IOException e) {
            e.printStackTrace();
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
