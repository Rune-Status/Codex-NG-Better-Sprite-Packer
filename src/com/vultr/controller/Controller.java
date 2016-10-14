package com.vultr.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import com.vultr.App;
import com.vultr.Configuration;
import com.vultr.codec.decoder.SpriteDecoder;
import com.vultr.codec.encoder.SpriteEncoder;
import com.vultr.model.Entry;
import com.vultr.model.Sprite;
import com.vultr.util.Dialogue;
import com.vultr.util.FileUtils;
import com.vultr.util.GenericUtils;
import com.vultr.util.ImageUtils;
import com.vultr.util.msg.InputMessage;

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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

/**
 * The class that is an association with the Main.FXML document.
 * 
 * @author Vult-R
 */
public final class Controller implements Initializable {

	@FXML
	private ListView<Entry> list;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private Button writeSprite, dumpSprite, loadSprite, loadArchive, clearBtn, resizeBtn;

	@FXML
	private ImageView imageView;

	@FXML
	private MenuItem openMI, openArchiveMI, creditMI, closeMI;

	@FXML
	private Text indexT, widthT, heightT;

	@FXML
	private TextField searchTf, nameTf, offsetXTf, offsetYTf;

	@FXML
	private TitledPane titledPane;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private ProgressIndicator progressI;

	@FXML
	private Text progressText, progressBarText;

	@FXML
	private MenuItem dumpSpriteMI, dumpAllSpritesMI, viewDirectoryMI;

	private FilteredList<Entry> filteredSprites;

	private ObservableList<Entry> elements = FXCollections.observableArrayList();

	private int currentSpriteIndex = 0;

	private Image newImage;

	private File currentDirectory = new File(System.getProperty("user.home"));

	private int totalSprites;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		filteredSprites = new FilteredList<>(elements, it -> true);

		searchTf.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredSprites.setPredicate(it -> {				

				if (it.getSprite() == null) {
					return false;
				}

				Sprite sprite = it.getSprite();

				if (newValue.isEmpty()) {
					return true;
				}				

				if (sprite.getName().toLowerCase().contains(newValue)) {
					return true;
				}

				if (Integer.toString(sprite.getIndex()).contains(newValue)) {
					return true;
				}

				return false;
			});
		});

		list.setItems(this.filteredSprites);

		list.setCellFactory(param -> new ListCell<Entry>() {

			private final ImageView listIconView = new ImageView();

			@Override
			public void updateItem(Entry value, boolean empty) {
				super.updateItem(value, empty);

				if (empty) {
					setGraphic(null);
					setText("");
				} else {

					if (value.getSprite() == null || value.getSprite().getData() == null
							|| value.getSprite().getData().length == 0) {
						listIconView.setImage(new Image(getClass().getResourceAsStream("/question_mark.png")));
						listIconView.setFitHeight(32);
						listIconView.setFitWidth(32);
						setText(Integer.toString(value.getIndex()));
						setGraphic(listIconView);
						return;
					}

					BufferedImage image = new BufferedImage(value.getSprite().getWidth(), value.getSprite().getHeight(),
							BufferedImage.TYPE_INT_ARGB);

					final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

					System.arraycopy(value.getSprite().getData(), 0, pixels, 0, value.getSprite().getData().length);

					listIconView.setFitHeight(image.getHeight() > 128 ? 128 : image.getHeight());
					listIconView.setFitWidth(image.getWidth() > 128 ? 128 : image.getWidth());
					listIconView.setPreserveRatio(true);

					newImage = SwingFXUtils.toFXImage(
							ImageUtils.makeColorTransparent(image, new java.awt.Color(0xFF00FF, true)), null);

					listIconView.setImage(newImage);
					setText(Integer.toString(value.getIndex()));
					setGraphic(listIconView);

				}
			}

		});

		list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Entry>() {
			@Override
			public void changed(ObservableValue<? extends Entry> observable, Entry oldValue, Entry newValue) {

				if (newValue != null) {

					try {
						currentSpriteIndex = newValue.getIndex();

						final Sprite sprite = newValue.getSprite();

						if (!sprite.getName().equalsIgnoreCase("None")) {
							nameTf.setText(sprite.getName());
						} else {
							nameTf.clear();
						}
						
						System.out.println(sprite.getIndex() + " " + sprite.getName());

						indexT.setText(Integer.toString(sprite.getIndex()));

						if (newImage != null) {
							widthT.setText(Double.toString(newImage.getWidth()));
							heightT.setText(Double.toString(newImage.getHeight()));
						}

						offsetXTf.setText(Integer.toString(sprite.getDrawOffsetX()));
						offsetYTf.setText(Integer.toString(sprite.getDrawOffsetY()));

						displaySprite(newValue);

						App.getMainStage().sizeToScene();

					} catch (Exception ex) {
						Dialogue.showException("A problem was encountered while trying to display a sprite.", ex);
					}
				}
			}
		});

		final Image clearImage = new Image(getClass().getResourceAsStream("/clear.png"));
		final Image saveArchiveImage = new Image(getClass().getResourceAsStream("/saveArchive.png"));
		final Image loadSpriteImage = new Image(getClass().getResourceAsStream("/loadSprite.png"));
		final Image loadArchiveImage = new Image(getClass().getResourceAsStream("/loadArchive.png"));
		final Image saveSpritesImage = new Image(getClass().getResourceAsStream("/saveSprites.png"));

		clearBtn.setGraphic(new ImageView(clearImage));
		writeSprite.setGraphic(new ImageView(saveArchiveImage));
		loadSprite.setGraphic(new ImageView(loadSpriteImage));
		loadArchive.setGraphic(new ImageView(loadArchiveImage));
		dumpSprite.setGraphic(new ImageView(saveSpritesImage));

		if (!Configuration.DEBUG) {
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(new URL(Configuration.VERSION_LINK).openStream()))) {
				double version = Double.parseDouble(in.readLine().trim());

				if (Configuration.VERSION != version) {
					
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Update");
					alert.setHeaderText("Update " + version + " available");
					alert.setContentText("Would you like to update to version: " + version + "?");

					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.OK) {
						GenericUtils.launchURL(Configuration.CREATOR_LINK);
						System.exit(1);
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

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
		indexT.setText("");
		widthT.setText("");
		heightT.setText("");

		App.getMainStage().setTitle(String.format("%s v%.2f%n", Configuration.TITLE, Configuration.VERSION));
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

				try {
					FileUtils.writeCachePathResource("cache_path.txt", selectedFile.getPath());
				} catch (IOException | URISyntaxException e) {
					Dialogue.showException("Was unable to save current directory.", e);
				}
			}

			String archiveName = "sprites";

			try {

				loadArchivedSprites(archiveName, selectedFile.toPath());

				Dialogue.showInfo("Information", "Successfully loaded sprite archives!");

			} catch (Exception ex) {
				Dialogue.showException("A problem was encountered while loading the sprites archive.", ex);
				return;
			}

		}
	}

	@FXML
	private void handleKeyEventPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {

			Sprite sprite = elements.get(currentSpriteIndex).getSprite();

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

			elements.set(currentSpriteIndex, new Entry(this.currentSpriteIndex, sprite));

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

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {

					Sprite sprite = elements.get(currentSpriteIndex).getSprite();

					if (sprite != null) {

						int[] pixels = sprite.getData();

						try {
							final BufferedImage image = new BufferedImage(sprite.getWidth(), sprite.getHeight(),
									BufferedImage.TYPE_INT_ARGB);

							int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

							System.arraycopy(pixels, 0, data, 0, pixels.length);

							ImageIO.write(image, "png", Paths
									.get(selectedDirectory.toString(), Integer.toString(sprite.getIndex()) + ".png")
									.toFile());
						} catch (IOException ex) {
							ex.printStackTrace();
						}

						updateProgress(sprite.getIndex() + 1, elements.size());
						updateMessage("(" + (sprite.getIndex() + 1) + "/" + elements.size() + ")");

						Platform.runLater(() -> {
							Dialogue.openDirectory("Success! Would you like to view this directory?",
									selectedDirectory);
						});

					}

					return true;
				}

			});
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

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {
					for (Entry entry : elements) {
						if (entry != null) {

							Sprite sprite = entry.getSprite();

							if (sprite != null) {

								int[] pixels = sprite.getData();

								if (pixels.length == 0) {
									continue;
								}

								try {
									final BufferedImage image = new BufferedImage(sprite.getWidth(), sprite.getHeight(),
											BufferedImage.TYPE_INT_ARGB);

									int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

									System.arraycopy(pixels, 0, data, 0, pixels.length);

									ImageIO.write(image, "png", Paths.get(selectedDirectory.toString(),
											Integer.toString(entry.getIndex()) + ".png").toFile());
								} catch (IOException ex) {
									ex.printStackTrace();
								}
								updateProgress(entry.getIndex() + 1, elements.size());
								updateMessage("(" + (entry.getIndex() + 1) + "/" + elements.size() + ")");
							}
						}
					}

					Platform.runLater(() -> {
						Dialogue.openDirectory("Success! Would you like to view this directory?", selectedDirectory);
					});

					return true;
				}

			});
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
						this.currentDirectory = selectedFile.getParentFile();
					}

					BufferedImage image = ImageUtils.makeColorTransparent(
							ImageUtils.convert(ImageIO.read(selectedFile), BufferedImage.TYPE_INT_ARGB),
							new java.awt.Color(0xFF00FF, true));

					int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

					Sprite sprite = new Sprite(elements.size());

					sprite.setName(selectedFile.getName());					
					sprite.setWidth(image.getWidth());
					sprite.setHeight(image.getHeight());
					sprite.setData(pixels);

					Entry entry = new Entry(elements.size(), sprite);

					elements.add(entry);

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
				this.currentDirectory == null ? new File(Configuration.CACHE_PATH) : this.currentDirectory);

		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(App.getMainStage());

		if (selectedFile != null) {

			try {

				int selectedIndex = list.getSelectionModel().getSelectedIndex();

				if (selectedIndex < 0) {
					return;
				}

				Entry entry = elements.get(selectedIndex);

				Entry copy = entry.copy();

				BufferedImage selectedImage = ImageUtils.makeColorTransparent(
						ImageUtils.convert(ImageIO.read(selectedFile), BufferedImage.TYPE_INT_ARGB),
						new java.awt.Color(0xFF00FF, true));

				int[] pixels = ((DataBufferInt) selectedImage.getRaster().getDataBuffer()).getData();

				if (copy.getSprite() != null) {
					copy.getSprite().setWidth(selectedImage.getWidth());
					copy.getSprite().setHeight(selectedImage.getHeight());
					copy.getSprite().setData(pixels);
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

			Entry entry = elements.get(selectedIndex);

			Entry copy = entry.copy();

			if (copy.getSprite() != null) {
				copy.getSprite().setData(new int[0]);
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

		final File[] files = new File(path.toString()).listFiles();

		final File[] sorted = FileUtils.sortImages(files);

		totalSprites = sorted.length;

		if (files == null || files.length <= 0) {
			return;
		}

		if (sorted.length <= 0) {
			Dialogue.showInfo("Information", "No sprites were found.");
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() {

				try {
					for (int index = 0; index < sorted.length; index++) {

						File file = sorted[index];

						if (file == null) {

							Sprite sprite = new Sprite(index);

							sprite.setData(new int[0]);

							Platform.runLater(() -> {
								elements.add(new Entry(sprite.getIndex(), sprite));
							});

							updateProgress(index + 1, sorted.length);
							updateMessage("(" + (index + 1) + "/" + sorted.length + ")");

							continue;
						}

						BufferedImage image = ImageUtils.makeColorTransparent(ImageUtils.convert(ImageIO.read(file), BufferedImage.TYPE_INT_ARGB), java.awt.Color.WHITE);
						
						int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

						if (data != null && data.length > 0) {
							Sprite sprite = new Sprite(index);
							
							sprite.setName(file.getName());
							sprite.setWidth(image.getWidth());
							sprite.setHeight(image.getHeight());
							sprite.setData(data);

							Platform.runLater(() -> {
								elements.add(new Entry(sprite.getIndex(), sprite));
							});

						}
						updateProgress(index + 1, sorted.length);
						updateMessage("(" + (index + 1) + "/" + sorted.length + ")");

						Platform.runLater(() -> {
							App.getMainStage().setTitle(String.format("%s v%.2f%n [%d]", Configuration.TITLE,
									Configuration.VERSION, elements.size()));
						});

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
	private void loadArchivedSprites(String name, Path path) throws Exception {
		clearEditor();

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				byte[] dat = FileUtils.readFile(path.toString());

				try (DataInputStream dataFile = new DataInputStream(
						new GZIPInputStream(new ByteArrayInputStream(dat)))) {

					totalSprites = dataFile.readInt();

					for (int index = 0; index < totalSprites; index++) {

						Sprite sprite = SpriteDecoder.decode(dataFile);

						Platform.runLater(() -> {
							elements.add(new Entry(sprite.getIndex(), sprite));
						});

						updateProgress(index + 1, totalSprites);
						updateMessage("(" + (index + 1) + "/" + totalSprites + ")");

					}

				}

				Platform.runLater(() -> {
					App.getMainStage().setTitle(
							String.format("%s v%.2f%n [%d]", Configuration.TITLE, Configuration.VERSION, totalSprites));
				});

				return true;
			}

		});

	}

	private void displaySprite(Entry entry) throws Exception {
		try {
			final Sprite sprite = elements.get(entry.getIndex()).getSprite();

			BufferedImage image = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);

			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

			System.arraycopy(sprite.getData(), 0, pixels, 0, sprite.getData().length);

			newImage = SwingFXUtils
					.toFXImage(ImageUtils.makeColorTransparent(image, new java.awt.Color(0xFF00FF, true)), null);

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

		if (selectedFile != null) {

			try {
				FileUtils.writeCachePathResource("cache_path.txt", currentDirectory.getPath());
			} catch (IOException | URISyntaxException e) {
				Dialogue.showException("Was unable to save current directory.", e);
			}

			InputMessage inputDialog = Dialogue.showInput("Information", "Please enter the name of the archives:",
					"sprites");

			Optional<String> result2 = inputDialog.showAndWait();

			if (result2.isPresent()) {
				archiveName = result2.get();
			} else {
				return;
			}
		} else {
			return;
		}

		final String tempArchiveName = archiveName;

		if (elements.size() != 0) {

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {

					boolean successful = true;

					try (DataOutputStream e = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(
							Paths.get(selectedFile.getPath(), tempArchiveName + ".dat").toFile())))) {

						e.writeInt(elements.size());

						for (int index = 0; index < elements.size(); index++) {

							Sprite sprite = elements.get(index).getSprite();

							if (sprite.getIndex() == index) {
								SpriteEncoder.encode(e, sprite);
							} else {
								System.out.println("index: " + index + " does not match: " + sprite.getIndex());
							}

							updateProgress(index + 1, elements.size());
							updateMessage("(" + (index + 1) + "/" + elements.size() + ")");

						}

					} catch (Exception ex) {
						successful = false;
						Platform.runLater(() -> {
							Dialogue.showException("A problem was encountered while trying to write a sprite archive.",
									ex);
						});

					}

					if (successful) {
						Platform.runLater(() -> {
							Dialogue.openDirectory("Success! Would you like to view this directory?", selectedFile);
						});
					}
					return true;
				}

			});
		}
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

		progressBar.setVisible(true);
		progressI.setVisible(true);

		progressBar.progressProperty().unbind();
		progressBar.progressProperty().bind(task.progressProperty());

		progressBarText.textProperty().unbind();
		progressBarText.textProperty().bind(task.messageProperty());

		progressI.progressProperty().unbind();
		progressI.progressProperty().bind(task.progressProperty());

		progressText.setText("In Progress");
		progressText.setFill(Color.WHITE);

		new Thread(task).start();

		task.setOnSucceeded(e -> {

			progressText.setText("Complete");
			progressText.setFill(Color.GREEN);

			PauseTransition pause = new PauseTransition(Duration.seconds(1));

			pause.setOnFinished(event -> {
				progressBar.setVisible(false);
				progressI.setVisible(false);
				progressText.setText("");

				progressBarText.textProperty().unbind();
				progressBarText.setText("");
			});

			pause.play();
		});

		task.setOnFailed(e -> {

			progressText.setText("Failed");
			progressText.setFill(Color.RED);

			PauseTransition pause = new PauseTransition(Duration.seconds(1));

			pause.setOnFinished(event -> {
				progressBar.setVisible(false);
				progressI.setVisible(false);
				progressText.setText("");

				progressBarText.textProperty().unbind();
				progressBarText.setText("");
			});

			pause.play();

		});
	}

	@FXML
	private void credits() {
		GenericUtils.launchURL("http://www.rune-server.org/members/free/");
	}

	@FXML
	private void close() {
		System.exit(0);
	}

}
