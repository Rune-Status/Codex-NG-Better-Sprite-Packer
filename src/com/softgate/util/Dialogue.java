package com.softgate.util;

import java.awt.Desktop;
import java.io.File;
import java.util.Optional;

import com.softgate.util.msg.ExceptionMessage;
import com.softgate.util.msg.InformationMessage;
import com.softgate.util.msg.InputMessage;
import com.softgate.util.msg.OptionMessage;
import com.softgate.util.msg.WarningMessage;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;

/**
 * The class that contains methods for displaying JavaFX dialogues.
 * 
 * @author Chad Adams
 */
public final class Dialogue {
	
	private Dialogue() {
		
	}
	
	/**
	 * Displays a directory to a user.
	 * 
	 * @param headerText
	 * 		The header text for this dialogue.
	 * 
	 * @param dir
	 * 		The path to open.
	 */
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
	
	/**
	 * Displays an information message to the user.
	 * 
	 * @param title
	 * 		The title text.
	 * 
	 * @param message
	 * 		The message to tell the user.
	 */
	public static InformationMessage showInfo(String title, String message) {
		return new InformationMessage(title, message);
	}
	
	/**
	 * Displays an exception to the user.
	 * 
	 * @param message
	 * 		The message to tell the user.
	 * 
	 * @param ex
	 * 		The exception to display.
	 */
	public static ExceptionMessage showException(String message, Exception ex) {
		return new ExceptionMessage(message, ex);
	}
	
	public static InputMessage showInput(String title, String context, String text) {
		return new InputMessage(title, context, text);
	}

}
