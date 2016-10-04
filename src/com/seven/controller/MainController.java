package com.seven.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import com.seven.App;
import com.seven.Configuration;
import com.seven.model.Entry;
import com.seven.model.Sprite;
import com.seven.util.Dialogue;
import com.seven.util.FileUtils;
import com.seven.util.GenericUtils;
import com.seven.util.msg.InputMessage;

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
 * @author Seven
 */
public final class MainController implements Initializable {

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
	private Text indexT, widthT, heightT, drawOffsetXT, drawOffsetYT;

	@FXML
	private TextField searchTf, nameTf;

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

	private File currentDirectory;

	private int totalSprites;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		filteredSprites = new FilteredList<>(elements, it -> true);

		searchTf.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredSprites.setPredicate($it -> {

				if ($it.getSprite() == null) {
					return false;
				}

				Sprite sprite = $it.getSprite();

				if (newValue.isEmpty()) {
					return true;
				}

				if (sprite.getName().contains(newValue)) {
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
					try {
						
						if (value.getSprite() == null || value.getSprite().getData() == null || value.getSprite().getData().length == 0) {
							listIconView.setImage(new Image(getClass().getResourceAsStream("/question_mark.png")));
							listIconView.setFitHeight(32);
							listIconView.setFitWidth(32);
							setText(Integer.toString(value.getIndex()));
							setGraphic(listIconView);
							return;
						}
					
						final BufferedImage image = ImageIO.read(new ByteArrayInputStream(value.getSprite().getData()));
						
						listIconView.setFitHeight(image.getHeight() > 128 ? 128 : image.getHeight());
						listIconView.setFitWidth(image.getWidth() > 128 ? 128 : image.getWidth());
						listIconView.setPreserveRatio(true);

						newImage = SwingFXUtils.toFXImage(image, null);

						listIconView.setImage(newImage);
						setText(Integer.toString(value.getIndex()));
						setGraphic(listIconView);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		});

		list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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

						indexT.setText(Integer.toString(sprite.getIndex()));

						if (newImage != null) {
							widthT.setText(Double.toString(newImage.getWidth()));
							heightT.setText(Double.toString(newImage.getHeight()));
						}
						drawOffsetXT.setText(Integer.toString(sprite.getDrawOffsetX()));
						drawOffsetYT.setText(Integer.toString(sprite.getDrawOffsetY()));

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

        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(Configuration.VERSION_LINK).openStream()))) {
        	double version = Double.parseDouble(in.readLine().trim());
        	
        	if (Configuration.VERSION != version) {
        		Alert alert = new Alert(AlertType.CONFIRMATION);
        		alert.setTitle("Update");
        		alert.setHeaderText("Update " + version + " available");
        		alert.setContentText("Would you like to update to version: " + version + "?");

        		Optional<ButtonType> result = alert.showAndWait();
        		if (result.get() == ButtonType.OK){
        		   GenericUtils.launchURL(Configuration.CREATOR_LINK);
        		   System.exit(1);
        		}
        	}
        	
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
		
	}

	@FXML
	private void clearEditor() {
		elements.clear();
		filteredSprites.clear();
		list.getItems().clear();
		imageView.setImage(null);
	
		App.getMainStage().setTitle(String.format("%s v%.2f%n", Configuration.TITLE, Configuration.VERSION));
	}

	@FXML
	private void openSpriteDirectory() {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select the directory that contains your sprites.");

		String homePath = Configuration.CACHE_PATH;

		File file = new File(homePath);

		if (!file.exists()) {
			file = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(file);

		File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());

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
		
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select the directory that contains your sprite archive files.");

		File file = new File(Configuration.CACHE_PATH);

		if (!file.exists()) {
			file = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(file);
		File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());

		if (selectedDirectory != null) {

			currentDirectory = selectedDirectory;

			try {
				FileUtils.writeCachePathResource("cache_path.txt", selectedDirectory.getPath());
			} catch (IOException | URISyntaxException e) {
				Dialogue.showException("Was unable to save current directory.", e);
			}

			File[] files = selectedDirectory.listFiles();

			int count = 0;

			String archiveName = "sprites";

			for (File check : files) {
				if (check != null) {

					if (check.getName().contains("sprites.idx")) {
						archiveName = check.getName().replaceAll(".idx", "");
						count++;
					}

					if (check.getName().contains("sprites.dat")) {
						archiveName = check.getName().replaceAll(".dat", "");
						count++;
					}

					if (count == 2) {
						try {
							loadArchivedSprites(archiveName, selectedDirectory.toPath());
						} catch (Exception ex) {
							Dialogue.showException("A problem was encountered while loading the sprites archive.", ex);
							return;
						}

						Dialogue.showInfo("Information", "Successfully loaded sprite archives!");
						return;
					}
				}
			}
			Dialogue.showInfo("Information", "No sprite archives have been found.");
		}
	}

	@FXML
	private void handleKeyEventPressed(KeyEvent event) {
		if (event.getSource() == nameTf) {
			if (event.getCode() == KeyCode.ENTER) {
				if (!nameTf.getText().isEmpty() && nameTf.getText().length() <= 14) {

					Sprite sprite = elements.get(currentSpriteIndex).getSprite();

					sprite.setName(nameTf.getText());

					elements.set(currentSpriteIndex, new Entry(this.currentSpriteIndex, sprite));
					nameTf.clear();
				}
			}
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

		String homePath = Configuration.CACHE_PATH;

		File file = new File(homePath);

		if (!file.exists()) {
			file = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(file);

		File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());

		if (selectedDirectory != null) {

			currentDirectory = selectedDirectory;

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {

					Sprite sprite = elements.get(currentSpriteIndex).getSprite();

					if (sprite != null) {

						byte[] data = sprite.getData();

						try {
							final BufferedImage image = FileUtils.byteArrayToImage(data);

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

		String homePath = Configuration.CACHE_PATH;

		File file = new File(homePath);

		if (!file.exists()) {
			file = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(file);
		File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());

		if (selectedDirectory != null) {

			currentDirectory = selectedDirectory;

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {
					for (Entry entry : elements) {
						if (entry != null) {

							Sprite sprite = entry.getSprite();

							if (sprite != null) {

								byte[] data = sprite.getData();
								
								if (data.length == 0) {
									continue;
								}

								try {
									final BufferedImage image = FileUtils.byteArrayToImage(data);

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
		FileChooser fileChooser = new FileChooser();
		
		fileChooser.setInitialDirectory(this.currentDirectory == null ? new File(Configuration.CACHE_PATH) : this.currentDirectory);
		
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(App.getMainStage());
		if (selectedFile != null) {
			try {
				
				if (!selectedFile.isDirectory()) {
					this.currentDirectory = selectedFile.getParentFile();
				} else {
					this.currentDirectory = selectedFile;
				}
				
				ImageIO.read(selectedFile);
				
				Sprite sprite = new Sprite(FileUtils.fileToByteArray(selectedFile));
				
				Entry entry = new Entry(elements.size(), sprite);
				
				sprite.setIndex(elements.size());

				elements.add(entry);

			} catch (IOException ex) {
				Dialogue.showWarning("Could not read selected file as an image.");
			}
			
		}

	}
	
	@FXML
	private void replaceSprite() {
		if (elements.isEmpty()) {
			Dialogue.showWarning("There are no sprites to replace.");
			return;
		}
		
		int selectedIndex = this.list.getSelectionModel().getSelectedIndex();
		
		if (selectedIndex < 0) {
			Dialogue.showWarning("Select a sprite to replace.");
			return;
		}

		FileChooser fileChooser = new FileChooser();
		
		fileChooser.setInitialDirectory(this.currentDirectory == null ? new File(Configuration.CACHE_PATH) : this.currentDirectory);
		
		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(App.getMainStage());
		if (selectedFile != null) {
			
			try {
				ImageIO.read(selectedFile);				
				
				Entry entry = elements.get(selectedIndex);
				
				Entry copy = entry.copy();
				
				if (copy.getSprite() != null) {
					copy.getSprite().setData(FileUtils.fileToByteArray(selectedFile));					
				}
			
				elements.remove(selectedIndex);
				
				elements.add(selectedIndex, copy);				
				
			} catch (IOException ex) {
				Dialogue.showWarning("The file you selected is not a valid image.");
			}
			
		}		
		
	}

	@FXML
	private void removeSprite() {
		int selectedIndex = this.list.getSelectionModel().getSelectedIndex();

		if (selectedIndex < 0) {
			return;
		}
		
		Entry entry = elements.get(selectedIndex);
		
		Entry copy = entry.copy();
		
		if (copy.getSprite() != null) {
			copy.getSprite().setData(new byte[0]);
		}
	
		elements.remove(selectedIndex);
		
		elements.add(selectedIndex, copy);
		
	}

	private void loadSprites(Path path) throws IOException {
		clearEditor();
		
		final File[] files = new File(path.toString()).listFiles();
		
		int imageIndex = 0;
		
		int largest = 0;
		
		boolean valid = false;
		
		for (int index = 0; index < files.length; index++) {
			
			File file = files[index];
			
			if (file.getName().contains("png") || file.getName().contains("jpg") || file.getName().contains("gif")) {
				valid = true;
			}
			
			final String p = file.getName().replace(file.getName().substring(file.getName().lastIndexOf(".")), "");			
			
			try {
				imageIndex = Integer.parseInt(p);
				
				if (largest < imageIndex) {
					largest = imageIndex;
				}
			} catch (NumberFormatException ex) {
				continue;
			}
			
		}
		
		final File[] sorted = new File[largest + 1];

		totalSprites = sorted.length;

		if (files == null || files.length <= 0) {
			return;
		}
				
		for (int index = 0; index < sorted.length; index++) {
			
			File pngFile = new File(path.toString() + File.separator + index + ".png");
			
			File jpgFile = new File(path.toString() + File.separator + index + ".jpg");
			
			File gifFile = new File(path.toString() + File.separator + index + ".gif");
			
			if (pngFile.exists()) {
				sorted[index] = pngFile;
			} else if (jpgFile.exists()) {
				sorted[index] = jpgFile;
			} else if (gifFile.exists()) {
				sorted[index] = gifFile;
			} else {
				sorted[index] = null;
			}
			
		}

		if (valid) {

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() {

					try {
						for (int index = 0; index < sorted.length; index++) {
							
							File file = sorted[index];
							
							if (file == null) {
								
								Sprite sprite = new Sprite(new byte[0]);
								
								sprite.setIndex(index);	
								
								Platform.runLater(() -> {									
									elements.add(new Entry(sprite.getIndex(), sprite));
								});
								
								updateProgress(index + 1, sorted.length);
								updateMessage("(" + (index + 1) + "/" + sorted.length + ")");
								
								continue;
							}

							byte[] data = new byte[(int) file.length()];

							try (FileInputStream d = new FileInputStream(file)) {
								d.read(data);
							}

							if (data != null && data.length > 0) {
								Sprite sprite = new Sprite(data);
								
								sprite.setIndex(index);

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

		} else {
			Dialogue.showInfo("Information", "No sprites were found.");
		}

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

		byte[] idx = FileUtils.readFile(path.toString() + System.getProperty("file.separator") + name + ".idx");
		byte[] dat = FileUtils.readFile(path.toString() + System.getProperty("file.separator") + name + ".dat");

		DataInputStream indexFile = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(idx)));
		DataInputStream dataFile = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(dat)));

		totalSprites = indexFile.readInt();

		for (int index = 0; index < totalSprites; index++) {

			int id = indexFile.readInt();

			Sprite sprite = new Sprite();
			
			sprite.setIndex(id);

			sprite.decode(indexFile, dataFile);

			elements.add(new Entry(id, sprite));

		}

		App.getMainStage()
				.setTitle(String.format("%s v%.2f%n [%d]", Configuration.TITLE, Configuration.VERSION, totalSprites));

		indexFile.close();
		dataFile.close();
	}

	private void displaySprite(Entry entry) throws Exception {
		try {
		final Sprite sprite = elements.get(entry.getIndex()).getSprite();
		final BufferedImage image = ImageIO.read(new ByteArrayInputStream(sprite.getData()));
		newImage = SwingFXUtils.toFXImage(image, null);

		imageView.setFitWidth(newImage.getWidth() > 512 ? 512 : newImage.getWidth());
		imageView.setFitHeight(newImage.getHeight() > 512 ? 512 : newImage.getHeight());

		imageView.setImage(newImage);
		Tooltip.install(imageView, new Tooltip("Width: " + newImage.getWidth() + " Height: " + newImage.getHeight()));
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

		String homePath = Configuration.CACHE_PATH;

		File file = new File(homePath);

		if (!file.exists()) {
			file = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(file);

		File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());

		if (selectedDirectory != null) {

			try {
				FileUtils.writeCachePathResource("cache_path.txt", selectedDirectory.getPath());
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

		boolean successful = true;
		if (elements.size() != 0) {

			int index;

			try (DataOutputStream e = new DataOutputStream(new GZIPOutputStream(
					new FileOutputStream(Paths.get(selectedDirectory.getPath(), archiveName + ".dat").toFile())))) {
				
				for (index = 0; index < elements.size(); index++) {
					
					Sprite sprite = elements.get(index).getSprite();
					
					if (sprite.getIndex() == index) {
						if (sprite.getIndex() != -1) {
							e.writeByte(1);
							e.writeShort(sprite.getIndex());
						}

						if (sprite.getName() != null) {
							e.writeByte(2);
							e.writeUTF(sprite.getName());
						}

						if (sprite.getDrawOffsetX() != 0) {
							e.writeByte(3);
							e.writeShort(sprite.getDrawOffsetX());
						}

						if (sprite.getDrawOffsetY() != 0) {
							e.writeByte(4);
							e.writeShort(sprite.getDrawOffsetY());
						}

						if (sprite.getData() != null) {
							e.writeByte(5);
							e.write(sprite.getData(), 0, sprite.getData().length);
						}
						e.writeByte(0);
					} else {
						System.out.println("index: " + index + " does not match: " + sprite.getIndex());
					}
				}

			} catch (Exception ex) {
				successful = false;
				Dialogue.showException("A problem was encountered while trying to write a sprite archive.", ex);
			}

			try (DataOutputStream e = new DataOutputStream(new GZIPOutputStream(
					new FileOutputStream(Paths.get(selectedDirectory.getPath(), archiveName + ".idx").toFile())))) {
				
				e.writeInt(elements.size());
				
				for (Entry entry : elements) {
					e.writeInt(entry.getIndex());
					e.writeInt(entry.getSprite().getData().length);
				}

			} catch (IOException ex) {
				successful = false;
				Dialogue.showException("A problem was encountered while trying to write a sprite archive.", ex);
			}

			if (successful) {
				Platform.runLater(() -> {
					Dialogue.openDirectory("Success! Would you like to view this directory?", selectedDirectory);
				});
			}

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
		GenericUtils.launchURL("http://www.rune-server.org/members/seven/");
	}

	@FXML
	private void close() {
		System.exit(0);
	}

}
