package io.nshusa.bsp.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Misc {
	
	private Misc() {
		
	}

	public static boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	public static int getDistinctColorCount(BufferedImage bimage) {
	    final Set<Integer> set = new HashSet<>();

	    for (int y = 0; y < bimage.getHeight(); y++) {
	        for (int x = 0; x < bimage.getWidth(); x++) {
	            set.add(bimage.getRGB(x, y));
            }
        }

	    return set.size();
    }

	public static BufferedImage convertToGIF(File imageFile) throws IOException {
	    if (imageFile.getName().endsWith(".gif")) {
	        return ImageIO.read(imageFile);
        }

        try(InputStream is = new FileInputStream(imageFile)) {
            final BufferedImage bImage = ImageIO.read(is);

            final ByteArrayOutputStream bos = new ByteArrayOutputStream();

            ImageIO.write(bImage, "gif", bos);

            return ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
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

    public static File search(File dir, int id) throws IOException {
	    final File[] files = dir.listFiles();

	    final String sid = Integer.toString(id);

	    for (File file : files) {
	        String extension = file.getName().substring(file.getName().lastIndexOf(".") != -1 ? file.getName().lastIndexOf(".") : 0, file.getName().length());

	        String fileId = dir.getName().substring(0, file.getName().lastIndexOf(".") != -1 ? file.getName().lastIndexOf(".") : file.getName().length());

	        System.out.println(fileId + " " + extension);

	        if (sid.equals(fileId)) {
	            return file;
            }

        }

        throw new IOException(String.format("File not found for dir=%s id=%d", dir.getName(), id));
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
