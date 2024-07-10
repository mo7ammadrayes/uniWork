package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>InstructionsScene class.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class InstructionsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  /**
   * {@inheritDoc} Initialise the Instructions scene
   */
  @Override
  public void initialise() {
    Multimedia.playBackgroundMusic("SHORT EPIC INTRO NO COPYRIGHT INTRO MUSIC.mp3");

    logger.info("starting the instruction scene ");
    this.scene.setOnKeyPressed(
        (event) -> {
          if (event.getCode() == KeyCode.ESCAPE) {
            Multimedia.playAudio("transition.wav");
            gameWindow.startMenu();
            logger.info("Escape Pressed");
          }
        });
  }


  /**
   * {@inheritDoc} * Build the Instructions window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var instructionPane = new BorderPane();
    instructionPane.setMaxWidth(gameWindow.getWidth());
    instructionPane.setMaxHeight(gameWindow.getHeight());
    instructionPane.getStyleClass().add("instcrScene-background");

    // Game Pieces Section
    Text pieceBoardsLabel = new Text("Game Pieces");
    pieceBoardsLabel.getStyleClass().add("heading");
    var pieceBoards = new GridPane();
    pieceBoards.setHgap(10);
    pieceBoards.setVgap(7);
    pieceBoards.setAlignment(Pos.CENTER);
    //pieceBoards.setMaxWidth(gameWindow.getWidth() / 4);
    pieceBoards.setMaxHeight(gameWindow.getHeight() / 5);

    // Instructions Section
    Text instTitle = new Text("Instructions");
    instTitle.getStyleClass().add("heading");
    Text instrContent = new Text(
        "TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of \n"
            + "the upcoming blocks before the time runs out.Lose all 3 lives and you're destroyed!");
    instrContent.getStyleClass().add("instructions");
    TextFlow instrText =
        new TextFlow(instrContent);
    instrText.setTextAlignment(TextAlignment.CENTER);
    // Image at the top of the instructions
    ImageView instrImage = new ImageView(
        new Image(MenuScene.class.getResource("/images/Instructions.png").toExternalForm()));
    instrImage.setFitHeight(300); // Increase the desired height to make the image bigger
    instrImage.setPreserveRatio(true);
    instrImage.setSmooth(true);
    instrImage.setCache(true);

    // Wrap the instruction elements in a VBox
    var topContainer = new VBox(10, instTitle, instrText, instrImage);
    topContainer.setAlignment(Pos.CENTER);

    // Populate the grid for game pieces
    int numberOfColumns = 5;
    int numberOfRows = 3;
    int pieceIndex = 0;

    for (int row = 0; row < numberOfRows; row++) {
      for (int col = 0; col < numberOfColumns; col++) {
        if (pieceIndex >= 15) {
          break; // Stop if we reach the number of pieces we have
        }
        GamePiece myGamePiece = GamePiece.createPiece(pieceIndex++);
        PieceBoard myGameBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 11,
            gameWindow.getHeight() / 11);
        myGameBoard.showPiece(myGamePiece);
        pieceBoards.add(myGameBoard, col, row);
      }
    }

    // Wrap the game pieces elements in a VBox
    var bottomContainer = new VBox(5, pieceBoardsLabel, pieceBoards);
    bottomContainer.setAlignment(Pos.CENTER);

    // Set top and bottom containers to the BorderPane
    instructionPane.setTop(topContainer);
    instructionPane.setBottom(bottomContainer);

    // Add the BorderPane to the root
    root.getChildren().add(instructionPane);
  }
}




