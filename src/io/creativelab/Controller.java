package io.creativelab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.creativelab.sprite.SpriteCache;
import io.creativelab.util.Dialogue;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public final class Controller implements Initializable {

	final SpriteCache cache = SpriteCache.create();
	
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
					SpriteCache cache = SpriteCache.load(selectedDirectory);
					
					try(FileOutputStream fos = new FileOutputStream("./main_file_sprites.dat")) {
						fos.write(cache.encode());
					}
					
					Platform.runLater(() -> {
						Dialogue.openDirectory("Would you like to view this file?", new File("./"));
					});					
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
