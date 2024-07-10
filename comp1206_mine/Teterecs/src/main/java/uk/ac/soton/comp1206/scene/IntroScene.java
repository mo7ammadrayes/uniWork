package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * class holds for the intro scene
 */
public class IntroScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(IntroScene.class);

  /**
   * Create a  new intro  scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public IntroScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  /**
   * initialise the intro scene
   */
  @Override
  public void initialise() {
    logger.info("starting the intro");
    Multimedia.playBackgroundMusic("intro.mp4");
    this.scene.setOnKeyPressed(
        (event) -> {
          if (event.getCode() == KeyCode.ESCAPE) {
            Multimedia.playAudio("transition.wav");
            System.exit(0);
            logger.info("Escape Pressed");
          }
        });

  }

  /**
   * Build the IntroScene window
   */
  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    // Your intro text
    Text introText = new Text("Welcome to Tetrecs!");
    introText.getStyleClass().add("intro-text");

    // Load the image for the logo
    Image logoImage = new Image(
        getClass().getResourceAsStream("/images/ECSGames.png")); // Replace with the correct path
    ImageView logoView = new ImageView(logoImage);
    logoView.setPreserveRatio(true);
    logoView.setFitHeight(100); // Adjust this to your needs

    // Create a VBox to hold both the text and the logo
    VBox introContent = new VBox();
    introContent.setAlignment(Pos.CENTER); // Center the content
    introContent.setSpacing(20); // Space between the text and the logo
    introContent.getChildren().addAll(logoView, introText);

    // Set up the introPane with the VBox as its content
    StackPane introPane = new StackPane();
    introPane.getStyleClass().add("intro-pane");
    introPane.getChildren().add(introContent); // Add VBox to the stack pane
    root.getChildren().add(introPane);

    // Set initial opacity to 0 so the text starts invisible
    introText.setOpacity(0);

    // Create a fade-in transition
    FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), introText);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);

    // Create a fade-out transition
    FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), introText);
    fadeOut.setFromValue(1);
    fadeOut.setToValue(0);
    fadeOut.setDelay(Duration.seconds(3)); // Delay for fade-out

    // Chain the fade-out after the fade-in completes
    fadeIn.setOnFinished(e -> fadeOut.play());

    // Transition to the menu scene after fade-out completes
    fadeOut.setOnFinished(e -> {
      gameWindow.startMenu();
      Multimedia.stopBackgroundMusic();
    });

    // Start the fade-in transition
    fadeIn.play();
  }
}