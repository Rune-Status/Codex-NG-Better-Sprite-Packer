package io.creativelab;

import java.net.URL;
import java.util.ResourceBundle;

import com.creativelab.sprite.SpriteCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public final class Controller implements Initializable {

	final SpriteCache cache = SpriteCache.create();
	
	private double xOffset, yOffset;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
	
	@FXML
	private void pack() {
		
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
