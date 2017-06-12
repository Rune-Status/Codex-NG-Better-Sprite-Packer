package io.creativelab;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import com.creativelab.sprite.SpriteBase;
import com.creativelab.sprite.codec.SpriteDecoder;
import com.creativelab.sprite.codec.SpriteEncoder;
import com.creativelab.util.ColorQuantizer;

import io.creativelab.util.Dialogue;
import io.creativelab.util.Misc;
import io.creativelab.util.msg.InputMessage;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class Controller implements Initializable {

	@FXML
	ListView<Node> list;

	@FXML
	ScrollPane scrollPane;

	@FXML
	Button writeSpriteBtn, dumpSpriteBtn, loadSpriteBtn, loadArchiveBtn, clearBtn, resizeBtn;

	@FXML
	ImageView imageView;

	@FXML
	MenuItem openMI, openArchiveMI, creditMI, closeMI;

	@FXML
	TextField indexTf, widthTf, heightTf;

	@FXML
	TextField searchTf, nameTf, offsetXTf, offsetYTf;

	@FXML
	TitledPane titledPane;

	@FXML
	MenuItem dumpSpriteMI, dumpAllSpritesMI, viewDirectoryMI;

	private FilteredList<Node> filteredSprites;

	private ObservableList<Node> elements = FXCollections.observableArrayList();

	private int currentSpriteIndex = 0;

	Image newImage;

	File currentDirectory = new File(System.getProperty("user.home"));

	private int totalSprites;

	@FXML
	ColorPicker colorPicker;

	@FXML
	BorderPane root;

	@FXML
	VBox rightVBox;

	private double xOffset, yOffset;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		colorPicker.setValue(Color.FUCHSIA);

		filteredSprites = new FilteredList<>(elements, it -> true);

		searchTf.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredSprites.setPredicate(it -> {

				if (it.getSprite() == null) {
					return false;
				}

				SpriteBase sprite = it.getSprite();

				if (newValue.isEmpty()) {
					return true;
				}

				if (sprite.getName().toLowerCase().contains(newValue)) {
					return true;
				}

				if (Integer.toString(sprite.getId()).contains(newValue)) {
					return true;
				}

				return false;
			});
		});

		list.setItems(this.filteredSprites);

		list.setCellFactory(param -> new ListCell<Node>() {

			private final ImageView listIconView = new ImageView();

			@Override
			public void updateItem(Node value, boolean empty) {
				super.updateItem(value, empty);

				if (empty) {
					setGraphic(null);
					setText("");
				} else {

					if (value.getSprite() == null || value.getSprite().getPixels() == null
							|| value.getSprite().getPixels().length == 0) {
						listIconView.setImage(new Image(getClass().getResourceAsStream("/icons/question_mark.png")));
						listIconView.setFitHeight(32);
						listIconView.setFitWidth(32);
						setText(Integer.toString(value.getId()));
						setGraphic(listIconView);
						return;
					}

					BufferedImage image = value.getSprite().toBufferedImage();

					listIconView.setFitHeight(image.getHeight() > 128 ? 128 : image.getHeight());
					listIconView.setFitWidth(image.getWidth() > 128 ? 128 : image.getWidth());
					listIconView.setPreserveRatio(true);

					newImage = SwingFXUtils
							.toFXImage(Misc.makeColorTransparent(image, new java.awt.Color(0xFF00FF, true)), null);

					listIconView.setImage(newImage);
					setText(Integer.toString(value.getId()));
					setGraphic(listIconView);

				}
			}

		});

		list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Node>() {
			@Override
			public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {

				if (newValue != null) {

					try {
						currentSpriteIndex = newValue.getId();

						final SpriteBase sprite = newValue.getSprite();

						if (!sprite.getName().equalsIgnoreCase("None")) {
							nameTf.setText(sprite.getName());
						} else {
							nameTf.clear();
						}

						System.out.println(sprite.getId() + " " + sprite.getName());

						indexTf.setText(Integer.toString(sprite.getId()));

						if (newImage != null) {
							widthTf.setText(Double.toString(newImage.getWidth()));
							heightTf.setText(Double.toString(newImage.getHeight()));
						}

						offsetXTf.setText(Integer.toString(sprite.getDrawOffsetX()));
						offsetYTf.setText(Integer.toString(sprite.getDrawOffsetY()));

						displaySprite(newValue);

					} catch (Exception ex) {
						Dialogue.showException("A problem was encountered while trying to display a sprite.", ex);
					}
				}
			}
		});

	}

	@FXML
	private void clearEditor() {
		elements.clear();
		filteredSprites.clear();
		list.getItems().clear();
		imageView.setImage(null);

		nameTf.clear();
		offsetXTf.clear();
		offsetYTf.clear();
		indexTf.setText("");
		widthTf.setText("");
		heightTf.setText("");

		colorPicker.setValue(Color.FUCHSIA);

		App.getMainStage().setTitle(String.format("%s", App.properties.getProperty("title")));
	}

	@FXML
	private void openSpriteDirectory() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select the directory that contains your sprites.");

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(currentDirectory);

		File selectedDirectory = chooser.showDialog(App.getMainStage());

		if (selectedDirectory != null) {

			currentDirectory = selectedDirectory;

			try {
				loadSprites(selectedDirectory.toPath());
			} catch (Exception ex) {
				Dialogue.showException("A problem was encountered when opening the sprites directory.", ex);
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
	private void openArchiveDirectory() {

		FileChooser fileChooser = new FileChooser();

		fileChooser.setInitialDirectory(currentDirectory);

		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Data files", "*.dat", "*.bsp"));

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		fileChooser.setInitialDirectory(currentDirectory);

		File selectedFile = fileChooser.showOpenDialog(App.getMainStage());

		if (selectedFile != null) {

			if (!selectedFile.isDirectory()) {
				currentDirectory = selectedFile.getParentFile();
			}

			String archiveName = "sprites";

			try {

				unpackSprites(archiveName, selectedFile.toPath());

			} catch (Exception ex) {
				Dialogue.showException("A problem was encountered while loading the sprites archive.", ex);
				return;
			}

		}
	}

	@FXML
	private void handleKeyEventPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {

			SpriteBase sprite = elements.get(currentSpriteIndex).getSprite();

			if (!nameTf.getText().isEmpty() && nameTf.getText().length() <= 14) {
				sprite.setName(nameTf.getText());
				nameTf.clear();
			}

			if (!offsetXTf.getText().isEmpty() && offsetXTf.getText().length() < 3) {
				sprite.setDrawOffsetX(Integer.parseInt(offsetXTf.getText()));
				offsetXTf.clear();
			}

			if (!offsetYTf.getText().isEmpty() && offsetYTf.getText().length() < 3) {
				sprite.setDrawOffsetY(Integer.parseInt(offsetYTf.getText()));
				offsetYTf.clear();
			}

			elements.set(currentSpriteIndex, new Node(this.currentSpriteIndex, sprite));

		}
	}

	@FXML
	private void dumpSprite() {

		if (elements.isEmpty()) {
			Dialogue.showInfo("Information", "There are no sprites to write.");
			return;
		}

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select the directory to place the sprites in.");

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(currentDirectory);

		File selectedDirectory = chooser.showDialog(App.getMainStage());

		if (selectedDirectory != null) {

			currentDirectory = selectedDirectory;

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Options");
			alert.setHeaderText(null);
			alert.setContentText("Choose your option.");

			ButtonType buttonTypeOne = new ButtonType("Transparent background");
			ButtonType buttonTypeTwo = new ButtonType("Colored Background");
			ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == buttonTypeOne) {

				createTask(new Task<Boolean>() {

					@Override
					protected Boolean call() throws Exception {

						SpriteBase sprite = elements.get(currentSpriteIndex).getSprite();

						if (sprite != null) {

							int[] pixels = sprite.getPixels();

							try {
								final BufferedImage image = new BufferedImage(sprite.getWidth(), sprite.getHeight(),
										BufferedImage.TYPE_INT_ARGB);

								int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

								System.arraycopy(pixels, 0, data, 0, pixels.length);

								ImageIO.write(image, "png", Paths
										.get(selectedDirectory.toString(), Integer.toString(sprite.getId()) + ".png")
										.toFile());
							} catch (IOException ex) {
								ex.printStackTrace();
							}

							updateProgress(sprite.getId() + 1, elements.size());
							updateMessage("(" + (sprite.getId() + 1) + "/" + elements.size() + ")");

							Platform.runLater(() -> {
								Dialogue.openDirectory("Success! Would you like to view this directory?",
										selectedDirectory);
							});

						}

						return true;
					}
				});

			} else if (result.get() == buttonTypeTwo) {

				createTask(new Task<Boolean>() {

					@Override
					protected Boolean call() throws Exception {

						SpriteBase sprite = elements.get(currentSpriteIndex).getSprite();

						if (sprite != null) {

							int[] pixels = sprite.getPixels();

							try {
								final BufferedImage image = new BufferedImage(sprite.getWidth(), sprite.getHeight(),
										BufferedImage.TYPE_INT_ARGB);

								int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

								System.arraycopy(pixels, 0, data, 0, pixels.length);

								ImageIO.write(
										Misc.createColoredBackground(image,
												Misc.fxColorToAWTColor(colorPicker.getValue())),
										"png", Paths.get(selectedDirectory.toString(),
												Integer.toString(sprite.getId()) + ".png").toFile());
							} catch (IOException ex) {
								ex.printStackTrace();
							}

							updateProgress(sprite.getId() + 1, elements.size());
							updateMessage("(" + (sprite.getId() + 1) + "/" + elements.size() + ")");

							Platform.runLater(() -> {
								Dialogue.openDirectory("Success! Would you like to view this directory?",
										selectedDirectory);
							});

						}

						return true;
					}
				});

			} else {
				return;
			}

		}
	}

	@FXML
	private void dumpSprites() {

		if (elements.isEmpty()) {
			Dialogue.showInfo("Information", "There are no sprites to write.");
			return;
		}

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select the directory to place the sprites in.");

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(currentDirectory);

		File selectedDirectory = chooser.showDialog(App.getMainStage());

		if (selectedDirectory != null) {

			currentDirectory = selectedDirectory;

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Options");
			alert.setHeaderText(null);
			alert.setContentText("Choose your option.");

			ButtonType buttonTypeOne = new ButtonType("Transparent background");
			ButtonType buttonTypeTwo = new ButtonType("Colored Background");
			ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

			alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == buttonTypeOne) {

				createTask(new Task<Boolean>() {

					@Override
					protected Boolean call() throws Exception {
						for (Node entry : elements) {
							if (entry != null) {

								SpriteBase sprite = entry.getSprite();

								if (sprite != null) {

									int[] pixels = sprite.getPixels();

									if (pixels.length == 0) {
										continue;
									}

									try {
										final BufferedImage image = new BufferedImage(sprite.getWidth(),
												sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);

										int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

										System.arraycopy(pixels, 0, data, 0, pixels.length);

										ImageIO.write(image, "png", Paths.get(selectedDirectory.toString(),
												Integer.toString(entry.getId()) + ".png").toFile());
									} catch (IOException ex) {
										ex.printStackTrace();
									}
									updateProgress(entry.getId() + 1, elements.size());
									updateMessage("(" + (entry.getId() + 1) + "/" + elements.size() + ")");
								}
							}
						}

						Platform.runLater(() -> {
							Dialogue.openDirectory("Success! Would you like to view this directory?",
									selectedDirectory);
						});

						return true;
					}

				});

			} else if (result.get() == buttonTypeTwo) {

				createTask(new Task<Boolean>() {

					@Override
					protected Boolean call() throws Exception {
						for (Node entry : elements) {
							if (entry != null) {

								SpriteBase sprite = entry.getSprite();

								if (sprite != null) {

									int[] pixels = sprite.getPixels();

									if (pixels.length == 0) {
										continue;
									}

									try {
										final BufferedImage image = new BufferedImage(sprite.getWidth(),
												sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);

										int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

										System.arraycopy(pixels, 0, data, 0, pixels.length);

										ImageIO.write(
												Misc.createColoredBackground(image,
														Misc.fxColorToAWTColor(colorPicker.getValue())),
												"png", Paths.get(selectedDirectory.toString(),
														Integer.toString(entry.getId()) + ".png").toFile());
									} catch (IOException ex) {
										ex.printStackTrace();
									}
									updateProgress(entry.getId() + 1, elements.size());
									updateMessage("(" + (entry.getId() + 1) + "/" + elements.size() + ")");
								}
							}
						}

						Platform.runLater(() -> {
							Dialogue.openDirectory("Success! Would you like to view this directory?",
									selectedDirectory);
						});

						return true;
					}

				});

			} else {
				return;
			}

		}

	}

	@FXML
	private void addSprite() {

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		FileChooser fileChooser = new FileChooser();

		fileChooser.setInitialDirectory(currentDirectory);

		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(App.getMainStage());

		if (selectedFiles != null) {

			for (File selectedFile : selectedFiles) {

				try {
					if (!selectedFile.isDirectory()) {
						currentDirectory = selectedFile.getParentFile();
					}

					BufferedImage image = Misc.makeColorTransparent(Misc.convert(ColorQuantizer.quantize(ImageIO.read(selectedFile)), BufferedImage.TYPE_INT_ARGB),
							new java.awt.Color(0xFF00FF, true));

					int[] source1Pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

					boolean duplicate = false;

					for (Node e : elements) {

						SpriteBase s = e.getSprite();

						BufferedImage image2 = s.toBufferedImage();

						int[] source2Pixels = ((DataBufferInt) image2.getRaster().getDataBuffer()).getData();

						for (int i = 0; i < source1Pixels.length; i++) {

							if (source1Pixels[i] != source2Pixels[i]) {
								duplicate = false;
								System.out.println(s.getId() + " " + source1Pixels[i] + " is not the same as " + source2Pixels[i]);
								break;
							}

							duplicate = true;

						}

						if (duplicate) {
							Dialogue.showWarning("Skipped duplicate at: " + s.getId());
							break;
						}

					}

					if (duplicate) {
						continue;
					}

					SpriteBase sprite = new SpriteBase(elements.size());

					sprite.setName(selectedFile.getName());
					sprite.setWidth(image.getWidth());
					sprite.setHeight(image.getHeight());
					sprite.setPixels(source1Pixels);

					elements.add(new Node(elements.size(), sprite));

				} catch (IOException ex) {
					Dialogue.showWarning("Could not read selected file as an image.");
				}

			}

		}

	}

	@FXML
	private void replaceSprite() {
		if (elements.isEmpty()) {
			Dialogue.showWarning("There are no sprites to replace.");
			return;
		}

		FileChooser fileChooser = new FileChooser();

		fileChooser.setInitialDirectory(
				this.currentDirectory == null ? new File(System.getProperty("user.home")) : this.currentDirectory);

		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(App.getMainStage());

		if (selectedFile != null) {

			try {

				int selectedIndex = list.getSelectionModel().getSelectedIndex();

				if (selectedIndex < 0) {
					return;
				}

				Node entry = elements.get(selectedIndex);

				Node copy = entry.copy();

				BufferedImage selectedImage = Misc.makeColorTransparent(ImageIO.read(selectedFile),
						new java.awt.Color(0xFF00FF, true));

				int[] pixels = ((DataBufferInt) selectedImage.getRaster().getDataBuffer()).getData();

				if (copy.getSprite() != null) {
					copy.getSprite().setWidth(selectedImage.getWidth());
					copy.getSprite().setHeight(selectedImage.getHeight());
					copy.getSprite().setPixels(pixels);
				}

				elements.remove(selectedIndex);

				elements.add(selectedIndex, copy);

			} catch (Exception ex) {
				Dialogue.showWarning("You have selected an invalid sprite. Valid sprites are png, jpg, or gif images.");
			}

		}

	}

	@FXML
	private void removeSprite() {
		List<Integer> selectedIndexes = this.list.getSelectionModel().getSelectedIndices();

		for (int selectedIndex : selectedIndexes) {
			if (selectedIndex < 0) {
				return;
			}

			Node entry = elements.get(selectedIndex);

			Node copy = entry.copy();

			if (copy.getSprite() != null) {
				copy.getSprite().setPixels(new int[0]);
			}

			if (selectedIndex == elements.size() - 1) {
				elements.remove(selectedIndex);
			} else {
				elements.remove(selectedIndex);

				elements.add(selectedIndex, copy);
			}

		}

	}

	private void loadSprites(Path path) throws IOException {
		clearEditor();

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() {

				final File[] files = new File(path.toString()).listFiles();

				final File[] sorted = Misc.sortImages(files);

				totalSprites = sorted.length;

				if (files == null || files.length <= 0) {
					return false;
				}

				if (sorted.length <= 0) {
					Platform.runLater(() -> {
						Dialogue.showInfo("Information", "No sprites were found.");
					});
					return false;
				}
				try {
					for (int index = 0; index < sorted.length; index++) {

						File file = sorted[index];

						if (file == null) {

							SpriteBase sprite = new SpriteBase(index);

							sprite.setPixels(new int[0]);

							Platform.runLater(() -> {
								elements.add(new Node(sprite.getId(), sprite));
							});

							updateProgress(index + 1, sorted.length);
							updateMessage("(" + (index + 1) + "/" + sorted.length + ")");

							continue;
						}

						BufferedImage image = Misc.convert(Misc.makeColorTransparent(ColorQuantizer.quantize(ImageIO.read(file)), Misc.fxColorToAWTColor(colorPicker.getValue())), BufferedImage.TYPE_INT_ARGB);

						int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

						if (data != null && data.length > 0) {
							SpriteBase sprite = new SpriteBase(index);

							sprite.setName(file.getName());
							sprite.setWidth(image.getWidth());
							sprite.setHeight(image.getHeight());
							sprite.setPixels(data);

							Platform.runLater(() -> {
								elements.add(new Node(sprite.getId(), sprite));
							});

						}
						updateProgress(index + 1, sorted.length);
						updateMessage("(" + (index + 1) + "/" + sorted.length + ")");

					}
				} catch (Exception ex) {
					Platform.runLater(() -> {
						Dialogue.showException("A problem was encountered while loading sprites.", ex);
					});
				}
				return true;
			}

		});

	}

	/**
	 * Reads the sequence of pixels in a given sprite archive and converts them
	 * into a sprite.
	 * 
	 * @param name
	 *            The name of this archive.
	 * 
	 * @param path
	 *            The path to this archive.
	 * 
	 * @throws Exception
	 *             The exception thrown.
	 */
	private void unpackSprites(String name, Path path) throws Exception {		
		clearEditor();

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				byte[] dat = Misc.readFile(path.toString());

				long start = System.currentTimeMillis();

				try (DataInputStream dataFile = new DataInputStream(
						new GZIPInputStream(new ByteArrayInputStream(dat)))) {

					totalSprites = dataFile.readInt();

					for (int index = 0; index < totalSprites; index++) {						

						SpriteBase sprite = SpriteDecoder.decode(dataFile);

						Platform.runLater(() -> {
							elements.add(new Node(sprite.getId(), sprite));
						});

						updateProgress(index + 1, totalSprites);
						updateMessage("(" + (index + 1) + "/" + totalSprites + ")");

					}

				}

				long end = System.currentTimeMillis();

				System.out.println("loaded in: " + (end - start) + " ms");
				
				Platform.runLater(() -> {
					Dialogue.showInfo("Information", "Successfully loaded sprite archives!");
				});				

				return true;
			}

		});

	}

	private void displaySprite(Node entry) throws Exception {
		try {
			final SpriteBase sprite = elements.get(entry.getId()).getSprite();

			BufferedImage image = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);

			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

			System.arraycopy(sprite.getPixels(), 0, pixels, 0, sprite.getPixels().length);

			newImage = SwingFXUtils.toFXImage(Misc.makeColorTransparent(image, new java.awt.Color(0xFF00FF, true)),
					null);

			imageView.setFitWidth(newImage.getWidth() > 512 ? 512 : newImage.getWidth());
			imageView.setFitHeight(newImage.getHeight() > 512 ? 512 : newImage.getHeight());

			imageView.setImage(newImage);
			Tooltip.install(imageView,
					new Tooltip("Width: " + newImage.getWidth() + " Height: " + newImage.getHeight()));
		} catch (Exception ex) {
			imageView.setImage(new Image(getClass().getResourceAsStream("/question_mark.png")));
		}
	}

	@FXML
	private void buildCache() {
		if (elements.isEmpty()) {
			Dialogue.showInfo("Information",
					"There are no sprites to write, load sprites before trying to create an archive.");
			return;
		}

		String archiveName = "sprites";

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select the directory to output your files to.");

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(currentDirectory);

		File selectedFile = chooser.showDialog(App.getMainStage());

		if (selectedFile == null) {
			return;
		}

		InputMessage inputDialog = Dialogue.showInput("Information", "Please enter a name for this sprite pack.",
				"sprites");

		Optional<String> result = inputDialog.showAndWait();

		if (!result.isPresent()) {
			return;
		}

		archiveName = result.get();

		final String tempArchiveName = archiveName;

		if (elements.size() == 0) {
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				try (DataOutputStream data = new DataOutputStream(new GZIPOutputStream(
						new FileOutputStream(Paths.get(selectedFile.getPath(), tempArchiveName + ".dat").toFile())))) {

					data.writeInt(elements.size());

					for (int index = 0; index < elements.size(); index++) {

						SpriteBase sprite = elements.get(index).getSprite();

						if (sprite.getId() == index) {
							SpriteEncoder.encode(data, sprite);
						} else {
							System.out.println("index: " + index + " does not match: " + sprite.getId());
						}

						updateProgress(index + 1, elements.size());
					}

				} catch (Exception ex) {
					Platform.runLater(() -> {
						Dialogue.showException("A problem was encountered while trying to write a sprite archive.", ex);
					});
					return false;
				}

				Platform.runLater(() -> {
					Dialogue.openDirectory("Success! Would you like to view this directory?", selectedFile);
				});

				return true;
			}

		});
	}

	@FXML
	private void viewCurrentDirectory() {

		if (!currentDirectory.exists()) {
			currentDirectory.mkdirs();
		}

		try {
			Desktop.getDesktop().open(currentDirectory);
		} catch (Exception ex) {
			Dialogue.showException("A problem was encountered while trying to view the current directory.", ex);
		}
	}

	private void createTask(Task<?> task) {

		new Thread(task).start();

		task.setOnSucceeded(e -> {

			PauseTransition pause = new PauseTransition(Duration.seconds(1));

			pause.setOnFinished(event -> {

			});

			pause.play();
		});

		task.setOnFailed(e -> {

			PauseTransition pause = new PauseTransition(Duration.seconds(1));

			pause.setOnFinished(event -> {

			});

			pause.play();

		});
	}

	@FXML
	private void credits() {
		Misc.launchURL("http://www.rune-server.org/members/free/");
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

	@FXML
	private void close() {
		System.exit(0);
	}

}
