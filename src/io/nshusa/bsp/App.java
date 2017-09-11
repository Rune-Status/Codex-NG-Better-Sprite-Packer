package io.nshusa.bsp;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import io.nshusa.bsp.util.SpritePackerUtils;
import io.nshusa.rsam.util.HashUtils;
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

	public static final Map<Integer, String> hashMap = new HashMap<>();

	public static final Map<String, Meta> offsetMap = new LinkedHashMap<>();

	@Override
	public void init() {
		try {

			new Thread(() -> {

				File cookieDir = new File(System.getProperty("user.home") + File.separator + ".rsam");

				if (!cookieDir.exists()) {
					cookieDir.mkdirs();
				}

				File hashFile = new File(cookieDir, "hashes.txt");

				if (!hashFile.exists()) {

					try(BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/377_hash_names.txt")))) {

						String line = null;

						while((line = reader.readLine()) != null) {

							if (line.lastIndexOf(".") != -1) { // non-hash with a file extension e.g	 '30.dat'
								App.hashMap.put(HashUtils.nameToHash(line), line);
							} else if(!line.isEmpty() && SpritePackerUtils.isNumeric(line.substring(1, line.length())) && line.length() < 12) { // hash e.g '-1548429542' or '4583276415'
								App.hashMap.put(Integer.parseInt(line), line);
							} else { // non-hash without a file extension 'midi_index'
								App.hashMap.put(HashUtils.nameToHash(line), line);
							}

						}
					} catch(IOException ex) {
						ex.printStackTrace();
					}

				} else {
					try (BufferedReader reader = new BufferedReader(new FileReader((hashFile)))) {

						String line = null;

						while ((line = reader.readLine()) != null) {

							String[] split = line.split(":");

							String name = split[0];

							int hash = -1;

							try {
								hash = Integer.parseInt(split[1]);
							} catch(NumberFormatException ex) {
								System.out.println(String.format("Could not parse %s as a valid hash.", split[1]));
								continue;
							}

							if (hash == -1) {
								continue;
							}

							hashMap.put(hash, name);

						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}).start();

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
									SpritePackerUtils.launchURL(App.properties.getProperty("creator_link"));
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
