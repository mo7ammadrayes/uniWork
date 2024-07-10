package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class Game {

  private static final Logger logger = LogManager.getLogger(Game.class);
  Random random = new Random();
  /**
   * the used NextPieceListener in the game and it used when it related to it
   */
  protected NextPieceListener myNextPieceListener;
  /**
   * the used LineClearedListener in the game and it used when it related to it
   */
  protected LineClearedListener myLineClearedListener;
  /**
   * IntegerProperty which is  the player current score
   */
  protected IntegerProperty score = new SimpleIntegerProperty(0);
  /**
   * IntegerProperty which is  the player current level
   */
  protected IntegerProperty level = new SimpleIntegerProperty(0);
  /**
   * IntegerProperty which is  the player current lives
   */
  protected IntegerProperty lives = new SimpleIntegerProperty(3);
  /**
   * IntegerProperty which is  the player current multiplier
   */
  protected IntegerProperty multiplier = new SimpleIntegerProperty(1);

  /**
   * Number of rows
   */
  protected final int rows;

  /**
   * Number of columns
   */
  protected final int cols;

  /**
   * The grid model linked to the game
   */
  protected final Grid grid;
  /**
   * GamePiece which is the 1st one to be played
   */
  protected GamePiece currentPiece;
  /**
   * GamePiece which is the 2nd one to be played
   */
  protected GamePiece theNextPiece;
  /**
   * the first delay time used for the game loop
   */
  protected final int firstDelayTimer = 12000;
  /**
   * Timer used withing the game loop
   */
  protected Timer myTimer;
  /**
   * the used GameLoopListener in the game and it used when it related to it
   */
  protected GameLoopListener myGameLoopListener;
  /**
   * the used GameLoopListener in the game and it used when it related to it
   */
  protected GameOverListener myGameOverListener;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;
    //Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  /**
   * Start the game
   */
  public void start() {
    logger.info("Starting game");
    Multimedia.playBackgroundMusic("newChallenge.mp3");

    initialiseGame();
    startGameLoop();
    logger.info("the lives " + getLives());

  }

  /**
   * Initialising the   new game and setting up anything that needs to be done at the game starting
   */
  public void initialiseGame() {
    currentPiece = spawnPiece();
    theNextPiece = spawnPiece();
    if (myNextPieceListener != null) {
      myNextPieceListener.onNextPiece(currentPiece);
    }

    myTimer = new Timer();
  }


  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    logger.info("trying to place " + currentPiece.toString());
    // Place the piece on the grid
    if (grid.canPlayPiece(currentPiece, gameBlock.getX(), gameBlock.getY())) {
      grid.playPiece(currentPiece, gameBlock.getX(), gameBlock.getY());
      Multimedia.playAudio("place.wav");

      // Perform any additional actions required after placing the piece
      afterPiece();

      // Update to the next piece now that the current piece has been successfully placed
      nextPiece();
      resetTimer();
    } else if (!grid.canPlayPiece(currentPiece, gameBlock.getX(), gameBlock.getY())) {
      Multimedia.playAudio("fail.wav");
    }
  }


  /**
   * <p>score.</p>
   * updates the player score
   *
   * @param noLines  a int
   * @param noBlocks a int
   */
  protected void score(int noLines, int noBlocks) {
    int additionalScore = noBlocks * noLines * 10 * this.multiplier.get();
    setScore(additionalScore + this.getScore());

  }

  /**
   * this method checks for the full lines and the line to be cleared , then remove them.
   */
  public void afterPiece() {
    logger.info("Entering afterPiece method.");
    int lines = 0;
    HashSet<GameBlockCoordinate> clearedBlocks = new HashSet<>();

    // Check for full horizontal lines
    for (int x = 0; x < getRows(); x++) {
      boolean fullLine = true;
      for (int y = 0; y < getCols(); y++) {
        if (grid.get(x, y) == 0) {
          fullLine = false;
          break;
        }
      }
      if (fullLine) {
        for (int y = 0; y < getCols(); y++) {
          clearedBlocks.add(new GameBlockCoordinate(x, y));
        }
        lines++;
      }
    }

    // Check for full vertical lines
    for (int y = 0; y < getCols(); y++) {
      boolean fullLine = true;
      for (int x = 0; x < getRows(); x++) {
        if (grid.get(x, y) == 0) {
          fullLine = false;
          break;
        }
      }
      if (fullLine) {
        for (int x = 0; x < getRows(); x++) {
          clearedBlocks.add(new GameBlockCoordinate(x, y));
        }
        lines++;

      }
    }
    if (clearedBlocks.size() != 0) {
      if (myLineClearedListener != null) {
        myLineClearedListener.lineClearing(clearedBlocks);
        for (GameBlockCoordinate gameBlockToRemove : clearedBlocks) {
          grid.set(gameBlockToRemove.getX(), gameBlockToRemove.getY(), 0);
        }
      }
    }
    if (lines > 0) {
      score(clearedBlocks.size(), lines);
      this.setMultiplier(this.getMultiplier() + 1);
    } else {
      this.setMultiplier(1);
    }
    levelChecking();

    logger.info("Exiting afterPiece method.");

  }

  /**
   * method to increase the level based on the score
   */
  public void levelChecking() {
    double level = (double) this.getScore() / 1000;
    if (level > getLevel()) {
      setLevel((int) level);
      Multimedia.playAudio("level.wav");

    }
  }


  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * <p>spawnPiece.</p>
   *
   * @return a random GamePiece object
   */
  protected GamePiece spawnPiece() {

    return GamePiece.createPiece(random.nextInt(15));
  }

  /**
   * the method sets the next peice
   * <p>nextPiece.</p>
   */
  protected void nextPiece() {
    currentPiece = theNextPiece;
    theNextPiece = spawnPiece();
    // Notify the listener about the new next piece, if the listener is registered
    if (myNextPieceListener != null) {
      myNextPieceListener.onNextPiece(currentPiece);

    }
  }

  /**
   * <p>rotateCurrentPiece.</p>
   * it rotate the current piece using the rotate method in the GamePiece class
   *
   * @return a {@link uk.ac.soton.comp1206.game.GamePiece} object
   */
  public GamePiece rotateCurrentPiece() {
    currentPiece.rotate();
    return currentPiece;
  }


  public IntegerProperty scoreProperty() {
    return this.score;
  }

  /**
   * <p>Getter for the field <code>score</code>.</p>
   *
   * @return a int
   */
  public final int getScore() {
    return this.score.get();
  }

  /**
   * <p>Setter for the field <code>score</code>.</p>
   *
   * @param score a int
   */
  public final void setScore(int score) {
    this.score.set(score);
  }


  public IntegerProperty levelProperty() {
    return this.level;
  }

  /**
   * <p>Getter for the field <code>level</code>.</p>
   *
   * @return a int
   */
  public final int getLevel() {
    return this.level.get();
  }

  /**
   * <p>Setter for the field <code>level</code>.</p>
   *
   * @param level a int
   */
  public final void setLevel(int level) {
    this.level.set(level);
  }


  public IntegerProperty livesProperty() {
    return this.lives;
  }

  /**
   * <p>Getter for the field <code>lives</code>.</p>
   *
   * @return a int
   */
  public final int getLives() {
    return this.lives.get();
  }

  /**
   * <p>Setter for the field <code>lives</code>.</p>
   *
   * @param lives a int
   */
  public final void setLives(int lives) {
    this.lives.set(lives);
  }

  public IntegerProperty multiplierProperty() {
    return this.multiplier;
  }

  /**
   * <p>Getter for the field <code>multiplier</code>.</p>
   *
   * @return a int
   */
  public final int getMultiplier() {
    return this.multiplier.get();
  }

  /**
   * <p>Setter for the field <code>multiplier</code>.</p>
   *
   * @param multiplier a int
   */
  public final void setMultiplier(int multiplier) {
    this.multiplier.set(multiplier);
  }

  /**
   * <p>setNextPieceListener.</p>
   *
   * @param listener of NextPieceListener kind
   */
  public void setNextPieceListener(NextPieceListener listener) {
    this.myNextPieceListener = listener;

  }

  /**
   * <p>Setter for the field myLineClearedListener
   *
   * @param listener which is LineClearedListener object
   */
  public void setMyLineClearedListener(LineClearedListener listener) {
    this.myLineClearedListener = listener;
  }

  /**
   * <p>Getter for the currentPiece field variable
   *
   * @return a GamePiece object
   */
  public GamePiece getCurrentPiece() {
    return currentPiece;
  }

  /**
   * <p>getNextPiece.</p>
   *
   * @return a GamePiece object
   */
  public GamePiece getNextPiece() {
    return theNextPiece;
  }


  /**
   * this method swapCurrentPiece.
   */
  public void swapCurrentPiece() {
    GamePiece x = this.currentPiece;
    currentPiece = theNextPiece;
    theNextPiece = x;

  }

  /**
   * <p>getTimerDelay.</p>
   *
   * @return a int representing the timer dleay
   */
  public int getTimerDelay() {
    int delayTimer = firstDelayTimer - (500 * getLevel());
    return Math.max(delayTimer, 2500);
  }

  /**
   * this method implement how the gameLoop works.
   */
  public void gameLoop() {
    if (getLives() >= 1) {
      setLives(getLives() - 1);
      logger.info("Lives: " + getLives());
      setMultiplier(1);
      nextPiece();
      Multimedia.playAudio("lifelose.wav");

    } else {
      gameEnding();
    }


  }

  /**
   * this method statrs the gameLoop
   */
  protected void startGameLoop() {
    long period = getTimerDelay();
    myTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        gameLoop();
      }
    }, period, period);
    myGameLoopListener.setOnGameLoop(getTimerDelay());
  }


  /**
   * this method resetTimer.
   */
  public void resetTimer() {
    if (myTimer != null) {
      myTimer.cancel();
    }
    myTimer = new Timer();
    startGameLoop();
  }


  /**
   * <p>Setter for the field myGameLoopListener.
   *
   * @param myGameLoopListener which is a GameLoopListener object
   */
  public void setMyGameLoopListener(GameLoopListener myGameLoopListener) {
    this.myGameLoopListener = myGameLoopListener;
  }

  /**
   * method for stopping the game timer .
   */
  public void gameStopping() {
    myTimer.cancel();
    myTimer.purge();
  }

  /**
   * <p>gameEnding.</p>
   */
  public void gameEnding() {
    if (myGameOverListener != null) {
      Platform.runLater(() -> myGameOverListener.onGameOver(this));
    }
  }


  /**
   * <p>Setter for the field myGameOverListener.
   *
   * @param myGameOverListener which is a GameOverListener object
   */
  public void setMyGameOverListener(GameOverListener myGameOverListener) {
    this.myGameOverListener = myGameOverListener;
  }

}
