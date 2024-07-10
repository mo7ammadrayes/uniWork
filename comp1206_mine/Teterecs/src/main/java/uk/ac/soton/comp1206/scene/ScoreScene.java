package uk.ac.soton.comp1206.scene;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * <p>ScoreScene class.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class ScoreScene extends BaseScene {

  private boolean onlineScoresLoading;


  private static final Logger logger = LogManager.getLogger(ScoreScene.class);
  private final Game myGame;
  private SimpleListProperty<Pair<String, Integer>> myLocalScoreList;
  ArrayList<Pair<String, Integer>> localScorePairs;
  ObservableList<Pair<String, Integer>> observableLocalScores;
  private int myScore;
  ArrayList<Pair<String, Integer>> onlineScorePairs;

  private SimpleListProperty<Pair<String, Integer>> myOnlineScores = new SimpleListProperty<>();
  ScoresList localScoresList;
  ScoresList onlineScoreList;
  private SimpleListProperty<Pair<String, Integer>> myMultiPScores = new SimpleListProperty<>();
  Communicator myCommunicator;
  private final int MAX_SCORES = 10;
  private String name;


  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   * @param game       a {@link uk.ac.soton.comp1206.game.Game} object
   */
  public ScoreScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.myGame = game;
    this.localScorePairs = loadScores(); // Assuming this is a synchronous operation
    observableLocalScores = FXCollections.observableArrayList(localScorePairs);
    myLocalScoreList = new SimpleListProperty<>(observableLocalScores);

    myScore = game.scoreProperty().get(); // Get the final score
    this.onlineScorePairs = new ArrayList<>();
    this.myOnlineScores = new SimpleListProperty<>(
        FXCollections.observableArrayList(this.onlineScorePairs));
    // Initially, online scores are not loaded, so set this flag to true
    onlineScoresLoading = true;

    myCommunicator = gameWindow.getCommunicator();
    myScore = myGame.scoreProperty().get();
  }

  /**
   * <p>Constructor for ScoreScene.</p>
   * which is used when it is multplayer Game
   *
   * @param gameWindow a {@link uk.ac.soton.comp1206.ui.GameWindow} object
   * @param game       a {@link uk.ac.soton.comp1206.game.Game} object
   * @param x          a {@link javafx.beans.property.SimpleListProperty} object
   */
  public ScoreScene(GameWindow gameWindow, Game game, SimpleListProperty<Pair<String, Integer>> x) {
    super(gameWindow);
    this.myGame = game;
    this.localScorePairs = loadScores(); // Assuming this is a synchronous operation
    observableLocalScores = FXCollections.observableArrayList(localScorePairs);
    myLocalScoreList = new SimpleListProperty<>(observableLocalScores);

    myScore = game.scoreProperty().get(); // Get the final score
    this.onlineScorePairs = new ArrayList<>();
    this.myOnlineScores = new SimpleListProperty<>(
        FXCollections.observableArrayList(this.onlineScorePairs));
    // Initially, online scores are not loaded, so set this flag to true
    onlineScoresLoading = true;
    myCommunicator = gameWindow.getCommunicator();
    //System.out.println(myLocalScoreList);
    myScore = myGame.scoreProperty().get();
    this.myMultiPScores.set(x);
  }


  /**
   * write the local scores
   */
  public void writeScores() {
    File myFile = new File("myFile.txt");

    // Use FileWriter and BufferedWriter for writing to the myFile
    try (FileWriter myFileWriter = new FileWriter(myFile,
        false); // false to overwrite. true to append.
        BufferedWriter writer = new BufferedWriter(myFileWriter)) {

      for (Pair<String, Integer> score : myLocalScoreList) {
        writer.write(score.getKey() + " : " + score.getValue());
        writer.newLine();
      }
      this.myLocalScoreList.sort((x, y) -> y.getValue() - x.getValue());

    } catch (IOException e) {
      logger.error("Error writing scores to myFile: " + e.getMessage());
    }
  }


  /**
   * <p>loadScores.</p>
   * load the local score form the file or downlaod default one if it no file existed
   *
   * @return a {@link java.util.ArrayList} object
   */
  public static ArrayList<Pair<String, Integer>> loadScores() {
    Path path = Paths.get("myFile.txt");
    ArrayList<Pair<String, Integer>> loadedScores = new ArrayList<>();

    if (Files.exists(path)) {
      try {
        List<String> lines = Files.readAllLines(path);
        for (String line : lines) {
          String[] parts = line.split(" : ");
          loadedScores.add(new Pair<>(parts[0].trim(), Integer.parseInt(parts[1].trim())));
        }
      } catch (IOException | NumberFormatException e) {
        logger.error("Error while loading scores: " + e.getMessage());
      }
    } else {
      for (int i = 1; i < 11; i++) {
        int tmpScore = (i * 200) - 20;
        loadedScores.add(new Pair("Mohammad", tmpScore));
      }
      logger.info(
          "Scores file does not exist. A default one will be created to test the main function.");
    }
    loadedScores.sort((pair1, pair2) -> pair2.getValue() - pair1.getValue());

    return loadedScores;
  }

  /**
   * this method send the HISCORES to the server
   */
  void askingForOnlineScores() {
    onlineScoresLoading = true;
    myCommunicator.send("HISCORES");
  }


  /**
   * this method handles the received messages form the server and then calling method depending on
   * the message
   */
  private void setupCommunicationsListener() {
    myCommunicator.addListener(message -> Platform.runLater(() -> {
      if (message.startsWith("HISCORES")) {
        onlineScoresLoading = false;
        myOnlineScores.clear(); // Clears the existing scores
        String[] scores = message.substring("HISCORES ".length()).trim().split("\n");
        for (String scoreLine : scores) {
          String[] parts = scoreLine.trim().split(":");
          try {
            String name = parts[0].trim();
            int scoreValue = Integer.parseInt(parts[1].trim());
            myOnlineScores.add(new Pair<>(name, scoreValue));// Adding scores to the list
          } catch (NumberFormatException e) {
            logger.error("Failed to parse score value: " + parts[1], e);
          }
        }
        myOnlineScores.sort((pair1, pair2) -> pair2.getValue() - pair1.getValue());

        logger.info("The online score list size is: " + myOnlineScores.size() + "\n the ");
      } else if (message.startsWith("NEWSCORE")) {
        logger.info("High score submission confirmed: " + message);
        askingForOnlineScores();
      }
    }));
  }

  /**
   * sends the HISCORE with the name and the score to the server
   */
  private void writeOnlineScore() {
    myCommunicator.send("HISCORE " + this.name + ":" + myScore);
  }

  /**
   * this method used to check the local high score
   *
   * @param currentScore int
   * @return boolean
   */
  private boolean isNewHighScore(int currentScore) {
    if (myLocalScoreList.size() < MAX_SCORES) {
      return true; // If there's room for more scores
    }
    return myLocalScoreList.stream().anyMatch(score -> score.getValue() < currentScore);
  }

  /**
   * this method used to check the online high score
   *
   * @param currentScore int
   * @return boolean
   */
  private boolean isNewOnlineHighScore(int currentScore) {
    if (onlineScoresLoading) {
      // If we are still loading the online scores, we cannot determine if it's a new high score.
      logger.info("Online scores still loading. Cannot determine new online high score yet.");
      return false;
    }

    // Assuming that having fewer than MAX_SCORES means there's room for the score
    if (myOnlineScores.size() < MAX_SCORES) {
      logger.info("There's room for new scores in the online high scores list.");
      return true;
    }

    // Check if the current score is higher than any of the online high scores
    return myOnlineScores.stream().anyMatch(score -> score.getValue() < currentScore);
  }

  /**
   * this method create propmt for the players to enter thier names
   *
   * @return String of the player name
   */
  private String promptForPlayerName() {
    TextInputDialog dialog = new TextInputDialog("Player");
    dialog.setTitle("Adding your  Score!");
    dialog.setHeaderText("enter your score to check for the higher scores");
    dialog.setContentText(
        "Please enter your name (use 3 or more letters; or Anonymous will be used):");

    Optional<String> result = dialog.showAndWait();

    // Checking and handling the name length
    String playerName = result.orElse("Anonymous");
    if (playerName.length() < 3) {
      logger.info("The chosen name is less than 3 letters.");
      playerName = "Anonymous"; // Assign default if invalid
    }

    logger.info("Name entered or default used: " + playerName);
    return playerName;
  }

  /**
   * this method used to update the local high scores
   *
   * @param playerName String
   * @param score      int
   */
  private void updateHighScores(String playerName, int score) {
    myLocalScoreList.add(new Pair<>(playerName, score));

    // Sort in descending order of scores
    myLocalScoreList.sort((pair1, pair2) -> pair2.getValue().compareTo(pair1.getValue()));

    // Trim the list if it exceeds the maximum size
    while (myLocalScoreList.size() > MAX_SCORES) {
      myLocalScoreList.remove(myLocalScoreList.size() - 1);
    }
  }

  /**
   * this is the main method for handelling both online and local high scores by calling all the
   * related method on certain conditions
   *
   * @param gameScore int the game score
   */
  private void manageNewHighScore(int gameScore) {
    if ((isNewOnlineHighScore(gameScore) || isNewHighScore(gameScore)) && (myMultiPScores.isEmpty()
        || myMultiPScores == null)) {
      this.name = promptForPlayerName();
      logger.info("the name is " + this.name);
      // Check and update the local high scores list
      boolean isLocalHighScore = isNewHighScore(gameScore);
      if (isLocalHighScore) {
        updateHighScores(this.name, gameScore);
        writeScores(); //  this method updates the local high scores storage
      }

      // Defer online high score submission to after online scores are loaded
      // This check is incorporated in isNewOnlineHighScore
      if (isNewOnlineHighScore(gameScore)) {
        writeOnlineScore();


      }
    }
  }

  /**
   * {@inheritDoc} * Initialise the Scores Scene
   */
  @Override
  public void initialise() {
    Multimedia.playBackgroundMusic(
        "8-Bit NES Music Loop | Retro Pixel Game Music | Famicom Chiptune  Background Music | Royalty Free.mp3");
    logger.info("initialising the Score Scene scene");

    this.scene.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() == KeyCode.ESCAPE) {
        Multimedia.playAudio("transition.wav");
        gameWindow.startMenu();
        logger.info("Escape Pressed");
      }

    });
    askingForOnlineScores();
    setupCommunicationsListener();
    manageNewHighScore(myScore);

  }


  /**
   * {@inheritDoc} building the Scores Scene object
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var scorePane = new StackPane();
    scorePane.setMaxWidth(gameWindow.getWidth());
    scorePane.setMaxHeight(gameWindow.getHeight());
    scorePane.getStyleClass().add("scores-background");
    root.getChildren().add(scorePane);

    var mainPane = new BorderPane();
    scorePane.getChildren().add(mainPane);

    Text gameTitle = new Text("Game Over");
    gameTitle.getStyleClass().add("heading");
    Text highScoresTitle = new Text("High Scores");
    highScoresTitle.getStyleClass().add("heading");
    VBox titleBox = new VBox(gameTitle, highScoresTitle);
    titleBox.setAlignment(Pos.CENTER);

    Text yourScoreTitle = new Text("Your Score: ");
    yourScoreTitle.getStyleClass().add("title");
    Text yourScore = new Text(String.valueOf(myGame.getScore()));
    yourScore.getStyleClass().add("score");
    HBox scoreBox = new HBox(yourScoreTitle, yourScore);
    scoreBox.setAlignment(Pos.CENTER);

    VBox topSection = new VBox(10, titleBox, scoreBox);
    topSection.setAlignment(Pos.CENTER);
    mainPane.setTop(topSection);

    Text localScoresTitle = new Text("Top 10 Local Scores");
    localScoresTitle.getStyleClass().add("heading");
    localScoresList = new ScoresList();
    localScoresList.getCurrentScoreList().bind(myLocalScoreList);

    VBox localScoresContainer = new VBox(localScoresTitle, localScoresList);

    Text onlineScoresTitle = new Text("Top 10  Online Scores");
    onlineScoresTitle.getStyleClass().add("heading");
    onlineScoreList = new ScoresList();
    onlineScoreList.getCurrentScoreList().bind(this.myOnlineScores);
    VBox onlineScoresContainer = new VBox(onlineScoresTitle, onlineScoreList);

    HBox scoresContainer = new HBox(50);
    scoresContainer.setAlignment(Pos.CENTER);

    if (myMultiPScores != null && !myMultiPScores.isEmpty()) {
      Text mulTPScoresTitle = new Text("Multiplayer Scores");
      mulTPScoresTitle.getStyleClass().add("heading");
      ScoresList multPLeaderboard = new ScoresList();
      multPLeaderboard.getCurrentScoreList().bind(myMultiPScores);
      VBox multiplayerScoresContainer = new VBox(mulTPScoresTitle, multPLeaderboard);
      scoresContainer.getChildren().addAll(multiplayerScoresContainer, onlineScoresContainer);
    } else {
      scoresContainer.getChildren().addAll(localScoresContainer, onlineScoresContainer);
    }

    mainPane.setCenter(scoresContainer);

    Button backButton = new Button("Exit");
    backButton.getStyleClass().add("chatfeildbuttons");
    backButton.setOnAction(event -> this.gameWindow.startMenu());
    HBox buttonPlacement = new HBox(backButton);
    buttonPlacement.setAlignment(Pos.CENTER);
    mainPane.setBottom(buttonPlacement);

    localScoresList.reveal();
    onlineScoreList.reveal();

  }

}

