package com.seven.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import javax.imageio.ImageIO;

/**
 * The class that contains operations for files.
 * 
 * @author Seven
 */
public final class FileUtils {

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
