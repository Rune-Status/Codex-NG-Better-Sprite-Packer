package seven.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import seven.controller.MainController;
import seven.sprite.Sprite;

/**
 * The class that contains operations for files.
 * 
 * @author Seven
 */
public final class FileUtil {

      /**
       * The number of sprites found in a sprite archive.
       */
      public static int totalArchivedSprites;
      
      /**
       * The number of sprites found in a file directory.
       */
      public static int totalSprites = 0;

      /**
       * Reads the sequence of pixels in a given sprite archive and converts them into a sprite.
       * 
       * @param name
       *    The name of this archive.
       *    
       * @param path
       *    The path to this archive.
       *    
       * @throws Exception
       *    The exception thrown.
       */
      public static void loadArchivedSprites(String name, Path path) throws Exception {
            MainController.SPRITES.clear();

            byte[] idx = readFile(path.toString() + System.getProperty("file.separator") + name + ".idx");
            byte[] dat = readFile(path.toString() + System.getProperty("file.separator") + name + ".dat");

            DataInputStream indexFile = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(idx)));
            DataInputStream dataFile = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(dat)));

            totalArchivedSprites = indexFile.readInt();

            for (int i = 0; i < totalArchivedSprites; ++i) {
                  int id = indexFile.readInt();

                  Sprite sprite = new Sprite();

                  sprite.decode(indexFile, dataFile);

                  MainController.SPRITES.add(id, sprite);

            }
            indexFile.close();
            dataFile.close();
      }

      /**
       * Reads an array of bytes from a file.
       * 
       * @param name
       *    The name of the file.
       *    
       * @return The read bytes.
       */
      public static byte[] readFile(String name) throws Exception {
            try (RandomAccessFile e = new RandomAccessFile(name, "r")) {
                  MappedByteBuffer buf = e.getChannel().map(MapMode.READ_ONLY, 0L, e.length());

                  byte[] data;
                  if (buf.hasArray()) {
                        data = buf.array();
                        return data;
                  }

                  byte[] array = new byte[buf.remaining()];
                  buf.get(array);
                  data = array;
                  return data;
            }
      }

      /**
       * Converts an array of bytes to a {@link BufferedImage}.
       * 
       * @param data
       *    The array of pixels.
       * 
       * @return The newly created image.
       */
      public static BufferedImage byteArrayToImage(byte[] data) throws IOException {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            BufferedImage image = ImageIO.read(in);
            return image;
      }

}
