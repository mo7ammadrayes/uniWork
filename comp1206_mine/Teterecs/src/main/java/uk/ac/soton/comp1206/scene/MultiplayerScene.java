package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;


/**
 * <p>MultiplayerScene class extend the ChallengeScene class.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class MultiplayerScene extends ChallengeScene {

  private static final Logger myLogger = LogManager.getLogger(MultiplayerScene.class);
  BooleanProperty isVisible = new SimpleBooleanProperty(false);
  Communicator myCommunicator;
  /**
   * VBox holds the messages.
   */
  protected VBox messagesBox;
  /**
   * TextField holds for where the players enter their messages
   */
  protected TextField textField;
  /**
   * Leaderboard to show the current players score
   */
  protected Leaderboard localLeaderboard;
  SimpleListProperty<Pair<String, Integer>> myMultPlScores;
  ArrayList<Pair<String, Integer>> myMultPlScoresArrayList = new ArrayList<>();
  SimpleListProperty<Pair<String, Integer>> myLocalMultPlScores;

  private HashMap<String, GameBoard> playerAndBoard;

  /**
   * Create a new MultiplayerScene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
    this.myCommunicator = gameWindow.getCommunicator();
    this.playerAndBoard = new HashMap<>();
    myMultPlScores = new SimpleListProperty();
    myLocalMultPlScores = new SimpleListProperty();

  }

  //


  /**
   * * this method handles the received messages form the server and then calling method depending
   * on the message
   *
   * @param message a {@link java.lang.String} object
   */
  protected void handleIncomingMessages(String message) {
    myLogger.info("Received message: {}", message);
    String[] components = message.split(" ", 2);
    switch (components[0]) {
      case "MSG":
        displayMessage(components[1]);
        break;
      case "SCORES":
        updateScores(components[1]);
        break;

      case "BOARD":
        updatePlayerBoard(components[1]);
        break;
      case "PIECE":
        myLogger.info("this is handled in the MultiplayerGame Class. ");
        break;
      default:
        myLogger.warn("Unhandled message type: {}", components[0]);
    }
  }

  /**
   * this method displays the messages when the game in progress
   *
   * @param content string which is the sent messages
   */
  private void displayMessage(String content) {
    myLogger.info("the rec mes is : ", content);
    if (messagesBox.getChildren().size() > 2 && messagesBox.getChildren().get(2) instanceof Text) {
      Text latestMessage = (Text) messagesBox.getChildren().get(2);
      latestMessage.setText("Last message: " + content);
      latestMessage.setVisible(true);
    } else {
      // Log an error  where the latestMessage is not available
      myLogger.error("Expected Text node not found in messagesBox.");
    }
  }

  /**
   * this method update player the scores
   *
   * @param scores String
   */
  private void updateScores(String scores) {
    myLogger.info("Received scores: " + scores);
    ArrayList<Pair<String, Integer>> tempScoresList = new ArrayList<>();
    ArrayList<Pair<String, Integer>> allScoresList = new ArrayList<>();
    String[] scorePairs = scores.split("\n");

    for (String pair : scorePairs) {
      String[] parts = pair.split(":");
      if (parts.length < 3) {
        myLogger.warn("Skipping malformed score entry: {}", pair);
        continue;  // Skip malformed entries
      }

      String playerName = parts[0];
      String scoreStr = parts[1];
      String status = parts[2];

      try {
        int score = Integer.parseInt(scoreStr);
        allScoresList.add(new Pair<>(playerName, score));
        if (!status.equals("DEAD")) {
          // Only add non-dead players to the temp list for UI display
          tempScoresList.add(new Pair<>(playerName, score));
        }
      } catch (NumberFormatException e) {
        myLogger.error("Error parsing score for player {}: {}", playerName, e.getMessage());
      }
    }

    // Update the main scores list in a thread-safe manner
    myMultPlScoresArrayList.clear();
    myMultPlScoresArrayList.addAll(allScoresList); // This keeps all scores, including dead players
    myMultPlScoresArrayList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    tempScoresList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

    // Update observable list and UI component with filtered list
    myMultPlScores.set(FXCollections.observableArrayList(tempScoresList));
    myLogger.info("Updated UI scores, current list size: {}", myMultPlScoresArrayList.size());
    myLocalMultPlScores.set(FXCollections.observableArrayList(allScoresList));
  }

  /**
   * Updates or initializes a game board for a player based on provided serialized data.
   *
   * @param data The string format "playerName:boardValues", where boardValues are space-separated
   *             integers. Errors during data processing are logged.
   */


  private void updatePlayerBoard(String data) {
    // Splitting the incoming data to separate player name and board data
    String[] message = data.split(":", 2);
    if (message.length < 2) {
      myLogger.error("Invalid board data received: {}", data);
      return; // Early return if the data format is not as expected
    }

    String player = message[0].trim();
    String[] playerBoardValues = message[1].trim().split(" ");

    // Ensuring the GameBoard for the player exists in the map
    GameBoard gameBoard = playerAndBoard.get(player);
    if (gameBoard == null) {
      // Assuming dimensions and block size for new game board initialization
      gameBoard = new GameBoard(5, 5, 75, 75);  // Assuming 5x5 board and cell size of 75x75
      playerAndBoard.put(player, gameBoard);
    }

    // Parsing board values and updating the board
    try {
      int index = 0;
      for (int y = 0; y < 5; y++) {
        for (int x = 0; x < 5; x++) {
          int blockValue = Integer.parseInt(playerBoardValues[index++]);
          gameBoard.getGrid().set(x, y, blockValue);
        }
      }
    } catch (NumberFormatException e) {
      myLogger.error("Error parsing board values for player {}: {}", player, e.getMessage());
    } catch (ArrayIndexOutOfBoundsException e) {
      myLogger.error("Not enough values provided for the board of player {}", player);
    }

  }

  /**
   * Setup the Multiplayer game object and model
   */
  @Override
  public void setupGame() {
    game = new MultiplayerGame(myCommunicator, 5, 5);
  }


  /**
   * this method sets up the chat for the players within the scene.
   *
   * @return Vbox which is Chat UI
   */
  private VBox setupChatUI() {
    // Chat instructions
    Text chatInstruction = new Text("Press 'T' for chat");
    chatInstruction.getStyleClass().add("smalltitle");
    chatInstruction.setVisible(true);  // Initially visible

    // Text field for input
    textField = new TextField();
    textField.setOnKeyPressed(this::keyListener);
    textField.setVisible(false);  // Initially hide the text field

    // Display for the latest message sent
    Text latestMessage = new Text("Last message: ");
    latestMessage.getStyleClass().add("messages");
    latestMessage.setVisible(false);

    messagesBox = new VBox(10);
    messagesBox.setAlignment(Pos.BOTTOM_LEFT);
    messagesBox.getChildren().addAll(chatInstruction, textField, latestMessage);
    return messagesBox;
  }

  /**
   * this methos used to show and send the message
   *
   * @param message string
   */
  private void sendMessageAndDisplay(String message) {
    myCommunicator.send("MSG " + message);
    Text latestMessage = (Text) messagesBox.getChildren().get(2);
    latestMessage.setText("Last message: " + message);
    latestMessage.setVisible(true);
    textField.clear();
  }


  /**
   * an overRidden method of the keyListener which add some new functionality
   */
  protected void keyListener(KeyEvent keyEvent) {
    KeyCode code = keyEvent.getCode();
    if (isVisible.get()) {
      // Handle keys when chat is active
      switch (code) {
        case ESCAPE:
          textField.setVisible(false);
          textField.clear();
          isVisible.set(false);
          myLogger.info("ESC was pressed - Chat Closed");
          keyEvent.consume();
          Text chatInstruction = (Text) messagesBox.getChildren().get(0);
          chatInstruction.setVisible(true);

          break;
        case ENTER:
          sendMessage();
          keyEvent.consume();

          break;
        default:
          break;
      }
    } else {
      switch (code) {
        case X:
        case ENTER:
          blockClicked(this.board.getBlock(this.keyX, this.keyY));
          myLogger.info("X was pressed");
          break;
        case SPACE:
        case R:
          swap(this.board.getBlock(this.keyX, this.keyY));
          myLogger.info("R or Space was pressed");
          break;
        case Q:
        case Z:
        case OPEN_BRACKET:
          rotatePieceCounterClockwise();
          myLogger.info("Q, Z, or OPEN_BRACKET was pressed");
          break;
        case E:
        case C:
        case CLOSE_BRACKET:
          rotatePieceClockwise();
          myLogger.info("E, C, or CLOSE_BRACKET was pressed");
          break;
        case A:
        case LEFT:
          moveAimLeft();
          myLogger.info("A or Left was pressed");
          break;
        case D:
        case RIGHT:
          moveAimRight();
          myLogger.info("D or Right was pressed");
          break;
        case W:
        case UP:
          moveAimUp();
          myLogger.info("W or Up was pressed");
          break;
        case S:
        case DOWN:
          moveAimDown();
          myLogger.info("S or Down was pressed");
          break;
        case T:
          toggleTextField();
          break;
        case ESCAPE:
          gameEnd();
          exitToMenu();
          myCommunicator.send("DIE");
          myLogger.info("ESC was pressed - Exiting Game");
          keyEvent.consume();

          break;
        default:
          break;
      }
    }
    updateHover();
  }

  /**
   * this method used to to deal with the presed Enter and the calls the respnssible method for
   * sending the messages
   */
  private void sendMessage() {
    if (!textField.getText().isEmpty()) {
      sendMessageAndDisplay(textField.getText());
    }
    textField.setVisible(false);
    isVisible.set(false);
    Text chatInstruction = (Text) messagesBox.getChildren().get(0);
    chatInstruction.setVisible(true);
  }

  /**
   * this method used when T is pressed to set the chatting box
   */
  private void toggleTextField() {
    isVisible.set(!isVisible.get());
    textField.setVisible(isVisible.get());
    Text chatInstruction = (Text) messagesBox.getChildren().get(0);
    chatInstruction.setVisible(!isVisible.get());
    myLogger.info("Toggle TextField: isVisible now " + isVisible.get());

    if (isVisible.get()) {
      Platform.runLater(() -> {
        textField.requestFocus(); // Ensure focus is set after UI has updated
      });
    }
  }

  /**
   * this used when the game is ending
   */
  protected void gameEnd() {
    myLogger.info("Game Over");
    Multimedia.stopBackgroundMusic();
    game.gameStopping();
    myCommunicator.send("DIE");

  }

  /**
   * {@inheritDoc} *   Initialise the Multi player  scene
   */
  @Override
  public void initialise() {
    myLogger.info("Initialising MulTP");
    gameWindow.getScene().setOnKeyPressed(this::keyListener);

    game.start();
    myCommunicator.addListener(
        message -> Platform.runLater(() -> handleIncomingMessages(message.trim())));
    myCommunicator.send("SCORES");
  }

  /**
   * Build the MultiplayerScene window
   */
  public void build() {
    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    BorderPane borderPane = new BorderPane();
    borderPane.setPadding(new Insets(10));
    borderPane.getStyleClass().add("multP-background");
    root.getChildren().add(borderPane);
    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    borderPane.setCenter(board);
    currentPieceBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 6, gameWindow.getWidth() / 6);
    nextBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 6, gameWindow.getWidth() / 6);

    // Top - Game stats and title
    Text mainTitle = new Text("Multiplayer  Scene");
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
    HBox statsPane = new HBox(20, scoreLabel, livesLabel, levelLabel);
    statsPane.setAlignment(Pos.CENTER);
    VBox topContainer = new VBox(5, mainTitle, statsPane);
    topContainer.setAlignment(Pos.CENTER);
    borderPane.setTop(topContainer);
    myTimerBar = new Rectangle(gameWindow.getWidth() / 5, 10);
    myTimerBar.setFill(Color.ORCHID);
    StackPane timerPane = new StackPane(myTimerBar);
    timerPane.setAlignment(Pos.CENTER);

    VBox bottom = new VBox(10);
    bottom.getChildren().addAll(setupChatUI(), timerPane); // Add Timer and Chat UI
    bottom.setAlignment(Pos.CENTER);
    borderPane.setBottom(bottom);
    borderPane.setBottom(bottom);
    this.localLeaderboard = new Leaderboard();
    myLogger.info("myMultPlScores size is: " + myMultPlScores.size());

    this.localLeaderboard.getCurrentScoreList().bind(myMultPlScores);
    VBox piecesBoards = new VBox(10, localLeaderboard, currentPieceBoard, nextBoard);
    piecesBoards.setAlignment(Pos.CENTER_RIGHT);
    borderPane.setRight(piecesBoards);
    board.setOnBlockClick(this::blockClicked);
    game.setNextPieceListener(this::onNextPiece);
    currentPieceBoard.setOnBlockClick(this::rotate);
    currentPieceBoard.centerIndicator();
    board.setOnRightClicked(this::rotate);
    game.setMyLineClearedListener(this::lineClearing);
    game.setMyGameLoopListener(this::setOnGameLoop);
    game.setMyGameOverListener(myGame -> {
      gameEnd();
      this.gameWindow.startScoresMultP(this.game, myLocalMultPlScores);
    });
    nextBoard.setOnBlockClick(this::swap);
  }

}


