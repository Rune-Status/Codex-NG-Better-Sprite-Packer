package io.creativelabs.util.msg;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class OptionMessage extends Alert {

	public OptionMessage(String header) {
		super(AlertType.CONFIRMATION);
		setTitle("Information");
		setHeaderText(header);
		setContentText("Choose your option.");
		
        Stage stage = (Stage) getDialogPane().getScene().getWindow();            
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/info_icon.png")));
	}

}
