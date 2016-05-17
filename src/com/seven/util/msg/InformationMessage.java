package com.seven.util.msg;

import javafx.scene.control.Alert;

/**
 * The {@link Alert} implementation that creates a simplified Information message.
 * 
 * @author Seven
 */
public class InformationMessage extends Alert {
      
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
            showAndWait();
      }

}
