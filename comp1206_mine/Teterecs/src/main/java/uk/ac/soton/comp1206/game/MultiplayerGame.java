package uk.ac.soton.comp1206.game;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Timer;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

/**
 * <p>MultiplayerGame class.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class MultiplayerGame extends Game {

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
  private final Communicator myCommunicator;

  private LinkedList<GamePiece> pieceQueue;
  private boolean isStarted;

  /**
   * <p>Constructor for MultiplayerGame.</p>
   *
   * @param communicator object
   * @param cols         a int
   * @param rows         a int
   */
  public MultiplayerGame(Communicator communicator, int cols, int rows) {
    super(cols, rows);
    this.myCommunicator = communicator;
    this.pieceQueue = new LinkedList<>();
    this.myCommunicator.addListener(message -> Platform.runLater(() -> getMessage(message.trim())));
  }


  /**
   * this method handles the received messages form the server and then calling method depending on
   * the message
   *
   * @param message of string type
   */

  private void getMessage(String message) {
    logger.info("Received getMessage call with message: " + message);  // Log the received message
    String[] command = message.split(" ", 2);
    logger.info("Command split into: " + Arrays.toString(command));  // Log the result of the split

    if (command.length < 2) {
      logger.warn("Command array length less than expected: " + Arrays.toString(command));
    }

    switch (command[0]) {
      case "PIECE":
        if (command.length >= 1) {
          receivePiece(Integer.parseInt(command[1]));
        } else {
          logger.warn("PIECE command received without a piece index: " + message);
        }
        break;

      case "MSG":
      case "SCORES":
      case "DIE":
      case "BOARD":
        logger.info("this is handled in the MultiplayerScene Class. ");

        break;

      default:
        logger.warn("Unhandled message type: " + message);
    }
  }


  /**
   * {@inheritDoc} an overrided method for initialiseGame for the multipLayer game type with adding
   * some special functionality.
   */
  @Override
  public void initialiseGame() {
    myTimer = new Timer();

    this.score.set(0);
    this.level.set(0);
    this.lives.set(3);
    myCommunicator.send("SCORES");
    for (int i = 0; i < 5; i++) {
      myCommunicator.send("PIECE");
    }
  }

  /**
   * {@inheritDoc} * an overrided method for blockClicked for the multipLayer game type with adding
   * some special functionality.
   */
  public void blockClicked(GameBlock gameBlock) {
    super.blockClicked(gameBlock);
    sendBoard();
  }

  /**
   * used to  send board after the blook is clicked
   * <p>sendBoard.</p>
   */
  public void sendBoard() {
    StringBuilder boardInfo = new StringBuilder("BOARD ");
    for (int y = 0; y < this.getCols(); y++) {
      for (int x = 0; x < this.getRows(); x++) {
        boardInfo.append(this.grid.get(x, y)).append(" ");
      }
    }
    myCommunicator.send(boardInfo.toString().trim());
  }

  /**
   * {@inheritDoc}
   */
  protected void score(int noLines, int noBlocks) {
    super.score(noLines, noBlocks);
    myCommunicator.send("SCORE " + this.score.get());

  }

  /**
   * * an overrided method for gameLoop for the multipLayer game type with adding some special
   * functionality.
   */
  public void gameLoop() {
    if (getLives() >= 1) {
      setLives(getLives() - 1);
      logger.info("Lives: " + getLives());
      setMultiplier(1);
      nextPiece();
      Multimedia.playAudio("lifelose.wav");
      myCommunicator.send("LIVES " + getLives());
    } else {
      gameEnding();
    }
  }

  /**
   * this method handled the PIECE recieval from the server
   *
   * @param newPiece int
   */
  private void receivePiece(int newPiece) {
    GamePiece piece = GamePiece.createPiece(newPiece, this.random.nextInt(3));
    logger.info("Received piece: {}", piece);
    this.pieceQueue.add(piece);
    logger.info("Added piece to queue: {}", piece);
    if (!this.isStarted && this.pieceQueue.size() > 2) {
      logger.info("Start the game with pieces in the queue");
      this.theNextPiece = this.spawnPiece();
      this.nextPiece();
      this.isStarted = true;
    }
  }

  /**
   * * an overrided method for spawnPiece for the multipLayer game type with adding some special
   * functionality.
   *
   * @return a GamePiece object
   */
  public GamePiece spawnPiece() {
    logger.info("SpawnPiece from queue: {}", pieceQueue);
    this.myCommunicator.send("PIECE");
    return this.pieceQueue.pop();
  }

  public void start() {
    logger.info("Starting game");
    Multimedia.playBackgroundMusic("multP.mp3");

    initialiseGame();
    startGameLoop();
    logger.info("the lives " + getLives());

  }


}
