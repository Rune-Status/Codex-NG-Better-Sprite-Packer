package io.creativelab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.creativelab.sprite.ImageArchive;
import com.creativelab.sprite.SpriteBase;
import com.creativelab.sprite.SpriteCache;
import com.creativelab.util.SpritePackerUtils;

import io.creativelab.util.Dialogue;
import io.creativelab.util.Misc;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public final class Controller implements Initializable {

	@FXML
	ColorPicker colorPicker;
	
	private double xOffset, yOffset;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		colorPicker.setValue(Color.MAGENTA);
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
					SpriteCache cache = SpriteCache.load(Misc.fxColorToAWTColor(colorPicker.getValue()), selectedDirectory);

					try (FileOutputStream fos = new FileOutputStream("./" + selectedDirectory.getName() + ".dat")) {
						fos.write(cache.encode());
					}

					Platform.runLater(() -> {
						Dialogue.openDirectory("Would you like to view this file?", new File("./"));
					});

					Arrays.stream(selectedDirectory.listFiles()).filter(it -> it.isDirectory())
							.forEach(it -> App.names.put(SpritePackerUtils.nameToHash(it.getName()), it.getName()));

					try (PrintWriter writer = new PrintWriter(new FileWriter(new File("./names.txt")))) {
						App.names.values().forEach(it -> writer.println(it));
					}

				} catch (IOException e) {
					Platform.runLater(() -> {
						Dialogue.showException("An error occurred", e).showAndWait();
					});
				}
				return true;
			}
		}).start();

	}

	@FXML
	private void unpack() {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.setTitle("Open Resource File");
		chooser.getExtensionFilters().addAll(new ExtensionFilter("Data files", "*.dat"));

		File selectedFile = chooser.showOpenDialog(App.getMainStage());

		if (selectedFile == null) {
			return;
		}

		new Thread(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try {
					byte[] bytes = Files.readAllBytes(selectedFile.toPath());

					if (bytes.length < 3) {
						Platform.runLater(
								() -> Dialogue.showWarning("Detected wrong file type or corrupt data.").showAndWait());
					}

					if (bytes[0] != 'b' && bytes[1] != 's' && bytes[2] != 'p') {
						Platform.runLater(() -> Dialogue.showWarning("Detected wrong file type.").showAndWait());
					}

					final SpriteCache cache = SpriteCache.decode(bytes);

					File root = new File("./",
							selectedFile.getName().substring(0, selectedFile.getName().indexOf(".")));

					if (!root.exists()) {
						root.mkdirs();
					}

					for (ImageArchive archive : cache.getImageArchives()) {

						File archiveDir = new File(root, App.names.get(archive.getHash()) == null
								? Integer.toString(archive.getHash()) : App.names.get(archive.getHash()));

						if (!archiveDir.exists()) {
							archiveDir.mkdirs();
						}

						for (SpriteBase sprite : archive.getSprites()) {
							ImageIO.write(Misc.createColoredBackground(sprite.toBufferedImage(), Misc.fxColorToAWTColor(colorPicker.getValue())), "png",
									new File(archiveDir, sprite.getId() + ".png"));
						}
					}

					Platform.runLater(() -> Dialogue.openDirectory("Would you like to view this directory?", root));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}

		}).start();

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

	@FXML
	private void close() {
		System.exit(0);
	}

}
