package io.creativelabs.util.msg;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * The {@link Alert} implementation that creates a simplified Exception message.
 * 
 * @author Chad Adams
 */
public final class ExceptionMessage extends Alert {

      /**
       * Creates a new {@link ExceptionMessage}.
       * 
       * @param message
       *    The message to tell the user.
       *    
       * @param ex
       *    The exception to display.
       */
      public ExceptionMessage(String message, Exception ex) {
            super(AlertType.ERROR);
            setTitle("Exception");
            setHeaderText("Encountered an Exception");
            setContentText(message);

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            getDialogPane().setExpandableContent(expContent);

            showAndWait();
      }

}
