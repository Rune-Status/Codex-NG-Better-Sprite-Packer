package com.seven.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import javax.imageio.ImageIO;

import com.seven.Configuration;

/**
 * The class that contains operations for files.
 * 
 * @author Seven
 */
public final class FileUtils {

	/**
	 * Will read the text file resource containing the current directory.
	 * 
	 * @param fileName
	 * 		The name of the resource.
	 * 
	 * @throws IOException
	 */
	public static void readCachePathResource(String fileName) throws IOException {
		try (BufferedReader input = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(System.getProperty("user.home") + File.separator + fileName))))) {

			final String line = input.readLine();
			
			if (line != null) {		
				Configuration.CACHE_PATH = line.equalsIgnoreCase("user.home") ? System.getProperty("user.home")
					: line;
			}
		}
	}

	/**
	 * Will write the current directory to the resource file.
	 * 
	 * @param fileName
	 * 		The name of the file to write.
	 * 
	 * @param path
	 * 		The current directory.
	 * 
	 * @throws IOException, URISyntaxException
	 */
	public static void writeCachePathResource(String fileName, String path) throws IOException, URISyntaxException {
		try (PrintWriter writer = new PrintWriter(System.getProperty("user.home") + File.separator + fileName)) {
			writer.println(path);
			Configuration.CACHE_PATH = path;
		}
	}

	/**
	 * Reads an array of bytes from a file.
	 * 
	 * @param name
	 *            The name of the file.
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
	 *            The array of pixels.
	 * 
	 * @return The newly created image.
	 */
	public static BufferedImage byteArrayToImage(byte[] data) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		BufferedImage image = ImageIO.read(in);
		return image;
	}

}
