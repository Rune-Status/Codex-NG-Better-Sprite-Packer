package seven.controller;

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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
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
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import seven.App;
import seven.Configuration;
import seven.sprite.Sprite;
import seven.util.FileUtil;
import seven.util.msg.ExceptionMessage;
import seven.util.msg.InformationMessage;
import seven.util.GenericUtil;

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

      public static final List<Sprite> SPRITES = new ArrayList<>();      

      private FilteredList<Integer> filteredSprites;

      private SortedList<Integer> sortedList;

      private ObservableList<Integer> elements = FXCollections.observableArrayList();

      private int currentSpriteIndex = 0;
      
      private Image newImage;

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
                                                      widthT.setText(Double.toString(newImage.getWidth()));
                                                      heightT.setText(Double.toString(newImage.getHeight()));
                                                }
                                                drawOffsetXT.setText(Integer.toString(sprite.getDrawOffsetX()));
                                                drawOffsetYT.setText(Integer.toString(sprite.getDrawOffsetY()));

                                                displaySprite(newValue.intValue());
                                          } catch (Exception ex) {
                                                new ExceptionMessage("A problem was encountered while trying to display a sprite.", ex);
                                          }
                                    }
                              }
                        });

            titledPane.heightProperty().addListener((obs, oldHeight, newHeight) -> resizeStage());

            Image clearImage = new Image(App.class.getResourceAsStream("resources/clear.png"));
            Image saveArchiveImage = new Image(App.class.getResourceAsStream("resources/saveArchive.png"));
            Image loadSpriteImage = new Image(App.class.getResourceAsStream("resources/loadSprite.png"));
            Image loadArchiveImage = new Image(App.class.getResourceAsStream("resources/loadArchive.png"));
            Image saveSpritesImage = new Image(App.class.getResourceAsStream("resources/saveSprites.png"));
            
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
            App.getMainStage().setTitle(String.format("%s v%.1f%n", Configuration.TITLE, Configuration.VERSION));
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
                  try {
                        loadSprites(selectedDirectory.toPath());
                  } catch (Exception ex) {
                        new ExceptionMessage("A problem was encountered when opening the sprites directory.", ex);
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
                                          FileUtil.loadArchivedSprites(archiveName,
                                                      selectedDirectory.toPath());
                                    } catch (Exception ex) {                                          
                                          new ExceptionMessage("A problem was encountered while loading the sprites archive.", ex);
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
      private void dumpSprites() {
            if (SPRITES.isEmpty()) {
                  new InformationMessage("Information", null, "There are no sprites to dump.");
                  return;
            }
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select the directory to place the sprites in.");
            String homePath = System.getProperty("user.home");
            File file = new File(homePath);
            chooser.setInitialDirectory(file);
            File selectedDirectory = chooser.showDialog(loadSprite.getScene().getWindow());
            if (selectedDirectory != null) {
                  for (Sprite sprite : SPRITES) {
                        byte[] data = sprite.getData();

                        try {
                              final BufferedImage image = FileUtil.byteArrayToImage(data);

                              ImageIO.write(image, "png",
                                          Paths.get(selectedDirectory.toString(),
                                                      Integer.toString(sprite.getIndex()) + ".png")
                                                      .toFile());
                        } catch (IOException e) {
                              e.printStackTrace();
                        }
                  }
                  new InformationMessage("Information", null,
                              "Successfully dumped " + SPRITES.size() + " sprites!");
            }
      }

      private void populateList() {
            int size = FileUtil.totalArchivedSprites;
            for (int index = 0; index < size; index++) {
                  elements.add(index);
            }
            App.getMainStage().setTitle(String.format("%s v%.1f%n [%d]", Configuration.TITLE, Configuration.VERSION, size));
      }

      private void loadSprites(Path path) throws IOException {
            this.elements.clear();

            File[] files = (new File(path.toString())).listFiles();
            FileUtil.totalSprites = files.length;
     
            File[] sortedFiles = new File[files.length];

            for (File file : files) {
                  if (file != null) {

                        String p = file.getName().replaceAll(".png", "").replaceAll(".PNG", "");

                        sortedFiles[Integer.valueOf(p)] = file;

                  }
            }

            for (int index = 0; index < sortedFiles.length; index++) {
                  byte[] data = new byte[(int) sortedFiles[index].length()];
                  try {
                        FileInputStream d = new FileInputStream(sortedFiles[index]);
                        d.read(data);
                        d.close();
                  } catch (Exception ex) {
                        new ExceptionMessage("A problem was encountered while loading sprites.", ex);
                  }
                  if (data != null && data.length > 0) {
                        Sprite sprite = new Sprite(index, data);
                        SPRITES.add(sprite);
                        this.elements.add(sprite.getIndex());
                  }

            }

            App.getMainStage().setTitle(String.format("%s v%.1f%n [%d]", Configuration.TITLE, Configuration.VERSION, SPRITES.size()));
      }

      private void displaySprite(int index) throws Exception {
            Sprite sprite = SPRITES.get(index);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(sprite.getData()));
            newImage = SwingFXUtils.toFXImage(image, null);

            imageView.setImage(newImage);
            Tooltip.install(imageView, new Tooltip("Width: " + newImage.getWidth() + " Height: " + newImage.getHeight()));
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
                        new ExceptionMessage("A problem was encountered while trying to write a sprite archive.", ex);
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
                        new ExceptionMessage("A problem was encountered while trying to write a sprite archive.", ex);
                  }

                  if (successful) {
                        new InformationMessage("Information", "Successful!");
                  }

            }
      }

      @FXML
      private void credits() {
            GenericUtil.launchURL("http://www.rune-server.org/members/seven/");
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
