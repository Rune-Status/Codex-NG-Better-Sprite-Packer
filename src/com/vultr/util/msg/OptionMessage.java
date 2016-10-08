package com.vultr.util.msg;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * The JavaFX confirmation dialogue.
 * 
 * @author Vult-R
 */
public final class OptionMessage extends Alert {

	/**
	 * Creates a new {@link OptionDialogue}.
	 * 
	 * @param header
	 * 		The text to display
	 */
	public OptionMessage(String header) {
		super(AlertType.CONFIRMATION);
		setTitle("Information");
		setHeaderText(header);
		setContentText("Choose your option.");
		
        Stage stage = (Stage) getDialogPane().getScene().getWindow();            
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/info_icon.png")));
	}

}
