package com.softgate.util;

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

import com.softgate.Configuration;

/**
 * The class that contains operations for files.
 * 
 * @author Chad Adams
 */
public final class FileUtils {

	/**
	 * Will read the text file resource containing the current directory.
	 * 
	 * @param fileName
	 *            The name of the resource.
	 * 
	 * @throws IOException
	 */
	public static void readCachePathResource(String fileName) throws IOException {
		try (BufferedReader input = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(System.getProperty("user.home") + File.separator + fileName))))) {

			final String line = input.readLine();

			if (line != null) {
				Configuration.CACHE_PATH = line.equalsIgnoreCase("user.home") ? System.getProperty("user.home") : line;
			}
		}
	}

	/**
	 * Will write the current directory to the resource file.
	 * 
	 * @param fileName
	 *            The name of the file to write.
	 * 
	 * @param path
	 *            The current directory.
	 * 
	 * @throws IOException,
	 *             URISyntaxException
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
		try (RandomAccessFile raf = new RandomAccessFile(name, "r")) {			
			MappedByteBuffer buffer = raf.getChannel().map(MapMode.READ_ONLY, 0L, raf.length());

			byte[] data = new byte[buffer.remaining()];

			buffer.get(data);
			
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

	public static int calculateLargestImageIndex(File[] files) {

		int largest = 0;

		for (int index = 0; index < files.length; index++) {

			File file = files[index];
			
			if (file == null) {
				continue;
			}

			if (!FileUtils.isValidImage(file.getName())) {
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
		
		File[] sorted = new File[FileUtils.calculateLargestImageIndex(files) + 1];
		
		for (int index = 0; index < files.length; index++) {
			
			File file = files[index];
			
			if (file == null) {
				continue;
			}
			
			if (!FileUtils.isValidImage(file.getName())) {
				continue;
			}			
			
			final String modifiedName = file.getName().replace(file.getName().substring(file.getName().lastIndexOf(".")), "");			
			
			try {
				int imageIndex = Integer.parseInt(modifiedName);

				sorted[imageIndex] = file;
			} catch (NumberFormatException ex) {
				sorted[FileUtils.findFreeIndex(sorted)] = file;
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
