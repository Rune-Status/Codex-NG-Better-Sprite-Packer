package com.softgate.util.msg;

import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class InputMessage extends TextInputDialog {
	
	public InputMessage(String title, String context, String text) {
		super(text);
		this.setTitle(title);
		this.setHeaderText(null);
		this.setContentText(context);
		
        Stage stage = (Stage) getDialogPane().getScene().getWindow();            
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/info_icon.png")));
	}

}
