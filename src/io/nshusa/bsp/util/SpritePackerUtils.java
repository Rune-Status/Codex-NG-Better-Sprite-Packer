package io.nshusa.bsp.util;

import io.nshusa.rsam.util.ColorQuantizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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

    public static void calculateNextArchive(File bust) {

        final File parentBust = bust.getParentFile();

        if (parentBust == null) {
            System.out.println("parentBust is null");
            return;
        }

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
            Files.move(fileToMove.toPath(), target.toPath());
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

            final BufferedImage bImage = ColorQuantizer.quantize(ImageIO.read(imageFile));

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
