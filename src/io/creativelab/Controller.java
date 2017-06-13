package io.creativelab;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.creativelab.sprite.ImageArchive;
import com.creativelab.sprite.SpriteBase;
import com.creativelab.sprite.SpriteCache;
import com.creativelab.util.ColorQuantizer;

import io.creativelab.util.Dialogue;
import io.creativelab.util.Misc;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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
	TreeView<Node> treeView;

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

	Image newImage;

	File currentDirectory = new File(System.getProperty("user.home"));

	@FXML
	ColorPicker colorPicker;

	@FXML
	BorderPane root;

	@FXML
	VBox rightVBox;

	private double xOffset, yOffset;

	SpriteCache cache = SpriteCache.create();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		TreeItem<Node> rootTI = new TreeItem<Node>(new Node("Cache"));

		rootTI.setExpanded(true);

		treeView.setRoot(rootTI);

		colorPicker.setValue(Color.FUCHSIA);

		treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue.isLeaf()) {

				try {

					ImageView view = (ImageView) newValue.getGraphic();

					Image image = view.getImage();

					imageView.setFitWidth(image.getWidth() > 256 ? 256 : image.getWidth());
					imageView.setFitHeight(image.getHeight() > 256 ? 256 : image.getHeight());
					imageView.setPreserveRatio(true);

					imageView.setImage(view.getImage());

					indexTf.setText(newValue.getValue().getName());
					widthTf.setText(Double.toString(image.getWidth()));
					heightTf.setText(Double.toString(image.getHeight()));
					nameTf.setText(newValue.getValue().getSpriteName());
					offsetXTf.setText("" + newValue.getValue().getDrawOffsetX());
					offsetYTf.setText("" + newValue.getValue().getDrawOffsetY());

				} catch (Exception ex) {

				}

			}
		});
	}

	@FXML
	private void clearEditor() {
		treeView.getRoot().getChildren().clear();
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
	private void loadFromDirectory() {

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select the directory that contains your sprites.");

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		chooser.setInitialDirectory(currentDirectory);

		File selectedDirectory = chooser.showDialog(App.getMainStage());

		if (selectedDirectory == null) {
			return;
		}

		currentDirectory = selectedDirectory;

		clearEditor();

		try {

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {
					SpriteCache cache = SpriteCache.load(selectedDirectory);

					for (ImageArchive imageArchive : cache.getImageArchives()) {

						TreeItem<Node> imageArchiveTI = new TreeItem<>(new Node("" + imageArchive.getHash()));

						for (SpriteBase sprite : imageArchive.getSprites()) {

							BufferedImage bimage = sprite.toBufferedImage();

							Image image = SwingFXUtils
									.toFXImage(Misc.makeColorTransparent(ColorQuantizer.quantize(bimage),
											Misc.fxColorToAWTColor(colorPicker.getValue())), null);

							ImageView imageView = new ImageView(image);

							imageView.setFitWidth(image.getWidth() > 128 ? 128 : image.getWidth());
							imageView.setFitHeight(image.getHeight() > 128 ? 128 : image.getHeight());
							imageView.setPreserveRatio(true);

							TreeItem<Node> spriteTI = new TreeItem<>(new Node("" + sprite.getId()), imageView);

							imageArchiveTI.getChildren().add(spriteTI);

						}

						Platform.runLater(() -> {
							treeView.getRoot().getChildren().add(imageArchiveTI);
						});

					}
					return true;
				}

			});

		} catch (Exception ex) {
			Dialogue.showException("A problem was encountered when opening the sprites directory.", ex);
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
	private void unpackArchive() {

		FileChooser fileChooser = new FileChooser();

		fileChooser.setInitialDirectory(currentDirectory);

		fileChooser.setTitle("Open Resource File");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Data files", "*.dat"));

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		fileChooser.setInitialDirectory(currentDirectory);

		File selectedFile = fileChooser.showOpenDialog(App.getMainStage());

		if (selectedFile == null) {
			return;
		}

		if (!selectedFile.isDirectory()) {
			currentDirectory = selectedFile.getParentFile();
		}

		try {

			clearEditor();

			createTask(new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {

					SpriteCache cache = SpriteCache.decode(Files.readAllBytes(selectedFile.toPath()));

					for (ImageArchive archive : cache.getImageArchives()) {

						TreeItem<Node> archiveTI = new TreeItem<>(new Node("" + archive.getHash()));

						for (SpriteBase sprite : archive.getSprites()) {

							BufferedImage bimage = sprite.toBufferedImage();

							ImageView imageView = new ImageView(SwingFXUtils.toFXImage(bimage, null));

							imageView.setFitWidth(bimage.getWidth() > 128 ? 128 : bimage.getWidth());
							imageView.setFitHeight(bimage.getHeight() > 128 ? 128 : bimage.getHeight());
							imageView.setPreserveRatio(true);

							archiveTI.getChildren()
									.add(new TreeItem<Node>(new Node("" + sprite.getId())
											.setSpriteName(sprite.getName()).setDrawOffsetX(sprite.getDrawOffsetX())
											.setDrawOffsetY(sprite.getDrawOffsetY()), imageView));

						}

						Platform.runLater(() -> {
							treeView.getRoot().getChildren().add(archiveTI);
						});

					}

					return true;
				}

			});

		} catch (Exception ex) {
			Dialogue.showException("A problem was encountered while loading the sprites archive.", ex);
			return;
		}

	}

	@FXML
	private void handleKeyEventPressed(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {

			TreeItem<Node> selectedTI = treeView.getSelectionModel().getSelectedItem();

			if (!selectedTI.isLeaf()) {
				return;
			}

			if (!nameTf.getText().isEmpty() && nameTf.getText().length() <= 23) {
				selectedTI.getValue().setSpriteName(nameTf.getText());
			}

			if (!offsetXTf.getText().isEmpty() && offsetXTf.getText().length() < 3) {
				selectedTI.getValue().setDrawOffsetX(Integer.parseInt(offsetXTf.getText()));
			}

			if (!offsetYTf.getText().isEmpty() && offsetYTf.getText().length() < 3) {
				selectedTI.getValue().setDrawOffsetY(Integer.parseInt(offsetYTf.getText()));
			}

			selectedTI.setValue(selectedTI.getValue().copy());

		}
	}

	@FXML
	private void dumpSprite() {

	}

	@FXML
	private void dumpSprites() {

		if (treeView.getRoot().getChildren().isEmpty()) {
			Dialogue.showInfo("Information", "There are no sprites to dump.");
			return;
		}

		DirectoryChooser directoryChooser = new DirectoryChooser();

		directoryChooser.setInitialDirectory(currentDirectory);

		directoryChooser.setTitle("Open Resource File");

		if (!currentDirectory.isDirectory()) {
			currentDirectory = new File(System.getProperty("user.home"));
		}

		directoryChooser.setInitialDirectory(currentDirectory);

		File selectedDir = directoryChooser.showDialog(App.getMainStage());

		if (selectedDir == null) {
			return;
		}

		if (!selectedDir.isDirectory()) {
			Dialogue.showInfo("Information", "Please select a valid directory.");
			return;
		}

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				List<TreeItem<Node>> selectedItems = treeView.getSelectionModel().getSelectedItems();

				for (TreeItem<Node> selectedItem : selectedItems) {

					if (!selectedItem.isLeaf()) {

						File parentDir = new File(selectedDir, selectedItem.getValue().getName());

						if (!parentDir.exists()) {
							parentDir.mkdirs();
						}

						for (TreeItem<Node> nodeTI : selectedItem.getChildren()) {

							// root
							if (!nodeTI.isLeaf()) {

								File childDir = new File(parentDir, nodeTI.getValue().getName());

								if (!childDir.exists()) {
									childDir.mkdirs();
								}

								for (TreeItem<Node> spriteTI : nodeTI.getChildren()) {

									BufferedImage bimage = SwingFXUtils
											.fromFXImage(((ImageView) spriteTI.getGraphic()).getImage(), null);

									try {
										ImageIO.write(bimage, "png",
												new File(childDir, spriteTI.getValue().getName() + ".png"));
									} catch (IOException e) {
										e.printStackTrace();
										continue;
									}

								}

							} else {

								BufferedImage bimage = SwingFXUtils
										.fromFXImage(((ImageView) nodeTI.getGraphic()).getImage(), null);

								try {
									ImageIO.write(bimage, "png",
											new File(parentDir, nodeTI.getValue().getName() + ".png"));
								} catch (IOException e) {
									e.printStackTrace();
									continue;
								}

							}

						}
					} else {

						BufferedImage bimage = SwingFXUtils
								.fromFXImage(((ImageView) selectedItem.getGraphic()).getImage(), null);

						try {
							ImageIO.write(bimage, "png",
									new File(selectedDir, selectedItem.getValue().getName() + ".png"));
						} catch (IOException e) {
							e.printStackTrace();
						}

					}

				}

				Platform.runLater(() -> {
					Dialogue.openDirectory("Would you like to view these sprites?", selectedDir);
				});

				return true;
			}

		});

	}

	@FXML
	private void addSprite() {

	}

	@FXML
	private void replaceSprite() {

	}

	@FXML
	private void removeSprite() {

	}

	@FXML
	private void buildCache() {

		if (treeView.getRoot().getChildren().isEmpty()) {
			Dialogue.showInfo("Information", "No sprites to pack.");
			return;
		}

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

		createTask(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				final SpriteCache cache = SpriteCache.create();

				for (TreeItem<Node> imageArchiveTI : treeView.getRoot().getChildren()) {

					ImageArchive archive = ImageArchive.create(Integer.parseInt(imageArchiveTI.getValue().getName()));

					for (TreeItem<Node> spriteTI : imageArchiveTI.getChildren()) {

						ImageView imageView = (ImageView) spriteTI.getGraphic();

						if (imageView == null) {
							continue;
						}

						Image image = imageView.getImage();
						
						SpriteBase sprite = SpriteBase.convert(SwingFXUtils.fromFXImage(image, null));
						
						sprite.setId(Integer.parseInt(spriteTI.getValue().getName()));	
						sprite.setName(spriteTI.getValue().getSpriteName());
						sprite.setDrawOffsetX(spriteTI.getValue().getDrawOffsetX());
						sprite.setDrawOffsetY(spriteTI.getValue().getDrawOffsetY());

						archive.add(sprite);

						System.out.println(imageArchiveTI.getValue().getName() + " " + spriteTI.getValue().getName());						

					}

					cache.add(archive);

				}

				try (FileOutputStream fos = new FileOutputStream(new File(selectedFile, "main_file_sprites.dat"))) {
					fos.write(cache.encode());
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
