package io.nshusa.bsp;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;

import io.nshusa.bsp.util.Dialogue;
import io.nshusa.bsp.util.Misc;
import io.nshusa.rsam.binary.Archive;
import io.nshusa.rsam.binary.sprite.Sprite;
import io.nshusa.rsam.codec.ImageArchiveDecoder;
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

					readMetaFile(selectedDirectory);

					final Archive archive = Archive.create();

					final ByteArrayOutputStream ibos = new ByteArrayOutputStream();

					// marks the position of the first sprite within each image archive.
					int idxOffset = 0;

					try(DataOutputStream idxOut = new DataOutputStream(ibos)) {

						// iterator over the image archives which are essentially directories that store images
						for (File imageArchiveDir : selectedDirectory.listFiles()) {

							// its not an image archive so skip it
							if (!imageArchiveDir.isDirectory()) {
								continue;
							}

							String imageArchiveName = imageArchiveDir.getName();

							if (imageArchiveName.lastIndexOf(".") == -1) {

								if (!Misc.isNumeric(imageArchiveName.substring(1, imageArchiveName.length()))) {
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

								// first we have to calculate the largest width and largest height of the image found in this image archive.
								int largestWidth = 0;

								int largestHeight = 0;

								// cache all of the images so we don't have to perform redundant I/O operations again
								final List<BufferedImage> images = new ArrayList<>();

								// list that acts as a set, using a list for the #get and #indexOf functions
								final List<Integer> colorSet = new ArrayList<>();

								colorSet.add(0);

								final File[] imageFiles = imageArchiveDir.listFiles();

								// make sure the images are sorted, order is really important
								Misc.sortImages(imageFiles);

								// iterator over the actual images
								for (int imageIndex = 0; imageIndex < imageFiles.length; imageIndex++) {

									final File imageFile = imageFiles[imageIndex];

									if (imageArchiveName.contains("orbs")) {
										System.out.println(imageArchiveName + " " + imageFile.getName());
									}

									// an image can't be a directory so skip it
									if (!imageFile.exists() || imageFile.isDirectory() || !Misc.isValidImage(imageFile)) {
										System.out.println("skipping: " + imageArchiveName + " " + imageIndex + " " + !Misc.isValidImage(imageFile));
										continue;
									}

											final BufferedImage bimage = Misc.convertToGIF(imageFile);

											if (largestWidth < bimage.getWidth()) {
												largestWidth = bimage.getWidth();
											}

											if (largestHeight < bimage.getHeight()) {
												largestHeight = bimage.getHeight();
											}

											int colorCount = 0;

											for (int x = 0; x < bimage.getWidth(); x++) {
												for (int y = 0; y < bimage.getHeight(); y++) {
													final int argb = bimage.getRGB(x,y);

													final int rgb = argb & 0xFFFFFF;

													// make sure there's no duplicate rgb values
													if (colorSet.contains(rgb)) {
														continue;
													}

													colorCount++;

													colorSet.add(rgb);

												}
											}

										if ((colorCount + colorSet.size()) > 256 && imageIndex < imageFiles.length - 1) {
											final int tempCount = colorCount;
											Platform.runLater(() -> Dialogue.showWarning(String.format("Archive exceeds 256 colors on= %s colors current=%d predicted=%d", imageFile.getParentFile().getName() + "/" + imageFile.getName(), colorSet.size(), (tempCount + colorSet.size()))).showAndWait());
											return false;
										}

										images.add(bimage);

								}

								if (colorSet.size() > 256) {
									final String tempName = imageArchiveName;
									Platform.runLater(() -> Dialogue.showWarning(String.format("imageArchive=%s exeeded color limit of 256 colors=%d", tempName, colorSet.size())).showAndWait());
									return false;
								}

								if (imageArchiveName.contains("orbs")) {
									System.out.println(imageArchiveName + " " + images.size());
								}

								// the largest width found in this image archive
								idxOut.writeShort(largestWidth);

								// the largest height found in this image archive
								idxOut.writeShort(largestHeight);

								// the palette size
								idxOut.writeByte(colorSet.size());

								// make sure to skip the first index
								for (int i = 1; i < colorSet.size(); i++) {
									ByteBufferUtils.writeU24Int(colorSet.get(i), idxOut);
								}

								final List<BufferedImageWrapper> wImages = new ArrayList<>();

								for (int i = 0; i < images.size(); i++) {

									final BufferedImage bimage = images.get(i);

									final String key = imageArchiveName.substring(0, imageArchiveName.lastIndexOf(".") != -1 ? imageArchiveName.lastIndexOf(".") : imageArchiveName.length()) + ":" + i;

									final SpriteMeta meta = App.offsetMap.get(key);

									final int offsetX = meta == null ? 0 : meta.getX();

									final int offsetY = meta == null ? 0 : meta.getY();

									final int format = meta == null ? 0 : meta.getFormat();

									// offsetX
									idxOut.writeByte(offsetX);

									// offsetY
									idxOut.writeByte(offsetY);

									// image width
									idxOut.writeShort(bimage.getWidth());

									// image height
									idxOut.writeShort(bimage.getHeight());

									// encoding type (0 horizontal | 1 vertical)
									idxOut.writeByte(format);

//									if (imageArchiveHash == HashUtils.nameToHash("orbs.dat")) {
//										System.out.println(String.format("id=%d offsetX=%d offsetY=%d width=%d height=%d format=%d colors=%d", i, offsetX, offsetY, bimage.getWidth(), bimage.getHeight(), format, colorSet.size()));
//									}

									wImages.add(new BufferedImageWrapper(bimage, format));
								}

								datOut.writeShort(idxOffset);

								idxOffset = idxOut.size();

								for (BufferedImageWrapper wrapper : wImages) {

									final BufferedImage bimage = wrapper.getBimage();

									if (wrapper.getFormat() == 0) { // horizontal encoding
										for (int y = 0; y < bimage.getHeight(); y++) {
											for (int x = 0; x < bimage.getWidth(); x++) {
												final int argb = bimage.getRGB(x, y);

												final int rgb = argb & 0xFFFFFF;

												final int paletteIndex = colorSet.indexOf(rgb);

												assert(paletteIndex != -1);

												datOut.writeByte(paletteIndex);
											}
										}
									} else { // vertical encoding
										for (int x = 0; x < bimage.getWidth(); x++) {
											for (int y = 0; y < bimage.getHeight(); y++) {
												final int argb = bimage.getRGB(x, y);

												final int rgb = argb & 0xFFFFFF;

												final int paletteIndex = colorSet.indexOf(rgb);

												assert(paletteIndex != -1);

												datOut.writeByte(paletteIndex);
											}
										}
									}

								}

								//System.out.println(imageArchiveName + " " + colorSet.size());

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
					Platform.runLater(() -> {
						Dialogue.showWarning(String.format("Failed to pack directory=%s", selectedDirectory.getName())).showAndWait();
					});
					return false;
				}

				Platform.runLater(() -> {
					Dialogue.showInfo("Information", "Success!").showAndWait();
				});

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

					final Map<String, SpriteMeta> offsetMap = new LinkedHashMap<>();

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

						List<Sprite> sprites = null;

						try {
							sprites = ImageArchiveDecoder.decode(ByteBuffer.wrap(archive.readFile(entry.getHash())), ByteBuffer.wrap(archive.readFile("index.dat")));
						} catch (Exception ex) {
							ex.printStackTrace();
						}

						if (sprites == null) {
							System.out.println("sprite is null");
							continue;
						}

						final File imageArchiveDir = new File(outputDir, imageArchiveName);

						if (!imageArchiveDir.exists()) {
							imageArchiveDir.mkdirs();
						}

						if (HashUtils.nameToHash("orbs.dat") == entry.getHash()) {
							System.out.println(sprites.size());
						}

						for (int i = 0; i < sprites.size(); i++) {

							final Sprite sprite = sprites.get(i);

							if (sprite == null) {
								continue;
							}

							String key = imageArchiveName + ":" + i;

							SpriteMeta value = new SpriteMeta(sprite.getOffsetX(), sprite.getOffsetY(), sprite.getFormat());

							offsetMap.put(key, value);

							ImageIO.write(sprite.toBufferedImage(), "gif", new File(imageArchiveDir, Integer.toString(i) + ".gif"));

						}

					}

					try(PrintWriter writer = new PrintWriter(new FileWriter(new File(outputDir, "meta.txt")))) {
						for (Map.Entry<String, SpriteMeta> entry : offsetMap.entrySet()) {

							if (entry.getValue().getX() != 0 || entry.getValue().getY() != 0 || entry.getValue().getFormat() != 0) {
								writer.println(entry.getKey() + ":" + entry.getValue());
							}
						}
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

	private void readMetaFile(File selectedDirectory) throws IOException {
		final File offsetFile = new File(selectedDirectory, "meta.txt");

		if (offsetFile.exists()) {

			final List<String> lines = Files.readAllLines(offsetFile.toPath());

			for (String line : lines) {

				final String split[] = line.split(":");

				int x = 0;
				int y = 0;
				int format = 0;

				if (split.length == 5) {
					try {
						x = Integer.parseInt(split[2]);
					} catch (NumberFormatException ex) {

					}

					try {
						y = Integer.parseInt(split[3]);
					} catch (NumberFormatException ex) {

					}

					try {
						format = Integer.parseInt(split[4]);
					} catch (NumberFormatException ex) {

					}

				} else if (split.length == 4) {
					try {
						x = Integer.parseInt(split[2]);
					} catch (NumberFormatException ex) {

					}

					try {
						y = Integer.parseInt(split[3]);
					} catch (NumberFormatException ex) {

					}
				}

				if (!(format == 0 || format == 1)) {
					final int tempFormat = format;
					Platform.runLater(() -> Dialogue.showWarning(String.format("Format must be either 0 (horizontal) or 1 (vertical) detected format=%d", tempFormat)).showAndWait());
					return;
				}

				App.offsetMap.put(split[0] + ":" +  split[1], new SpriteMeta(x, y, format));

			}

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
