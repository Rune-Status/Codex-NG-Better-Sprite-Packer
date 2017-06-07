package io.creativelabs.util;

import java.awt.Desktop;
import java.io.File;
import java.util.Optional;

import io.creativelabs.util.msg.ExceptionMessage;
import io.creativelabs.util.msg.InformationMessage;
import io.creativelabs.util.msg.InputMessage;
import io.creativelabs.util.msg.OptionMessage;
import io.creativelabs.util.msg.WarningMessage;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;

public final class Dialogue {
	
	private Dialogue() {
		
	}

	public static void openDirectory(String headerText, File dir) {

		OptionMessage alert = new OptionMessage(headerText);

		ButtonType choiceOne = new ButtonType("Yes.");
		ButtonType close = new ButtonType("No", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(choiceOne, close);

		Optional<ButtonType> result = alert.showAndWait();

		if (result.isPresent()) {

			ButtonType type = result.get();

			if (type == choiceOne) {
				try {
					Desktop.getDesktop().open(dir);
				} catch (Exception ex) {
					Dialogue.showException("Error while trying to view image on desktop.", ex);
				}
			}

		}
	}
	
	public static WarningMessage showWarning(String message) {
		return new WarningMessage(message);
	}

	public static InformationMessage showInfo(String title, String message) {
		return new InformationMessage(title, message);
	}

	public static ExceptionMessage showException(String message, Exception ex) {
		return new ExceptionMessage(message, ex);
	}
	
	public static InputMessage showInput(String title, String context, String text) {
		return new InputMessage(title, context, text);
	}

}
