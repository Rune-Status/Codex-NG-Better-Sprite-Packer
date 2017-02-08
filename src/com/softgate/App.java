package com.softgate;

import java.io.IOException;
import java.net.URISyntaxException;

import com.softgate.util.FileUtils;
import com.softgate.util.msg.ExceptionMessage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The main class which is the entry way to the application.
 * 
 * @author Chad Adams
 */
public class App extends Application {

	/**
	 * The main stage that will act as our window.
	 */
	private static Stage mainStage;

	@Override
	public void init() {
		try {
			FileUtils.readCachePathResource("bsp.txt");
		} catch (IOException e) {
			try {
				FileUtils.writeCachePathResource("bsp.txt", System.getProperty("user.home"));
			} catch (IOException | URISyntaxException e1) {
				new ExceptionMessage("Could not create resource for current directory!", e1);
			}
		}
	}

	/**
	 * The main entry way into our application.
	 * 
	 * @param args
	 *            The command-line arguments.
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		App.mainStage = stage;
		Parent root = FXMLLoader.load(getClass().getResource("/com/softgate/ui/Main.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
		stage.setTitle(String.format("%s v%.2f%n", Configuration.TITLE, Configuration.VERSION));
		stage.centerOnScreen();
		stage.setResizable(false);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.initStyle(StageStyle.UNDECORATED);		
		stage.show();
	}

	/**
	 * Gets the main stage of this application.
	 * 
	 * @return The main stage.
	 */
	public static Stage getMainStage() {
		return mainStage;
	}

}
