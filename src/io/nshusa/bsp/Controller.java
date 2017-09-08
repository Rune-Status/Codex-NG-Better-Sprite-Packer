package io.nshusa.bsp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import io.nshusa.bsp.util.Dialogue;
import io.nshusa.rsam.binary.Archive;
import io.nshusa.rsam.binary.sprite.Sprite;
import io.nshusa.rsam.codec.ImageArchiveDecoder;
import io.nshusa.rsam.codec.SpriteDecoder;
import io.nshusa.rsam.codec.SpriteEncoder;
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

				SpriteEncoder.encode(selectedDirectory, 0);

				return true;
			}
		}).start();

	}

	@FXML
	private void unpack() {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.setTitle("Select a sprite archive file");

		File selectedFile = chooser.showOpenDialog(App.getMainStage());

		if (selectedFile == null) {
			return;
		}

		if (!selectedFile.getName().endsWith(".jag")) {
			selectedFile.renameTo(new File(selectedFile.getParentFile(), selectedFile.getName() + ".jag"));
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

		File outputDir = new File(selectedFile.getParentFile(), selectedFile.getName().lastIndexOf(".") != -1 ? selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf(".")) : selectedFile.getName());

		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		Archive archive = result.get();

		if (!archive.contains("index.dat")) {
			Dialogue.showWarning(String.format("archive=%s does not contain an index.dat", selectedFile.getName())).showAndWait();
			return;
		}

		new Thread(new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {

				final int indexHash = HashUtils.nameToHash("index.dat");

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

					List<Sprite> sprites = ImageArchiveDecoder.decode(ByteBuffer.wrap(archive.readFile(entry.getHash())), ByteBuffer.wrap(archive.readFile("index.dat")));

					if (sprites == null) {
						System.out.println("sprite is null");
						continue;
					}

					File imageArchiveDir = new File(outputDir, imageArchiveName);

					if (!imageArchiveDir.exists()) {
						imageArchiveDir.mkdirs();
					}

					for (int i = 0; i < sprites.size(); i++) {

						Sprite sprite = sprites.get(i);

						if (sprite == null) {
							continue;
						}

						ImageIO.write(sprite.toBufferedImage(), "png", new File(imageArchiveDir, Integer.toString(i) + ".png"));

					}

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

}
