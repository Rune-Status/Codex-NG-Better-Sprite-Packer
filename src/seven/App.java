package seven;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * The main class which is the entry way to the application.
 * 
 * @author Seven
 */
public class App extends Application {
      
      /**
       * The main stage that will act as our window.
       */
      private static Stage mainStage;

      /**
       * The main entry way into our application.
       * 
       * @param args
       *    The command-line arguments.
       */
      public static void main(String[] args) {
            launch(args);
      }

      @Override
      public void start(Stage stage) throws Exception {
            App.mainStage = stage;
            Parent root = FXMLLoader.load(getClass().getResource("/seven/fxml/Main.fxml"));            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            stage.getIcons().add(new Image(App.class.getResourceAsStream("resources/icon.png")));
            stage.setTitle(String.format("%s v%.1f%n", Configuration.TITLE, Configuration.VERSION));
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
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
