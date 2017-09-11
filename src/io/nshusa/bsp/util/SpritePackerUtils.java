package io.nshusa.bsp.util;

import io.nshusa.rsam.util.BufferedImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

public final class SpritePackerUtils {
	
	private SpritePackerUtils() {
		
	}

    public static void markPixel(File imageFile) {

        BufferedImage bimage = null;

        try {
            bimage = convertToGIF(imageFile);
        } catch (Exception ex) {

        }

        if (bimage == null) {
            return;
        }

        int[] coords = SpritePackerUtils.getDarkestPixelCoord(bimage);



    }

    public static void createNewImageArchive(Queue<File> queue, File[] imageFiles, int imageIndex) throws IOException {

        if (imageIndex >= imageFiles.length) {
            return;
        }

        final File imageFile = imageFiles[imageIndex];

        final File parent = imageFile.getParentFile();

        if (parent == null) {
            return;
        }

        final String parentName = parent.getName().substring(0, parent.getName().lastIndexOf(".") != -1 ? parent.getName().lastIndexOf(".") : parent.getName().length());

        if (parentName.isEmpty()) {
            return;
        }

        if (parentName.charAt(0) == '-' || Character.isDigit(parentName.charAt(0))) {
            throw new IOException(String.format("Can not generate a new archive from hashed archive=%s", parentName));
        }

        int pos = 0;

        final char[] array = parentName.toCharArray();

        for (pos = 0; pos < array.length; pos++) {
            final char ch = array[pos];

            if (Character.isDigit(ch)) {
                break;
            }
        }

        String prefix = parentName.substring(0, pos);

        boolean hasAnotherArchive = pos != array.length;

        int archiveId = 0;

        if (hasAnotherArchive) {
            try {
                archiveId = Integer.parseInt(parentName.substring(pos, parentName.length()));

                System.out.println(String.format("archive id=%d", archiveId));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        final File nextImageArchive = new File(parent.getParentFile(), prefix + (archiveId + 1));

        if (!nextImageArchive.exists()) {
            nextImageArchive.mkdirs();
        }

        queue.add(nextImageArchive);

        final int end = imageFiles.length - 1;

        for (int i = imageIndex; i <= end; i++) {
            final File existing = imageFiles[i];

            final String ext = getFileExtension(existing);

            final File target = new File(nextImageArchive, (i - imageIndex) + ext);

            Files.move(existing.toPath(), target.toPath());
        }

    }

    public static String getPrefix(File input) {
        return input.getName().substring(0, input.getName().lastIndexOf(".") != -1 ? input.getName().lastIndexOf(".") : input.getName().length());
    }

    public static String getExtension(File input) {
        return input.getName().substring(input.getName().lastIndexOf(".") != -1 ? input.getName().lastIndexOf(".") : input.getName().length(), input.getName().length());
    }

    public static void calculateNextArchive(File file) {

        File parent = file.getParentFile();

        String prefix = getPrefix(parent);

        // TODO make sure the archive is not a hashed archive because we can't rebuild if it is
        if (prefix.charAt(0) == '-' || Character.isDigit(prefix.charAt(0))) {
            System.out.println(String.format("Can't rebuild archive from a hashed archive=%s", parent.getName()));
            return;
        }

        char[] array = prefix.toCharArray();

        int index;
        for (index = 0; index < array.length; index++) {
            if (Character.isDigit(array[index])) {
                break;
            }
        }

        // no digit in archive name
        if (index == array.length) {

            Optional<File> result = calculuteNextFile(parent.getParentFile(), prefix, -1);

            if (result.isPresent()) {

                //System.out.println("next archive is: " + result.get().getName() + " for " + file.getParentFile().getName());

                moveFile(parent, result.get(), file);

            }

        } else {
            int value = Integer.parseInt(prefix.substring(index, prefix.length()));

            //System.out.println("value in " + parent.getName() + " is " + value);

            Optional<File> result = calculuteNextFile(parent.getParentFile(), prefix, value);

            if (result.isPresent()) {

                //System.out.println("next archive is: " + result.get().getName() + " for " + file.getParentFile().getName());

                moveFile(parent, result.get(), file);

            }
        }

    }

    private static void moveFile(File prevDir, File nextDir, File fileToMove) {

        if (!nextDir.exists()) {
            nextDir.mkdirs();
        }

        String prefix = getPrefix(fileToMove);
        String ext = getExtension(fileToMove);

        File target = new File(nextDir, nextDir.listFiles().length + ext);

        int id = -1;

        //System.out.println("prefix: " + prefix);

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

        // rename files after this one in prev archive

        for (int i = (id + 1); i < prevDir.listFiles().length; i++) {

            File toRename = prevDir.listFiles()[i];

            String ext2 = getExtension(toRename);

            File renamed = new File(prevDir, (i - 1) + ext2);

            toRename.renameTo(renamed);

        }

    }

    public static Optional<File> calculuteNextFile(File root, String prefix , int did) {
        int count = 0;

        if (did != -1) {
            prefix = prefix.substring(0, prefix.lastIndexOf(Integer.toString(did)));
        } else {
            did = 1;
        }

        for (;;) {
            File next = new File(root, prefix + (did + count));

            if (!next.exists()) {
                return Optional.of(next);
            }

            count++;
        }
    }

    public static Optional<File> validateArchiveColorLimit(File[] imageFiles) throws IOException {
        // TODO keep crack of all of the distinct pixels

        int totalColors = 0;

        for (File imageFile : imageFiles) {

            // TODO convert to gif

            BufferedImage gif = convertToGIF(imageFile);

            // TODO count this gifs distinct pixels

            int count = getDistinctPixelCount(gif);

            if (count + totalColors > 256) {
                // System.out.println(String.format("image=%s exceeds archive limit of 256 colors", imageFile.getParentFile().getName() + "/" + imageFile.getName()));
                return Optional.of(imageFile);
            }

            totalColors += count;

            //System.out.println(String.format("image=%s has %d colors", imageFile.getParentFile().getName() + "/" + imageFile.getName(), count));

        }

        return Optional.empty();
    }

    public static int getDistinctPixelCount(BufferedImage bimage) {
        Set<Integer> set = new HashSet<>();

        for (int y = 0; y < bimage.getHeight(); y++) {
            for (int x = 0; x < bimage.getWidth(); x++) {
                set.add(bimage.getRGB(x, y));
            }
        }

        return set.size();
    }

    public static int[] getDarkestPixelCoord(BufferedImage bimage) {
        int lx = bimage.getWidth();
        int ly = bimage.getHeight();
        int sr = 255;
        int sg = 255;
        int sb = 255;
        int lrgb = -1;

        for (int y = 0; y < bimage.getWidth(); y++) {
            for (int x = 0; x < bimage.getWidth(); x++) {
                final int rgb = bimage.getRGB(x, y);

                final int r = (rgb >> 16) & 0xFF;
                final int g = (rgb >> 8) & 0xFF;
                final int b = rgb & 0xFF;

                if (x == 0 && y == 0) {
                    lrgb = rgb;
                }

                if ((r <= sr && g <= sg) || (r <= sr && b <= sb) || (g <= sg && b <= sb)) {
                    lx = x;
                    ly = y;
                    lrgb = rgb;
                    sr = r;
                    sg = g;
                    sb = b;
                }

            }
        }

        //int lr = ColorUtils.getRed(lrgb);
        //int lg = ColorUtils.getGreen(lrgb);
        //int lb = ColorUtils.getBlue(lrgb);

        //System.out.println(String.format("lowest pixel for image=%s is rgb[r=%d g=%d b=%d] s[r=%d g=%d b=%d] on x=%d y=%d", imageFile.getParentFile().getName() + "/" + imageFile.getName(), lr, lg, lb, sr, sg, sb, lx, ly));
        return new int[] {lx, ly};
    }

    public static String getFileExtension(File input) {
        return input.getName().substring(input.getName().lastIndexOf(".") != -1 ? input.getName().lastIndexOf(".") : input.getName().length(), input.getName().length());
    }

	public static boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
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
