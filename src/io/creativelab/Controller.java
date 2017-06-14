package io.creativelab;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
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

	File currentDirectory = new File(System.getProperty("user.home"));

	@FXML
	ColorPicker colorPicker;

	@FXML
	BorderPane root;

	@FXML
	VBox rightVBox;

	Image emptyIcon;

	private double xOffset, yOffset;

	SpriteCache cache = SpriteCache.create();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		emptyIcon = new Image(App.class.getResourceAsStream("/icons/question_mark.png"));

		TreeItem<Node> rootTI = new TreeItem<Node>(new Node(-1, "Cache"));

		rootTI.setExpanded(true);

		treeView.setRoot(rootTI);

		colorPicker.setValue(Color.FUCHSIA);

		treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
			
			if (newValue.getValue().isSpriteNode()) {
				
				SpriteNode spriteNode = (SpriteNode) newValue.getValue();
				
				try {

					ImageView view = (ImageView) newValue.getGraphic();

					Image image = view.getImage();

					imageView.setFitWidth(image.getWidth() > 256 ? 256 : image.getWidth());
					imageView.setFitHeight(image.getHeight() > 256 ? 256 : image.getHeight());
					imageView.setPreserveRatio(true);

					imageView.setImage(view.getImage());

					indexTf.setText(Integer.toString(spriteNode.getId()));
					widthTf.setText(Double.toString(image.getWidth()));
					heightTf.setText(Double.toString(image.getHeight()));
					nameTf.setText(spriteNode.getName());
					offsetXTf.setText(Integer.toString(spriteNode.getOffsetX()));
					offsetYTf.setText(Integer.toString(spriteNode.getOffsetY()));

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

					int archiveIndex = 0;
					
					for (ImageArchive imageArchive : cache.getImageArchives()) {

						TreeItem<Node> imageArchiveTI = new TreeItem<>(new Node(archiveIndex, Integer.toString(imageArchive.getHash())));

						for (SpriteBase sprite : imageArchive.getSprites()) {

							BufferedImage bimage = sprite.toBufferedImage();

							Image image = SwingFXUtils
									.toFXImage(Misc.makeColorTransparent(ColorQuantizer.quantize(bimage),
											Misc.fxColorToAWTColor(colorPicker.getValue())), null);

							ImageView imageView = new ImageView(image);

							imageView.setFitWidth(image.getWidth() > 128 ? 128 : image.getWidth());
							imageView.setFitHeight(image.getHeight() > 128 ? 128 : image.getHeight());
							imageView.setPreserveRatio(true);

							TreeItem<Node> spriteTI = new TreeItem<>(new SpriteNode(sprite.getId(), Integer.toString(sprite.getId())), imageView);

							imageArchiveTI.getChildren().add(spriteTI);

						}

						Platform.runLater(() -> {
							treeView.getRoot().getChildren().add(imageArchiveTI);
						});
						
						archiveIndex++;

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
					
					int archiveIndex = 0;

					for (ImageArchive archive : cache.getImageArchives()) {

						TreeItem<Node> archiveTI = new TreeItem<>(new Node(archiveIndex, Integer.toString(archive.getHash())));
						
						List<SpriteNode> nodes = new ArrayList<>();

						for (SpriteBase sprite : archive.getSprites()) {
							nodes.add(new SpriteNode(sprite.getId(), Integer.toString(sprite.getId())).setName(sprite.getName()).setOffsetX(sprite.getDrawOffsetX()).setOffsetY(sprite.getDrawOffsetY()).setbImage(sprite.toBufferedImage()));
						}						
						
						Collections.sort(nodes);
						
						for (SpriteNode node : nodes) {
							
							BufferedImage bimage = node.getbImage();

							ImageView imageView = new ImageView(SwingFXUtils.toFXImage(bimage, null));

							imageView.setFitWidth(bimage.getWidth() > 128 ? 128 : bimage.getWidth());
							imageView.setFitHeight(bimage.getHeight() > 128 ? 128 : bimage.getHeight());
							imageView.setPreserveRatio(true);
							
						}

						Platform.runLater(() -> {
							treeView.getRoot().getChildren().add(archiveTI);
						});
						
						archiveIndex++;

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

			if (!selectedTI.getValue().isSpriteNode()) {
				return;
			}
			
			SpriteNode spriteNode = (SpriteNode) selectedTI.getValue();

			if (!nameTf.getText().isEmpty() && nameTf.getText().length() <= 23) {
				spriteNode.setName(nameTf.getText());
			}

			if (!offsetXTf.getText().isEmpty() && offsetXTf.getText().length() < 3) {
				spriteNode.setOffsetX(Integer.parseInt(offsetXTf.getText()));
			}

			if (!offsetYTf.getText().isEmpty() && offsetYTf.getText().length() < 3) {
				spriteNode.setOffsetY(Integer.parseInt(offsetYTf.getText()));
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

						File parentDir = new File(selectedDir, selectedItem.getValue().getDisplayName());

						if (!parentDir.exists()) {
							parentDir.mkdirs();
						}

						for (TreeItem<Node> nodeTI : selectedItem.getChildren()) {

							// root
							if (!nodeTI.isLeaf()) {

								File childDir = new File(parentDir, nodeTI.getValue().getDisplayName());

								if (!childDir.exists()) {
									childDir.mkdirs();
								}

								for (TreeItem<Node> spriteTI : nodeTI.getChildren()) {

									BufferedImage bimage = SwingFXUtils
											.fromFXImage(((ImageView) spriteTI.getGraphic()).getImage(), null);

									try {
										ImageIO.write(bimage, "png",
												new File(childDir, spriteTI.getValue().getId() + ".png"));
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
											new File(parentDir, nodeTI.getValue().getId() + ".png"));
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
									new File(selectedDir, selectedItem.getValue().getId() + ".png"));
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
	private void remove() {

		List<TreeItem<Node>> selectedItems = treeView.getSelectionModel().getSelectedItems();

		for (TreeItem<Node> selected : selectedItems) {
			
			TreeItem<Node> parent = selected.getParent();

			// selected the root
			if (parent == null) {
				treeView.getRoot().getChildren().clear();
				break;
			}

			// selected a directory
			if (selected.getGraphic() == null) {
				parent.getChildren().remove(selected);
				continue;
			}
			
			// selected an image
			final int id = selected.getValue().getId();

			if (id == parent.getChildren().size() - 1) {

				parent.getChildren().remove(id);

			} else {

				ImageView imageView = new ImageView(emptyIcon);

				imageView.setFitWidth(32);
				imageView.setFitHeight(32);
				imageView.setPreserveRatio(true);

				parent.getChildren().add(id, new TreeItem<Node>(new SpriteNode(id, Integer.toString(id)), imageView));

				parent.getChildren().remove(id + 1);

			}

		}

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

					ImageArchive archive = ImageArchive.create(imageArchiveTI.getValue().getId());

					for (TreeItem<Node> spriteTI : imageArchiveTI.getChildren()) {

						ImageView imageView = (ImageView) spriteTI.getGraphic();

						if (imageView == null) {
							continue;
						}

						Image image = imageView.getImage();

						SpriteBase sprite = SpriteBase.convert(SwingFXUtils.fromFXImage(image, null));
						
						SpriteNode spriteNode = (SpriteNode) spriteTI.getValue();

						sprite.setId(spriteTI.getValue().getId());
						sprite.setName(spriteNode.getName());
						sprite.setDrawOffsetX(spriteNode.getOffsetX());
						sprite.setDrawOffsetY(spriteNode.getOffsetY());

						archive.add(sprite);

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
