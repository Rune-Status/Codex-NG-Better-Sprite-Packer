package io.nshusa.bsp;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.nshusa.bsp.util.ColorUtils;
import io.nshusa.bsp.util.Dialogue;
import io.nshusa.bsp.util.MultiMapAdapter;
import io.nshusa.bsp.util.SpritePackerUtils;
import io.nshusa.rsam.binary.Archive;
import io.nshusa.rsam.binary.sprite.Sprite;
import io.nshusa.rsam.util.ByteBufferUtils;
import io.nshusa.rsam.util.CompressionUtil;
import io.nshusa.rsam.util.HashUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public final class Controller implements Initializable {

	private double xOffset, yOffset;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	@FXML
	private void pack() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select the directory that contains your sprites.");
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));

		File selectedDirectory = chooser.showDialog(App.getMainStage());

		if (selectedDirectory == null) {
			return;
		}

		new Thread(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {

					Map<String, Meta> metaMap = readMetaFile(selectedDirectory);

					final Archive archive = Archive.create();

					final ByteArrayOutputStream ibos = new ByteArrayOutputStream();

					// marks the position of the first sprite within each image archive.
					int idxOffset = 0;

					try(DataOutputStream idxOut = new DataOutputStream(ibos)) {

						boolean flag = false;

						for (File imageArchiveDir : selectedDirectory.listFiles()) {

							if (!imageArchiveDir.isDirectory()) {
								continue;
							}

							File[] imageFiles = imageArchiveDir.listFiles();
							SpritePackerUtils.sortImages(imageFiles);

							for(;;) {
								Optional<File> result = SpritePackerUtils.validateArchiveColorLimit(imageFiles);

								if (result.isPresent()) {
									SpritePackerUtils.moveFile(result.get());
									flag = true;
								} else {
									break;
								}
							}

						}

						if (flag) {
							Platform.runLater(() -> Dialogue.showWarning(String.format(String.format("Images in dir=%s need their colors reduced.", selectedDirectory.getName() + "_output"))).showAndWait());
							return false;
						}

						File toQuantOutputDir = new File(selectedDirectory.getParentFile(), selectedDirectory.getName() + "_output");

						if (toQuantOutputDir.exists() && toQuantOutputDir.isDirectory()) {

							File[] files = toQuantOutputDir.listFiles();

							if (files.length > 0) {
								Platform.runLater(() -> Dialogue.showWarning(String.format("Images in dir=%s need their colors reduced.", toQuantOutputDir.getName())).showAndWait());
								return false;
							}

						}

						for (File imageArchiveDir : selectedDirectory.listFiles()) {

							// its not an image archive so skip it
							if (!imageArchiveDir.isDirectory()) {
								continue;
							}

							String imageArchiveName = imageArchiveDir.getName();

							if (imageArchiveName.lastIndexOf(".") == -1) {

								if (!SpritePackerUtils.isNumeric(imageArchiveName.substring(1, imageArchiveName.length()))) {
									imageArchiveName = imageArchiveName + ".dat";
								}

							}

							int imageArchiveHash = -1;

							try {
								imageArchiveHash = imageArchiveName.lastIndexOf(".") == -1 ? Integer.parseInt(imageArchiveName) : HashUtils.nameToHash(imageArchiveName);
							} catch (Exception ex) {

							}

							if (imageArchiveHash == -1) {
								System.out.println("hash is -1 for: " + imageArchiveName);
								continue;
							}

							if (!App.hashMap.containsKey(imageArchiveHash)) {
								System.out.println(String.format("Found a new image archive hash=%d name=%s", imageArchiveHash, imageArchiveName));
								App.hashMap.put(imageArchiveHash, imageArchiveName);
							}

							final ByteArrayOutputStream dbos = new ByteArrayOutputStream();

							try(DataOutputStream datOut = new DataOutputStream(dbos)) {

								int resizeWidth = 0;

								int resizeHeight = 0;

								// cache all of the images so we don't have to perform redundant I/O operations again
								final List<BufferedImage> images = new ArrayList<>();

								// list that acts as a set, using a list for the #get and #indexOf functions
								final List<Integer> colorSet = new ArrayList<>();
								colorSet.add(0);

								final File[] imageFiles = imageArchiveDir.listFiles();

								// make sure the images are sorted, order is really important
								SpritePackerUtils.sortImages(imageFiles);

								// iterator over the actual images
								for (int imageIndex = 0; imageIndex < imageFiles.length; imageIndex++) {

									final File imageFile = imageFiles[imageIndex];

									// an image can't be a directory so skip it
									if (!imageFile.exists() || imageFile.isDirectory() || !SpritePackerUtils.isValidImage(imageFile)) {
										System.out.println("skipping: " + imageArchiveName + " " + imageIndex + " " + !SpritePackerUtils.isValidImage(imageFile));
										continue;
									}

									final BufferedImage bimage = ImageIO.read(imageFile);

									if (resizeWidth < bimage.getWidth()) {
										resizeWidth = bimage.getWidth();
									}

									if (resizeHeight < bimage.getHeight()) {
										resizeHeight = bimage.getHeight();
									}

									final String key = imageArchiveName.substring(0, imageArchiveName.lastIndexOf(".") != -1 ? imageArchiveName.lastIndexOf(".") : imageArchiveName.length()) + ":" + imageIndex;

									final Meta meta = metaMap.get(key);

									if (meta != null) {

										if (meta.getResizeWidth() != 0) {
											resizeWidth = meta.getResizeWidth();
										}

										if (meta.getResizeHeight() != 0) {
											resizeHeight = meta.getResizeHeight();
										}

									}

									for (int x = 0; x < bimage.getWidth(); x++) {
										for (int y = 0; y < bimage.getHeight(); y++) {
											final int argb = bimage.getRGB(x,y);

											int rgb = argb & 0xFFFFFF;

											if (ColorUtils.getRed(rgb) == 0xFF && ColorUtils.getGreen(rgb) == 0 && ColorUtils.getBlue(rgb) == 0xFF) {
												bimage.setRGB(x, y, 0);
												rgb = bimage.getRGB(x, y);
											}

											// make sure there's no duplicate rgb values
											if (colorSet.contains(rgb)) {
												continue;
											}

											colorSet.add(rgb);

										}
									}

									images.add(bimage);

								}

								if (colorSet.size() > 256) {
									final String tempName = imageArchiveName;
									Platform.runLater(() -> Dialogue.showWarning(String.format("imageArchive=%s exeeded color limit of 256 colors=%d", tempName, colorSet.size())).showAndWait());
									return false;
								}

								// the largest width found in this image archive
								idxOut.writeShort(resizeWidth);

								// the largest height found in this image archive
								idxOut.writeShort(resizeHeight);

								// the palette size
								idxOut.writeByte(colorSet.size());

								// make sure to skip the first index
								for (int i = 1; i < colorSet.size(); i++) {
									ByteBufferUtils.writeU24Int(colorSet.get(i), idxOut);
								}

								final List<BufferedImageWrapper> wImages = new ArrayList<>();

								for (int i = 0; i < images.size(); i++) {

									final BufferedImage bImage = images.get(i);

									final String key = imageArchiveName.substring(0, imageArchiveName.lastIndexOf(".") != -1 ? imageArchiveName.lastIndexOf(".") : imageArchiveName.length()) + ":" + i;

									Meta meta = metaMap.get(key);

									final int offsetX = meta == null ? 0 : meta.getOffsetX();

									final int offsetY = meta == null ? 0 : meta.getOffsetY();

									final int format = meta == null ? 0 : meta.getFormat();

									// offsetX
									idxOut.writeByte(offsetX);

									// offsetY
									idxOut.writeByte(offsetY);

									// image width
									idxOut.writeShort(bImage.getWidth());

									// image height
									idxOut.writeShort(bImage.getHeight());

									// encoding type (0 horizontal | 1 vertical)
									idxOut.writeByte(format);

									wImages.add(new BufferedImageWrapper(bImage, format));
								}

								datOut.writeShort(idxOffset);

								idxOffset = idxOut.size();

								for (BufferedImageWrapper wrapper : wImages) {

									final BufferedImage bImage = wrapper.getBimage();

									if (wrapper.getFormat() == 0) { // horizontal encoding
										for (int y = 0; y < bImage.getHeight(); y++) {
											for (int x = 0; x < bImage.getWidth(); x++) {
												final int argb = bImage.getRGB(x, y);

												final int rgb = argb & 0xFFFFFF;

												final int paletteIndex = colorSet.indexOf(rgb);

												assert(paletteIndex != -1);

												datOut.writeByte(paletteIndex);
											}
										}
									} else { // vertical encoding
										for (int x = 0; x < bImage.getWidth(); x++) {
											for (int y = 0; y < bImage.getHeight(); y++) {
												final int argb = bImage.getRGB(x, y);

												final int rgb = argb & 0xFFFFFF;

												final int paletteIndex = colorSet.indexOf(rgb);

												assert(paletteIndex != -1);

												datOut.writeByte(paletteIndex);
											}
										}
									}

								}

							}

							final byte[] uncompresedData = dbos.toByteArray();

							final byte[] compressedData = CompressionUtil.bzip2(uncompresedData);

							archive.getEntries().add(new Archive.ArchiveEntry(imageArchiveHash, uncompresedData.length, compressedData.length, compressedData));
						}

						final byte[] uncompressed = ibos.toByteArray();

						final byte[] compressed = CompressionUtil.bzip2(uncompressed);

						archive.getEntries().add(new Archive.ArchiveEntry(HashUtils.nameToHash("index.dat"), uncompressed.length, compressed.length, compressed));

						final byte[] encoded = archive.encode();

						try(FileOutputStream fos = new FileOutputStream(new File(selectedDirectory.getParentFile(), selectedDirectory.getName() + ".jag"))) {
							fos.write(encoded);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

					final File rsam = new File(System.getProperty("user.home") + File.separator + ".rsam");

					if (!rsam.exists()) {
						rsam.mkdirs();
					}

					try(PrintWriter writer = new PrintWriter(new FileWriter(new File(rsam, "hashes.txt")))) {
						for (Map.Entry<Integer, String> entry : App.hashMap.entrySet()) {
							final int hash = entry.getKey();
							final String value = entry.getValue();
							writer.println(value + ":" + hash);
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
					Platform.runLater(() -> Dialogue.showWarning(String.format("Failed to pack directory=%s", selectedDirectory.getName())).showAndWait());
					return false;
				}

				Platform.runLater(() -> Dialogue.showInfo("Information", "Success!").showAndWait());
				return true;
			}
		}).start();

	}

	@FXML
	private void unpack() {
		final FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.setTitle("Select a sprite archive file");

		File selectedFile = chooser.showOpenDialog(App.getMainStage());

		if (selectedFile == null) {
			return;
		}

		if (!selectedFile.getName().endsWith(".jag")) {
			File newFile = new File(selectedFile.getParentFile(), selectedFile.getName().lastIndexOf(".") != -1 ? selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf(".")) + ".jag" : selectedFile.getName() + ".jag");

			if (selectedFile.renameTo(newFile)) {
				selectedFile = newFile;
			}

			Dialogue.showWarning("Added .jag file extension to avoid issues on Linux machines").showAndWait();
		}

		Optional<Archive> result = Optional.empty();

		try {
			result = Optional.of(Archive.decode(Files.readAllBytes(selectedFile.toPath())));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!result.isPresent()) {
			Dialogue.showWarning(String.format("File=%s is not in an RS2 archive format.", selectedFile.getName())).showAndWait();
			return;
		}

		final File outputDir = new File(selectedFile.getParentFile(), selectedFile.getName().lastIndexOf(".") != -1 ? selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf(".")) : selectedFile.getName());

		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		final Archive archive = result.get();

		if (!archive.contains("index.dat")) {
			Dialogue.showWarning(String.format("archive=%s does not contain an index.dat", selectedFile.getName())).showAndWait();
			return;
		}

		new Thread(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {

					final int indexHash = HashUtils.nameToHash("index.dat");

					final Multimap<String, Meta> metaMap = MultimapBuilder.treeKeys((Comparator<String>) Comparator.naturalOrder()).arrayListValues().build();

					for (Archive.ArchiveEntry entry : archive.getEntries()) {

						if (entry == null) {
							continue;
						}

						if (entry.getHash() == indexHash) {
							continue;
						}

						String imageArchiveName = App.hashMap.get(entry.getHash());

						if (imageArchiveName == null) {
							imageArchiveName = Integer.toString(entry.getHash());
						}

						if (imageArchiveName.lastIndexOf(".") != -1) {
							imageArchiveName = imageArchiveName.substring(0, imageArchiveName.lastIndexOf("."));
						}

						final List<Sprite> sprites = new ArrayList<>();

						int lastSpriteId;

						for (lastSpriteId = 0; ;lastSpriteId++) {
							try {

								Sprite sprite = Sprite.decode(archive, entry.getHash(), lastSpriteId);

								if (sprite.getWidth() > 765 || sprite.getHeight() > 765) {
									continue;
								}

								sprites.add(sprite);
							} catch (Exception ex) {
								break;
							}
						}

						if (sprites.isEmpty()) {
							final String tempName = imageArchiveName;
							final int tempLastSpriteId = lastSpriteId;

							Platform.runLater(() -> Dialogue.showWarning(String.format("Could not decode image archive=%s at index=%d", tempName, tempLastSpriteId)));
							return false;
						}

						final File imageArchiveDir = new File(outputDir, imageArchiveName);

						if (!imageArchiveDir.exists()) {
							imageArchiveDir.mkdirs();
						}

						for (int i = 0; i < sprites.size(); i++) {

							final Sprite sprite = sprites.get(i);

							if (sprite == null) {
								continue;
							}

							Meta metaValue = new Meta(i, sprite.getOffsetX(), sprite.getOffsetY(), sprite.getResizeWidth(), sprite.getResizeHeight(), sprite.getFormat());

							metaMap.put(imageArchiveName, metaValue);

							ImageIO.write(sprite.toBufferedImage(), "png", new File(imageArchiveDir, Integer.toString(i) + ".png"));

						}

					}

					Gson gson = new GsonBuilder().setPrettyPrinting().create();

					String json = gson.toJson(metaMap.asMap());

					try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputDir, "meta.json")))) {
						writer.write(json);
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

				Platform.runLater(() -> {
					Dialogue.showInfo("Information!", "Success!").showAndWait();
				});

				return true;

			}

		}).start();

	}

	private Map<String, Meta> readMetaFile(File selectedDirectory) throws IOException {
		final File metaFile = new File(selectedDirectory, "meta.json");

		Map<String, Meta> map = new HashMap<>();

		if (!metaFile.exists()) {
			return map;
		}

			Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
					.registerTypeAdapter(Multimap.class, new MultiMapAdapter<String, Meta>()).create();

			try(BufferedReader reader = new BufferedReader(new FileReader(metaFile))) {

				Type type = new TypeToken<Multimap<String, Meta>>() {}.getType();

				Multimap<String, Meta> multimap = gson.fromJson(reader, type);

				for (Map.Entry<String, Meta> entry : multimap.entries()) {

					final String key = entry.getKey();

					final Meta value = entry.getValue();

					if (!(value.getFormat() == 0 || value.getFormat() == 1)) {
						Platform.runLater(() -> Dialogue.showWarning(String.format("Format must be either 0 (horizontal) or 1 (vertical) detected format=%d", value.getFormat())).showAndWait());
						return map;
					}

					map.put(key + ":" + value.getId(), value);

				}

				return map;

			}

	}

	@FXML
	private void handleMouseDragged(MouseEvent event) {

		Stage stage = App.getMainStage();

		stage.setX(event.getScreenX() - xOffset);
		stage.setY(event.getScreenY() - yOffset);

	}

	@FXML
	private void handleMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	@FXML
	private void minimizeProgram() {

		if (App.getMainStage() == null) {
			return;
		}

		App.getMainStage().setIconified(true);
	}

	@FXML
	private void closeProgram() {
		Platform.exit();
	}

}
