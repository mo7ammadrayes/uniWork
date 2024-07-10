package uk.ac.soton.comp1206.scene;

import java.util.HashSet;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class ChallengeScene extends BaseScene {

  /**
   * used within the keylisteners. with Initial aim X position
   */
  protected int keyX = 0;
  /**
   * used within the keylisteners ,with // Initial aim Y position
   */
  protected int keyY = 0;

  private static final Logger logger = LogManager.getLogger(MenuScene.class);
  /**
   * Game represnrs the cuurent one
   */
  protected Game game;
  /**
   * text represents  the scoreLabel
   */
  protected Text scoreLabel;
  /**
   * text represents  the levelLabel
   */
  protected Text levelLabel;
  /**
   * text represents  the livesLabel
   */
  protected Text livesLabel;
  /**
   * text represents  the multiplierLabel
   */
  protected Text multiplierLabel;
  /**
   * text represents  the highScoreLabel
   */
  protected Text highScoreLabel;
  /**
   * a PieceBoard showing the second coming piece
   */
  protected PieceBoard nextBoard;
  /**
   * a PieceBoard showing the  coming piece
   */
  protected PieceBoard currentPieceBoard;
  /**
   * a GameBoard showing the real board
   */
  protected GameBoard board;
  /**
   * a Rectangle reperesnt the timer
   */
  protected Rectangle myTimerBar;
  /**
   * a Rectangle work as  the game timer
   */
  protected Timeline myTimeline;
  /**
   * the high score to be fectced and shown within the scene
   */
  IntegerProperty myHighScore;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
    myHighScore = new SimpleIntegerProperty(ScoreScene.loadScores().get(0).getValue());
  }


  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  protected void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
    getHighScore();
  }


  /**
   * Setup the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new Game(5, 5);
  }


  /**
   * <p>onNextPiece.</p>
   * used in within the pieeceBoards
   *
   * @param x which is a GamePiece object
   */
  protected void onNextPiece(GamePiece x) {
    this.currentPieceBoard.showPiece(x);
    this.nextBoard.showPiece(game.getNextPiece());
  }

  /**
   * rotates the pieces depenig on gameBloock
   *
   * @param x which a GameBlock object
   */
  protected void rotate(GameBlock x) {
    rotate(1);

  }

  /**
   * rotate the pices once using the rotate method in the Game class
   */
  protected void rotate() {
    this.currentPieceBoard.showPiece(game.rotateCurrentPiece());
  }

  /**
   * <p>rotate.</p>
   * calling the otate method in the Game class with adding an int specifyin how many time to
   * rotate
   *
   * @param x a int
   */
  protected void rotate(int x) {
    for (int i = 0; i < x; i++) {
      rotate();
    }
    Multimedia.playAudio("rotate.wav");

  }

  /**
   * this method used to swap the pieces betwwen the 2 PieceBoards
   *
   * @param gameBlock1 which is GameBlock object
   */
  protected void swap(GameBlock gameBlock1) {
    game.swapCurrentPiece();
    currentPieceBoard.showPiece(game.getCurrentPiece());
    nextBoard.showPiece(game.getNextPiece());
    Multimedia.playAudio("transition.wav");

  }

  /**
   * keyListener which does different thing depending on the pressed key
   *
   * @param keyEvent which is a KeyEvent object
   */
  protected void keyListener(KeyEvent keyEvent) {
    KeyCode code = keyEvent.getCode();
    switch (code) {
      case ESCAPE:
        gameEnd();
        exitToMenu();
        logger.info("ecs was pressed");
        break;
      case ENTER:
      case X:
        this.blockClicked(this.board.getBlock(this.keyX, this.keyY));
        logger.info("enter or x was pressed");
        break;
      case SPACE:
      case R:
        swap(this.board.getBlock(this.keyX, this.keyY));
        logger.info("R or Space was pressed");

        break;
      case Q:
      case Z:
      case OPEN_BRACKET:
        rotatePieceCounterClockwise();
        logger.info("Q , Z or OPEN_BRACKET was pressed");

        break;
      case E:
      case C:
      case CLOSE_BRACKET:
        rotatePieceClockwise();
        logger.info("E , C or CLOSE_BRACKET was pressed");

        break;
      case A:
      case LEFT:
        moveAimLeft();
        logger.info("A or Left was pressed");

        break;
      case D:
      case RIGHT:
        moveAimRight();
        logger.info("D or Right was pressed");

        break;
      case W:
      case UP:
        moveAimUp();
        logger.info("W or Up was pressed");
        break;
      case S:
      case DOWN:
        moveAimDown();
        logger.info("S or Up was pressed");

        break;
      default:
        break;
    }

    updateHover();
  }

  /**
   * a method to exit To the Menu.
   */
  protected void exitToMenu() {
    this.gameWindow.startMenu();
  }

  /**
   * this method rotates the Piece Counter Clockwise and used in keyListener method.
   */
  protected void rotatePieceCounterClockwise() {
    this.rotate(3);
  }

  /**
   * this method rotates the Piece  Clockwise and used in keyListener method.
   */
  protected void rotatePieceClockwise() {
    this.rotate(1);
  }

  /**
   * * this method moves  the aim left and used in keyListener method.
   */
  protected void moveAimLeft() {
    if (this.keyX > 0) {
      keyX--;
    }
  }

  /**
   * this method moves  the aim right and used in keyListener method.
   */
  protected void moveAimRight() {
    if (this.keyX < game.getCols() - 1) {
      keyX++;
    }
  }

  /**
   * this method moves  the aim up and used in keyListener method.
   */
  protected void moveAimUp() {
    if (this.keyY > 0) {
      keyY--;
    }
  }

  /**
   * this method moves  the aim down and used in keyListener method.
   */
  protected void moveAimDown() {
    if (this.keyY < game.getRows() - 1) {
      keyY++;
    }
  }

  /**
   * <p>this method updtast the hover depening on the aim x and aim y.</p>
   */
  protected void updateHover() {
    board.hover(board.getBlock(keyX, keyY));

  }

  /**
   * <p>lineClearing.</p>
   * a mthod to be called when Clearing a line
   *
   * @param x wich is a {HashSet} object
   */
  protected void lineClearing(HashSet<GameBlockCoordinate> x) {
    board.fadeOut(x);
    Multimedia.playAudio("clear.wav");
  }

  /**
   * <p>setOnGameLoop.</p>
   *
   * @param x a int
   */
  protected void setOnGameLoop(int x) {
    if (myTimeline != null) {
      myTimeline.stop(); // Stop any existing animation
    }
    myTimerBar.setWidth(
        gameWindow.getWidth() - 20); // Reset the width of the timer bar for a new cycle
// Reset the width and color of the timer bar for a new cycle
    myTimerBar.setFill(Color.GREEN);  // Start with green color

// Create a timeline for width and color animation
    myTimeline = new Timeline(
        new KeyFrame(Duration.ZERO,
            new KeyValue(myTimerBar.widthProperty(), gameWindow.getWidth() - 50),
            new KeyValue(myTimerBar.fillProperty(), Color.GREEN)
        ),
        new KeyFrame(Duration.millis(x / 2),
            new KeyValue(myTimerBar.fillProperty(), Color.YELLOW)
        ),
        new KeyFrame(Duration.millis(x),
            new KeyValue(myTimerBar.widthProperty(), 0),
            new KeyValue(myTimerBar.fillProperty(), Color.RED)
        )
    );
    myTimeline.setCycleCount(Timeline.INDEFINITE);
    myTimeline.play();
  }

  /**
   * gameEnd method used to end the game
   */
  protected void gameEnd() {
    logger.info("Game Over");
    Multimedia.stopBackgroundMusic();
    game.gameStopping();
  }

  /**
   * this method compares keep track of the high score
   */
  protected void getHighScore() {
    int siu = this.game.scoreProperty().get();
    if (siu > this.myHighScore.get()) {
      this.myHighScore.set(siu);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Initialise the scene and start the game
   */


  @Override
  public void initialise() {
    logger.info("Initialising Challenge");

    // Start the game logic
    game.start();

    // Adding keyboard listener
    gameWindow.getScene().setOnKeyPressed(this::keyListener);
  }

  /**
   * Build the Challenge window
   */
  public void build() {
    logger.info("Building " + this.getClass().getName());
    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    // The main container for the scene with padding and style
    BorderPane borderPane = new BorderPane();
    borderPane.setPadding(new Insets(10));
    borderPane.getStyleClass().add("newchallenge-background");
    root.getChildren().add(borderPane);

    // Center - Main game board
    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    borderPane.setCenter(board);

    // Right - Piece boards ,  level ,and multP
    currentPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 6, gameWindow.getWidth() / 6);
    nextBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 7, gameWindow.getWidth() / 8);

    // Top - Game stats and title
    Text mainTitle = new Text("Challenge Scene");
    mainTitle.getStyleClass().add("heading");

    scoreLabel = new Text();
    scoreLabel.textProperty().bind(game.scoreProperty().asString("Score: %d"));
    scoreLabel.getStyleClass().add("score");
    levelLabel = new Text();
    levelLabel.textProperty().bind(game.levelProperty().asString("Level: %d"));
    levelLabel.getStyleClass().add("level");
    livesLabel = new Text();
    livesLabel.textProperty().bind(game.livesProperty().asString("Lives: %d"));
    livesLabel.getStyleClass().add("lives");
    multiplierLabel = new Text();
    multiplierLabel.textProperty().bind(game.multiplierProperty().asString("Multiplier: %d"));
    multiplierLabel.getStyleClass().add("multP");
    highScoreLabel = new Text();
    highScoreLabel.getStyleClass().add("hiscore");
    highScoreLabel.textProperty().bind(this.myHighScore.asString("High score: %d"));

    HBox statsPane = new HBox(20, scoreLabel, livesLabel, highScoreLabel);
    statsPane.setAlignment(Pos.CENTER);
    var levelAndMult = new VBox(8);
    levelAndMult.setAlignment(Pos.CENTER);
    levelAndMult.getChildren().addAll(multiplierLabel, levelLabel);
    VBox piecesBoards = new VBox(10, levelAndMult, currentPieceBoard, nextBoard);
    piecesBoards.setAlignment(Pos.CENTER_RIGHT);

    borderPane.setRight(piecesBoards);

    VBox topContainer = new VBox(5, mainTitle, statsPane);
    topContainer.setAlignment(Pos.CENTER);
    borderPane.setTop(topContainer);

    // Bottom - Timer bar
    myTimerBar = new Rectangle(gameWindow.getWidth() / 5, 10);
    myTimerBar.setFill(Color.ORCHID);
    StackPane timerPane = new StackPane(myTimerBar);
    timerPane.setAlignment(Pos.CENTER);
    borderPane.setBottom(timerPane);
    //
    board.setOnBlockClick(this::blockClicked);

    game.setNextPieceListener(this::onNextPiece);
    currentPieceBoard.setOnBlockClick(this::rotate);
    currentPieceBoard.centerIndicator();
    board.setOnRightClicked(this::rotate);
    game.setMyLineClearedListener(this::lineClearing);
    game.setMyGameLoopListener(this::setOnGameLoop);
    game.setMyGameOverListener(game -> {
      gameEnd();
      this.gameWindow.startScores(this.game);
    });
    nextBoard.setOnBlockClick(this::swap);
  }

}
