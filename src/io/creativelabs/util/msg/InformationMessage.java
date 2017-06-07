package io.creativelabs.util.msg;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class InformationMessage extends Alert {
	
	private final Image icon = new Image(getClass().getResourceAsStream("/icons/info_icon.png"));

      public InformationMessage(String title, String content) {
            this(title, null, content);
      }

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
