package io.creativelabs.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import javax.imageio.ImageIO;

public final class FileUtils {

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
