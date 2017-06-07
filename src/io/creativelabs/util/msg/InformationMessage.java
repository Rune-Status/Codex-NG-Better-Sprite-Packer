package io.creativelabs.util.msg;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * The {@link Alert} implementation that creates a simplified Information message.
 * 
 * @author Chad Adams
 */
public final class InformationMessage extends Alert {
	
		private final Image icon = new Image(getClass().getResourceAsStream("/info_icon.png"));
      
      /**
       * Creates a new {@link InformationMessage}.
       * 
       * @param title
       *    The title text to display.
       *    
       * @param content
       *    The content text to display.
       */
      public InformationMessage(String title, String content) {
            this(title, null, content);
      }

      /**
       * Creates a new {@link InformationMessage}.
       * 
       * @param title
       *    The title text to display.
       *    
       * @param header
       *    The header text to display.
       *    
       * @param content
       *    The content text to display.
       */
      public InformationMessage(String title, String header, String content) {
            super(AlertType.INFORMATION);
            setTitle(title);
            setHeaderText(header);
            setContentText(content);
            Stage stage = (Stage) getDialogPane().getScene().getWindow();            
            stage.getIcons().add(icon);
            showAndWait();
      }

}
