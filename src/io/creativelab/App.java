package io.creativelab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import com.creativelab.util.SpritePackerUtils;

import io.creativelab.util.Misc;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

	private static Stage mainStage;
	
	public static final Properties properties = new Properties();
	
	public static final Map<Integer, String> names = new HashMap<>();
	
	static {
		
		names.put(SpritePackerUtils.nameToHash("default"), "default");
		
		File resource = new File("./names.txt");
		
		if (resource.exists() && !resource.isDirectory()) {
			try(BufferedReader reader = new BufferedReader(new FileReader(resource))) {
				
				String line = null;
				
				while((line = reader.readLine()) != null) {
					names.put(SpritePackerUtils.nameToHash(line.trim()), line.trim());
				}			
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void init() {
		try {
			properties.load(App.class.getResourceAsStream("/settings.properties"));
			
			final CountDownLatch latch = new CountDownLatch(1);
			
			final FutureTask<Boolean> query = new FutureTask<>(new Callable<Boolean>() {
				
			    @Override
			    public Boolean call() throws Exception {
			    	
					if (!Boolean.parseBoolean(App.properties.getProperty("debug"))) {
						try (BufferedReader in = new BufferedReader(
								new InputStreamReader(new URL(App.properties.getProperty("version_link")).openStream()))) {
							String version = in.readLine().trim();

							if (!App.properties.getProperty("version").equalsIgnoreCase(version)) {

								Alert alert = new Alert(AlertType.CONFIRMATION);
								alert.setTitle("Update");
								alert.setHeaderText("Update " + version + " available");
								alert.setContentText("Would you like to update to version: " + version + "?");

								Optional<ButtonType> result = alert.showAndWait();
								if (result.get() == ButtonType.OK) {
									Misc.launchURL(App.properties.getProperty("creator_link"));									
									System.exit(1);
								}
								
								latch.countDown();
							} else {
								latch.countDown();
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					
			        return true;
			    }
			    
			});
			
			Platform.runLater(query);
			
			try {
				latch.await();
			} catch (InterruptedException e) {
				Platform.exit();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		App.mainStage = stage;
		Parent root = FXMLLoader.load(getClass().getResource("/ui/Main.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon.png")));
		stage.setTitle(String.format("%s", properties.getProperty("title")));
		stage.centerOnScreen();
		stage.setResizable(false);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.initStyle(StageStyle.UNDECORATED);		
		stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}	

	public static Stage getMainStage() {
		return mainStage;
	}

}
