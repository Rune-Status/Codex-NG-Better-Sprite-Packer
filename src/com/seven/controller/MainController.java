package com.seven.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import com.seven.App;
import com.seven.Configuration;
import com.seven.sprite.Sprite;
import com.seven.util.FileUtils;
import com.seven.util.GenericUtils;
import com.seven.util.msg.ExceptionMessage;
import com.seven.util.msg.InformationMessage;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

/**
 * The class that is an association with the Main.FXML document.
 * 
 * @author Seven
 */
public final class MainController implements Initializable {

      @FXML
      private ListView<Integer> list;

      @FXML
      private ScrollPane scrollPane;

      @FXML
      private Button writeSprite, dumpSprite, loadSprite, loadArchive, clearBtn, resizeBtn;

      @FXML
      private ImageView imageView;

      @FXML
      private MenuItem openMI, openArchiveMI, creditMI, closeMI;

      @FXML
      private Text indexT, widthT, heightT, drawOffsetXT, drawOffsetYT;

      @FXML
      private TextField searchTf, nameTf;

      @FXML
      private TitledPane titledPane;

      @FXML
      private ProgressBar progressBar;

      @FXML
      private ProgressIndicator progressI;

      @FXML
      private Text progressText;
      
      @FXML
      private MenuItem dumpSpriteMI, dumpAllSpritesMI, viewDirectoryMI;

      public static final List<Sprite> SPRITES = new ArrayList<>();

      private FilteredList<Integer> filteredSprites;

      private SortedList<Integer> sortedList;

      private ObservableList<Integer> elements = FXCollections.observableArrayList();

      private int currentSpriteIndex = 0;

      private Image newImage;
      
      private File currentDirectory;

      @Override
      public void initialize(URL location, ResourceBundle resources) {

            filteredSprites = new FilteredList<>(elements, it -> true);

            searchTf.textProperty().addListener((observable, oldValue, newValue) -> {
                  filteredSprites.setPredicate($it -> {

                        if (newValue == null || newValue.isEmpty()) {
                              return true;
                        }

                        if (newValue.equalsIgnoreCase(Integer.toString($it))) {
                              return true;
                        }
                        return false;
                  });
            });

            sortedList = new SortedList<>(filteredSprites);
            sortedList.setComparator(new Comparator<Integer>() {

                  @Override
                  public int compare(Integer oldValue, Integer newValue) {
                        return oldValue.compareTo(newValue);
                  }

            });

            list.setItems(sortedList);

            list.setCellFactory(param -> new ListCell<Integer>() {

                  private final ImageView listIconView = new ImageView();

                  @Override
                  public void updateItem(Integer value, boolean empty) {
                        super.updateItem(value, empty);

                        if (empty) {
                              setGraphic(null);
                        } else {
                              BufferedImage image;
                              try {
                                    image = ImageIO.read(new ByteArrayInputStream(
                                                SPRITES.get(value).getData()));

                                    listIconView.setFitHeight(
                                                image.getHeight() > 128 ? 128 : image.getHeight());
                                    listIconView.setFitWidth(
                                                image.getWidth() > 128 ? 128 : image.getWidth());
                                    listIconView.setPreserveRatio(true);

                                    newImage = SwingFXUtils.toFXImage(image, null);

                                    listIconView.setImage(newImage);
                                    setText(Integer.toString(value));
                                    setGraphic(listIconView);
                              } catch (IOException e) {
                                    e.printStackTrace();
                              }
                        }
                  }

            });

            list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            list.getSelectionModel().selectedItemProperty()
                        .addListener(new ChangeListener<Integer>() {
                              @Override
                              public void changed(ObservableValue<? extends Integer> observable,
                                          Integer oldValue, Integer newValue) {

                                    if (newValue != null) {

                                          try {
                                                currentSpriteIndex = newValue.intValue();
                                                Sprite sprite = SPRITES.get(newValue.intValue());
                                                if (!sprite.getName().equalsIgnoreCase("Unknown")) {
                                                      nameTf.setText(sprite.getName());
                                                } else {
                                                      nameTf.clear();
                                                }

                                                indexT.setText(Integer.toString(sprite.getIndex()));

                                                if (newImage != null) {
                                                      widthT.setText(Double
                                                                  .toString(newImage.getWidth()));
                                                      heightT.setText(Double
                                                                  .toString(newImage.getHeight()));
                                                }
                                                drawOffsetXT.setText(Integer
                                                            .toString(sprite.getDrawOffsetX()));
                                                drawOffsetYT.setText(Integer
                                                            .toString(sprite.getDrawOffsetY()));

                                                displaySprite(newValue.intValue());
                                          } catch (Exception ex) {
                                                new ExceptionMessage(
                                                            "A problem was encountered while trying to display a sprite.",
                                                            ex);
                                          }
                                    }
                              }
                        });

            titledPane.heightProperty().addListener((obs, oldHeight, newHeight) -> resizeStage());

            Image clearImage = new Image(getClass().getResourceAsStream("/clear.png"));
            Image saveArchiveImage = new Image(getClass().getResourceAsStream("/saveArchive.png"));
            Image loadSpriteImage = new Image(getClass().getResourceAsStream("/loadSprite.png"));
            Image loadArchiveImage = new Image(getClass().getResourceAsStream("/loadArchive.png"));
            Image saveSpritesImage = new Image(getClass().getResourceAsStream("/saveSprites.png"));

            clearBtn.setGraphic(new ImageView(clearImage));
            writeSprite.setGraphic(new ImageView(saveArchiveImage));
            loadSprite.setGraphic(new ImageView(loadSpriteImage));
            loadArchive.setGraphic(new ImageView(loadArchiveImage));
            dumpSprite.setGraphic(new ImageView(saveSpritesImage));
      }

      @FXML
      private void clearEditor() {
            elements.clear();
            SPRITES.clear();
            imageView.setImage(null);
            App.getMainStage().setTitle(
                        String.format("%s v%.1f%n", Configuration.TITLE, Configuration.VERSION));
      }

      @FXML
      private void openSpriteDirectory() {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select the directory that contains your sprites.");
            String homePath = System.getProperty("user.home");
            File file = new File(homePath);
            chooser.setInitialDirectory(file);
            File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());
            
            if (selectedDirectory != null) {
                  
                  currentDirectory = selectedDirectory;
                  
                  try {
                        loadSprites(selectedDirectory.toPath());
                  } catch (Exception ex) {
                        new ExceptionMessage(
                                    "A problem was encountered when opening the sprites directory.",
                                    ex);
                  }
            }
      }

      @FXML
      private void resizeStage() {
            App.getMainStage().sizeToScene();
      }

      @FXML
      private void openArchiveDirectory() {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select the directory that contains your sprite archive files.");
            String homePath = System.getProperty("user.home");
            File file = new File(homePath);
            chooser.setInitialDirectory(file);
            File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());
            
            if (selectedDirectory != null) {
                  
                  currentDirectory = selectedDirectory;
                  
                  File[] files = selectedDirectory.listFiles();

                  int count = 0;

                  String archiveName = "sprites";

                  for (File check : files) {
                        if (check != null) {

                              if (check.getName().contains("sprites.idx")) {
                                    archiveName = check.getName().replaceAll(".idx", "");
                                    count++;
                              }

                              if (check.getName().contains("sprites.dat")) {
                                    archiveName = check.getName().replaceAll(".dat", "");
                                    count++;
                              }


                              if (count == 2) {
                                    elements.clear();
                                    try {
                                          FileUtils.loadArchivedSprites(archiveName,
                                                      selectedDirectory.toPath());
                                    } catch (Exception ex) {
                                          new ExceptionMessage(
                                                      "A problem was encountered while loading the sprites archive.",
                                                      ex);
                                          return;
                                    }
                                    populateList();
                                    new InformationMessage("Information", null,
                                                "Successfully loaded sprite archives!");
                                    return;
                              }
                        }
                  }
                  new InformationMessage("Information", null,
                              "No sprite archives have been found.");
            }
      }

      @FXML
      private void handleKeyEventPressed(KeyEvent event) {
            if (event.getSource() == nameTf) {
                  if (event.getCode() == KeyCode.ENTER) {
                        Sprite sprite = SPRITES.get(currentSpriteIndex);
                        if (!nameTf.getText().isEmpty()) {
                              sprite.setName(nameTf.getText());
                              nameTf.clear();
                              new InformationMessage("Information", null,
                                          "You have change the name of this sprite.");
                        }
                  }
            }
      }

      @FXML
      private void dumpSprite() {
            
            if (SPRITES.isEmpty()) {
                  new InformationMessage("Information", null, "There are no sprites to write.");
                  return;
            }
            
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select the directory to place the sprites in.");
            String homePath = System.getProperty("user.home");
            File file = new File(homePath);
            chooser.setInitialDirectory(file);
            File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());
             
            if (selectedDirectory != null) {
                  
                  currentDirectory = selectedDirectory;
                  
                  createTask(new Task<Boolean>() {

                        @Override
                        protected Boolean call() throws Exception {
                              
                              Sprite sprite = SPRITES.get(currentSpriteIndex);
                              
                              if (sprite != null) {                              
                              
                                    byte[] data = sprite.getData();

                                    try {
                                          final BufferedImage image = FileUtils.byteArrayToImage(data);

                                          ImageIO.write(image, "png", Paths.get(selectedDirectory.toString(), Integer.toString(sprite.getIndex()) + ".png").toFile());
                                    } catch (IOException ex) {                                          
                                          ex.printStackTrace();
                                    }
                                    
                                    updateProgress(sprite.getIndex() + 1, SPRITES.size());
                              
                              Platform.runLater(() -> {
                                    openDirectoryDialog("Success! Would you like to view this sprite?", selectedDirectory);
                              });
                              
                              }
                              
                              return true;
                        }

                  });
            }
      }
      
      @FXML
      private void dumpSprites() {

            if (SPRITES.isEmpty()) {
                  new InformationMessage("Information", null, "There are no sprites to write.");
                  return;
            }

            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select the directory to place the sprites in.");
            String homePath = System.getProperty("user.home");
            File file = new File(homePath);
            chooser.setInitialDirectory(file);
            File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());           
            
            if (selectedDirectory != null) {
                  
                  currentDirectory = selectedDirectory;

                  createTask(new Task<Boolean>() {

                        @Override
                        protected Boolean call() throws Exception {
                              for (Sprite sprite : SPRITES) {
                                    if (sprite != null) {
                                    byte[] data = sprite.getData();

                                    try {
                                          final BufferedImage image = FileUtils.byteArrayToImage(data);

                                          ImageIO.write(image, "png", Paths.get(selectedDirectory.toString(), Integer.toString(sprite.getIndex()) + ".png").toFile());
                                    } catch (IOException ex) {                                          
                                          ex.printStackTrace();
                                    }
                                    updateProgress(sprite.getIndex() + 1, SPRITES.size());
                                    }
                              }
                              
                              Platform.runLater(() -> {
                                    openDirectoryDialog("Success! Would you like to view these sprites?", selectedDirectory);
                              });
                              
                              return true;
                        }

                  });
            }
            
      }
      
      private void openDirectoryDialog(String headerText, File dir) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Information");
            alert.setHeaderText(headerText);
            alert.setContentText("Choose your option.");

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
                              new ExceptionMessage("Error while trying to view image on desktop.",
                                          ex);
                        }
                  }

            }
      }

      private void populateList() {
            int size = FileUtils.totalArchivedSprites;
            for (int index = 0; index < size; index++) {
                  elements.add(index);
            }
            App.getMainStage().setTitle(String.format("%s v%.1f%n [%d]", Configuration.TITLE,
                        Configuration.VERSION, size));
      }

      private void loadSprites(Path path) throws IOException {
            this.elements.clear();

            File[] files = (new File(path.toString())).listFiles();
            FileUtils.totalSprites = files.length;

            File[] sortedFiles = new File[files.length];

            for (File file : files) {
                  if (file != null) {

                        String p = file.getName().replaceAll(".png", "").replaceAll(".PNG", "");

                        int index = Integer.valueOf(p);

                        sortedFiles[index] = file;
                  }
            }
            
            createTask(new Task<Boolean>() {

                  @Override
                  protected Boolean call() throws Exception {
                        for (int index = 0; index < sortedFiles.length; index++) {
                              byte[] data = new byte[(int) sortedFiles[index].length()];
                              try {
                                    FileInputStream d = new FileInputStream(sortedFiles[index]);
                                    d.read(data);
                                    d.close();
                              } catch (Exception ex) {
                                    new ExceptionMessage("A problem was encountered while loading sprites.",
                                                ex);
                              }
                              if (data != null && data.length > 0) {
                                    Sprite sprite = new Sprite(index, data);
                                    
                                    
                                    SPRITES.add(sprite);
                                    
                                    Platform.runLater(() -> {
                                          elements.add(sprite.getIndex());
                                    });
                                    
                              }
                              updateProgress(index + 1, sortedFiles.length);
                        }
                        
                        Platform.runLater(() -> {
                              App.getMainStage().setTitle(String.format("%s v%.1f%n [%d]", Configuration.TITLE,
                                          Configuration.VERSION, SPRITES.size()));
                        });
                        
                        return true;
                  }
                  
            });
            

      }

      private void displaySprite(int index) throws Exception {
            Sprite sprite = SPRITES.get(index);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(sprite.getData()));
            newImage = SwingFXUtils.toFXImage(image, null);

            imageView.setImage(newImage);
            Tooltip.install(imageView, new Tooltip(
                        "Width: " + newImage.getWidth() + " Height: " + newImage.getHeight()));
      }

      @FXML
      private void buildCache() {
            if (SPRITES.isEmpty()) {
                  new InformationMessage("Information", null,
                              "There are no sprites to write, load sprites before trying to create an archive.");
                  return;
            }

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Information");
            alert.setHeaderText("Where would you like to output these files?");
            alert.setContentText("Choose your option.");

            ButtonType buttonTypeOne = new ButtonType("I would like the default output.");
            ButtonType buttonTypeTwo = new ButtonType("I would like to choose my own output.");
            ButtonType buttonTypeThree = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree);

            String output = "";

            String archiveName = "sprites";

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeOne) {
                  output = "./";

                  TextInputDialog inputDialog = new TextInputDialog("sprites");
                  inputDialog.setTitle("Information");
                  inputDialog.setHeaderText(null);
                  inputDialog.setContentText("Please enter the name of the archives:");

                  Optional<String> result2 = inputDialog.showAndWait();

                  if (result2.isPresent()) {
                        archiveName = result2.get();
                  } else {
                        return;
                  }
            } else if (result.get() == buttonTypeTwo) {
                  DirectoryChooser chooser = new DirectoryChooser();
                  chooser.setTitle("Select the directory to output your files to.");
                  String homePath = System.getProperty("user.home");
                  File file = new File(homePath);
                  chooser.setInitialDirectory(file);
                  File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());
                  if (selectedDirectory != null) {
                        output = selectedDirectory.toPath().toString();

                        TextInputDialog inputDialog = new TextInputDialog("sprites");
                        inputDialog.setTitle("Information");
                        inputDialog.setHeaderText(null);
                        inputDialog.setContentText("Please enter the name of the archives:");

                        Optional<String> result2 = inputDialog.showAndWait();

                        if (result2.isPresent()) {
                              archiveName = result2.get();
                        } else {
                              return;
                        }
                  } else {
                        return;
                  }
            } else {
                  return;

            }

            boolean successful = true;
            if (SPRITES.size() != 0) {
                  DataOutputStream e;
                  int index;
                  try {
                        e = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(
                                    Paths.get(output, archiveName + ".dat").toFile())));

                        for (index = 0; index < SPRITES.size(); index++) {
                              Sprite sprite = SPRITES.get(index);
                              if (sprite.getIndex() == index) {
                                    if (sprite.getIndex() != -1) {
                                          e.writeByte(1);
                                          e.writeShort(sprite.getIndex());
                                    }

                                    if (sprite.getName() != null) {
                                          e.writeByte(2);
                                          e.writeUTF(sprite.getName());
                                    }

                                    if (sprite.getDrawOffsetX() != 0) {
                                          e.writeByte(3);
                                          e.writeShort(sprite.getDrawOffsetX());
                                    }

                                    if (sprite.getDrawOffsetY() != 0) {
                                          e.writeByte(4);
                                          e.writeShort(sprite.getDrawOffsetY());
                                    }

                                    if (this.grabSpriteBytes(index) != null
                                                && this.grabSpriteBytes(index).length > 0) {
                                          e.writeByte(5);
                                          e.write(grabSpriteBytes(index), 0,
                                                      grabSpriteBytes(index).length);
                                    }

                                    e.writeByte(0);
                              }
                        }

                        e.flush();
                        e.close();
                  } catch (Exception ex) {
                        successful = false;
                        new ExceptionMessage(
                                    "A problem was encountered while trying to write a sprite archive.",
                                    ex);
                  }

                  try {
                        e = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(
                                    Paths.get(output, archiveName + ".idx").toFile())));
                        e.writeInt(SPRITES.size());

                        for (index = 0; index < SPRITES.size(); ++index) {
                              e.writeInt((SPRITES.get(index)).getIndex());
                              e.writeInt((SPRITES.get(index)).getData().length);
                        }

                        e.flush();
                        e.close();
                  } catch (Exception ex) {
                        successful = false;
                        new ExceptionMessage(
                                    "A problem was encountered while trying to write a sprite archive.",
                                    ex);
                  }

                  if (successful) {
                        new InformationMessage("Information", "Successful!");
                  }

            }
      }
      
      @FXML
      private void viewCurrentDirectory() {
            
            if (!currentDirectory.exists()) {
                 currentDirectory.mkdirs(); 
            }
            
            try {
                  Desktop.getDesktop().open(currentDirectory);
            } catch (Exception ex) {
                  new ExceptionMessage(
                              "A problem was encountered while trying to view the current directory.",
                              ex);
            }
      }

      private void createTask(Task<?> task) {

            progressBar.setVisible(true);
            progressI.setVisible(true);

            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(task.progressProperty());

            progressI.progressProperty().unbind();
            progressI.progressProperty().bind(task.progressProperty());

            progressText.setText("In Progress");
            progressText.setFill(Color.WHITE);

            new Thread(task).start();

            task.setOnSucceeded(e -> {

                  progressText.setText("Complete");
                  progressText.setFill(Color.GREEN);

                  PauseTransition pause = new PauseTransition(Duration.seconds(1));

                  pause.setOnFinished(event -> {
                        progressBar.setVisible(false);
                        progressI.setVisible(false);
                        progressText.setText("");
                  });

                  pause.play();
            });

            task.setOnFailed(e -> {
                  
                  System.out.println(e.getSource().getException());

                  progressText.setText("Failed");
                  progressText.setFill(Color.RED);

                  PauseTransition pause = new PauseTransition(Duration.seconds(1));

                  pause.setOnFinished(event -> {
                        progressBar.setVisible(false);
                        progressI.setVisible(false);
                        progressText.setText("");
                  });

                  pause.play();

            });
      }

      @FXML
      private void credits() {
            GenericUtils.launchURL("http://www.rune-server.org/members/seven/");
      }

      @FXML
      private void close() {
            System.exit(0);
      }

      private byte[] grabSpriteBytes(int index) throws Exception {
            int dataLength = (SPRITES.get(index)).getData().length;
            byte[] returnValue = new byte[dataLength];
            byte offset = 0;
            System.arraycopy((SPRITES.get(index)).getData(), 0, returnValue, offset, dataLength);
            return returnValue;
      }

}
