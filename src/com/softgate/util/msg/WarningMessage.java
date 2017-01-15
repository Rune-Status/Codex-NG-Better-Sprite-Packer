package com.softgate.util.msg;

import javafx.scene.control.Alert;

public final class WarningMessage extends Alert {

	public WarningMessage(String message) {
		super(AlertType.WARNING);
		setTitle("Warning");
		setHeaderText(null);
		setContentText(message);
		
		showAndWait();
	}

}
