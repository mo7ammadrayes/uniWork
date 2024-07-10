package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class MenuScene extends BaseScene {
;
  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /**
   * Handle when the Start Game button is pressed
   *
   * @param event which is an ActionEvent object
   */
  private void startGame(ActionEvent event) {
    gameWindow.startChallenge();
  }

  /**
   * Handle when the Instructions button is pressed
   *
   * @param event which is an ActionEvent object
   */
  public void startInstructions(ActionEvent event) {
    gameWindow.loadScene(new InstructionsScene(this.gameWindow));
  }

  /**
   * Handle when the Multiplayer button is pressed
   *
   * @param event which is an ActionEvent object
   */
  public void startMultiplayer(ActionEvent event) {
    this.gameWindow.startLobby();

  }

  /**
   * Build the menu layout
   */
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("newmenu-background");
    var mainPane = new VBox(25);
    // Load image and set its properties
    ImageView menuImage = new ImageView(
        new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm()));
    menuImage.setFitHeight(150); // Set the desired height
    menuImage.setPreserveRatio(true); // Preserve the aspect ratio
    menuImage.setSmooth(true); // Ensure the image is smoothly scaled
    menuImage.setCache(true); // Cache the image for performance

    // Create and configure the rotate transition for the image
    RotateTransition myRotater = new RotateTransition(Duration.seconds(5), menuImage);
    myRotater.setCycleCount(Animation.INDEFINITE);
    myRotater.setFromAngle(-3);
    myRotater.setToAngle(3);
    myRotater.setAutoReverse(true);
    myRotater.play();

    // Create the main pane and add image and title to it

    // Create and configure the title
    var title = new Text("TetrECS");
    title.getStyleClass().add("title");
    // mainPane.setTop(i); // The title will be at the top of the BorderPane

    // Create the play playButton and set its action
    Button playButton = new Button("Play");
    playButton.setOnAction(event -> {
      Multimedia.stopBackgroundMusic(); // Stop the music
      Multimedia.playAudio("transition.wav");
      Platform.runLater(() -> {
        startGame(event); // Start the game in the next UI update cycle
      });
    });
    var multiPButton = new Button(" Multiplayer");
    multiPButton.setOnAction(event -> {
      Multimedia.stopBackgroundMusic(); // Stop the music

      startMultiplayer(event);
      Multimedia.playAudio("transition.wav");
    });
    var instrButton = new Button(" How To play");
    instrButton.setOnAction(event -> {
      Multimedia.stopBackgroundMusic(); // Stop the music

      startInstructions(event);
      Multimedia.playAudio("transition.wav");
    });
    var exitButton = new Button("Exit");
    exitButton.setOnAction(event -> System.exit(0));
    mainPane.setAlignment(Pos.CENTER); // Center the contents of the VBox
    var buttons = new VBox(10);
    buttons.getChildren().addAll(playButton, multiPButton, instrButton, exitButton);
    playButton.getStyleClass().add("menuItem");
    multiPButton.getStyleClass().add("menuItem");
    instrButton.getStyleClass().add("menuItem");
    exitButton.getStyleClass().add("menuItem");

    buttons.setAlignment(Pos.CENTER);
    mainPane.getChildren().addAll(menuImage, buttons);
    // Add the main pane to the menu pane
    menuPane.getChildren().add(mainPane);

    // Set the menu pane as the root for the scene
    root.getChildren().add(menuPane);
  }


  /**
   * {@inheritDoc}
   *
   * Initialise the menu
   */
  @Override
  public void initialise() {
    Multimedia.playBackgroundMusic("newMenue.mp3");
    logger.info("initialising the Menu scene");
    gameWindow.getScene().setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        Multimedia.playAudio("transition.wav");
        System.exit(0);
        logger.info("Escape Pressed");

      }
    });
  }


}
