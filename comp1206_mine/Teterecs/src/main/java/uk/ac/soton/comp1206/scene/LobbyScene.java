package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * <p>LobbyScene class.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class LobbyScene extends BaseScene {

  /**
   * Constant <code>myLogger</code>
   */
  public static final Logger myLogger = LogManager.getLogger(LobbyScene.class);
  private Timer myTimer;
  private Communicator myCommunicator;
  private StringProperty myCurrentChanel = new SimpleStringProperty();
  /**
   * StringProperty hold the curren player name
   */
  public StringProperty myPlayerName;
  /**
   * a bollean used to check for if it joined or nor
   */
  public boolean joinedCheck;
  private ArrayList<String> myCurrentChannelsList;
  /**
   * Constant <code>myPlayers</code>
   */
  protected static ArrayList<String> myPlayers;
  private BooleanProperty host = new SimpleBooleanProperty();
  private VBox myGamesVBox;
  /**
   * The main borderpane
   */
  private BorderPane myMainPane;
  private final VBox myChannelNames;
  private BorderPane mySecondPane;

  private ArrayList<String> myMessages;
  /**
   * VBox containing the myMessagesVBox
   */
  private VBox myMessagesVBox;
  private Boolean textFieldOpen = false;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    this.myTimer = new Timer();
    this.myCommunicator = gameWindow.getCommunicator();
    myCurrentChannelsList = new ArrayList<>();
    myPlayers = new ArrayList<>();
    myPlayerName = new SimpleStringProperty();
    myChannelNames = new VBox();
    myMessages = new ArrayList<>();
  }

  /**
   * this method stops the timer and clears the filed list
   */
  void clear() {
    if (this.myTimer != null) {
      this.myTimer.purge();
      this.myTimer.cancel();
      this.myTimer = null;
    }
    myMessages.clear();
    myPlayers.clear();
  }


  /**
   * this method handles the received messages form the server and then calling method depending on
   * the message
   *
   * @param message which is String object
   */
  public void getMessage(String message) {
    myLogger.info("Received message: " + message);
    if (message == null || message.trim().isEmpty()) {
      myLogger.info("Received message is null or empty.");
      return;
    }

    String[] myMessage = message.trim().split(" ", 2); // Split on the first space
    String command = myMessage[0];
    String data = myMessage.length > 1 ? myMessage[1] : "";
    myLogger.info("data is " + data);
    switch (command) {
      case "HOST":
        host.set(true);
      case "CHANNELS":
        channelsUpdate(data);
        break;
      case "USERS":
        receivingUser(data);
        break;
      case "MSG":
        myMessages.add(data);
        receiveMSG(data);
        break;
      case "JOIN": {
        askForJoin(data);
      }
      break;
      case "NICK":
        String[] parts = data.split(":");
        if (parts.length > 1) {
          nickReceive(parts[1]); // Safely access the second element if it exists
        } else {
          myLogger.error("Received NICK command with invalid data format: " + data);
          // Optionally handle the error more gracefully here
        }
        break;
      case "PARTED":
        leaveChannel();
        break;
      case "START":
        startGame();
        break;
      case "ERROR":
        receiveError(myMessage);
        break;
      default:
        myLogger.info("Unknown command received: " + command);
    }
  }

  /**
   * this handles the ERROR received form the server
   *
   * @param x string array
   */
  private void receiveError(String[] x) {
    Alert alert = new Alert(AlertType.ERROR, x[1], new ButtonType[0]);
    alert.showAndWait();

  }

  /**
   * this method used to update the channels when reciving LIST
   *
   * @param data string
   */
  void channelsUpdate(String data) {
    Set<String> newChannels = new HashSet<>(Arrays.asList(data.split("\\n")));
    myCurrentChannelsList.clear();
    myCurrentChannelsList.addAll(newChannels);
    Platform.runLater(this::addChannels);
  }


  /**
   * this method used to join a cetrain channel
   *
   * @param channelName a {@link java.lang.String} object
   */
  public void askForJoin(String channelName) {
    myLogger.info("in arg channelName is" + channelName);
    myCurrentChanel.set(channelName);
    this.joinedCheck = (true);
    Multimedia.playAudio("message.wav");
    joinChannel(channelName);

  }

  /**
   * this method used to start  MultiplayerGame
   */

  private void startGame() {
    this.clear();
    this.gameWindow.startMultiplayer();
  }

  /**
   * this method ask For the Users.
   */
  public void askForUsers() {
    this.myCommunicator.send("USERS");
  }

  /**
   * this method handles the recieved USERS   *
   *
   * @param message String object
   */
  public void receivingUser(String message) {
    myPlayers.clear();
    String[] users = message.split("\n");
    for (String name : users) {
      if (!myPlayers.contains(name)) {
        myPlayers.add(name);
      }

    }
    getPlayers();

    Multimedia.playAudio("message.wav");
  }


  /**
   * this method handles the recieved NICK   *
   *
   * @param newNick a String object
   */
  public void nickReceive(String newNick) {
    myPlayerName.set(newNick);
    askForUsers();
  }


  /**
   * this method used to Refresh  the channels list
   */
  public void myRefresh() {
    TimerTask refresh =
        new TimerTask() {
          public void run() {
            LobbyScene.myLogger.info("Refreshing the lobby ");
            LobbyScene.this.myCommunicator.send("LIST");
          }
        };

    this.myTimer = new Timer();
    this.myTimer.schedule(refresh, 0, 5000);

  }

  /**
   * this method used when ESC is pressed in the scene initialization
   */
  private void clearAndShowMenu() {
    clear();  // Clear any timers or resources
    gameWindow.startMenu();  // Show the main menu
    myLogger.info(" in clearAndShowMenu");
  }


  /**
   * this method used when you  host a game
   */
  private void createChannel() {
    HBox enterChannelBox = new HBox();
    TextField enterChannelField = new TextField();
    enterChannelBox.getChildren().add(enterChannelField);
    myGamesVBox.getChildren().add(enterChannelBox);  // Adds the text field container to the UI

    // Handling the action of entering a channel name
    enterChannelField.setOnAction(event -> {
      String channelName = enterChannelField.getText().trim();
      myCommunicator.send("CREATE " + channelName);
      myRefresh();

      if (!channelName.isEmpty() && !joinedCheck && !myCurrentChannelsList.contains(channelName)) {
        host.set(true);
        textFieldOpen = false;
        joinedCheck = true;
        myCurrentChanel.set(channelName);

        // Add the new channel to the current channels list and update the UI
        myCurrentChannelsList.add(channelName);
        buildTextBox(); // Set up the chat box for the new channel
        myLogger.info("Channel created: {}, you are the host: {}", channelName, host);
        refresh();
        myGamesVBox.getChildren()
            .remove(enterChannelBox); // Correctly remove the text field box after use
      }
    });

    // Handle esc key to close the text field without adding a channel
    enterChannelField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        myGamesVBox.getChildren().remove(enterChannelBox);
        textFieldOpen = false;
        event.consume();
      }
    });

    enterChannelField.setPromptText("Enter new channel name");
    enterChannelField.requestFocus();
  }

  /**
   * this method responsible for showing the channels
   */


  private void addChannels() {
    // Clear existing buttons to avoid duplicates
    myChannelNames.getChildren().clear();

    // Re-populate myChannelNames with current channels
    for (String channelName : myCurrentChannelsList) {
      Button channelButton = new Button(channelName);
      channelButton.setOnAction(event -> myCommunicator.send("JOIN " + channelName));
      if (channelName.equals(myCurrentChanel.get())) {
        channelButton.getStyleClass().add("siu");

      } else {
        channelButton.getStyleClass().add("host-button");
      }
      myChannelNames.getChildren().add(channelButton);

    }

    // Check if myChannelNames is already a child of myGamesVBox
    if (!myGamesVBox.getChildren().contains(myChannelNames)) {
      myGamesVBox.getChildren().add(myChannelNames);
    }
  }

  /**
   * this implemet the chanel leaving logic
   */
  public void leaveChannel() {
    myMessages.clear();
    myPlayers.clear();
    host.set(false);
    joinedCheck = false;
    myCurrentChanel.set(null);
    Platform.runLater(() -> {
      myMainPane.setRight(null);
      myMessagesVBox.getChildren().clear();
      addChannels();
    });
    myRefresh();
  }

  /**
   * this method handles the UI of joining a channel
   *
   * @param channelName String
   */
  private void joinChannel(String channelName) {
    // Clear the current messages from the previous channel
    myMessages.clear();

    myLogger.info("joinedCheck is" + joinedCheck);
    if (joinedCheck) {
      buildTextBox();  // This sets up the chat UI for the new channel

      // Request the list of current players in the channel
      getPlayers();

      // Set the current channel to the new one
      myCurrentChanel.set(channelName);

      // Log the channel join action
      myLogger.info("You are currently in {} channel", channelName);

      myRefresh();
      refresh();
    }
  }

  /**
   * this build the text box used when you are in a channel
   */
  private void buildTextBox() {
    // Set up main pane and message box
    mySecondPane = new BorderPane();
    myMessagesVBox = new VBox();
    Text intro = new Text("Welcome to the lobby. \n"
        + "Type /nick NewName to change your name.");
    intro.getStyleClass().add("smalltitle");
    myMessagesVBox.getChildren().add(intro);

    // Configure scroll pane for messages
    ScrollPane scroller = new ScrollPane(myMessagesVBox);
    scroller.setFitToWidth(true);
    scroller.setOpacity(0.5); // Adjusted for better visibility
    mySecondPane.setCenter(scroller);

    // Configure input box
    HBox inputBox = setupInputBox(); // Refactored method for creating input box

    mySecondPane.setBottom(inputBox);
    myMainPane.setRight(mySecondPane);
  }

  /**
   * this is used to build the user input Box
   *
   * @return Hbox
   */
  private HBox setupInputBox() {
    HBox inputBox = new HBox(10);

    TextField inputField = new TextField();
    inputField.setPromptText("Send a message");
    Button sendButton = new Button("Send");
    sendButton.getStyleClass().add("chatfeildbuttons");

    sendButton.setOnAction(event -> {
      check(inputField.getText());
      inputField.clear();
    });
    inputBox.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        check(inputField.getText());
        inputField.clear();
      }
    });
    Button quitButton = new Button("Leave Game");
    quitButton.getStyleClass().add("chatfeildbuttons");

    quitButton.setOnAction(event -> myCommunicator.send("PART"));

    Button startGameButton = new Button("Start Game");
    startGameButton.getStyleClass().add("chatfeildbuttons");

    startGameButton.visibleProperty().bind(host);
    startGameButton.setOnAction(event -> myCommunicator.send("START"));

    inputBox.getChildren().addAll(inputField, quitButton, startGameButton, sendButton);

    return inputBox;
  }

  /**
   * this method used to reffrech the timer
   */

  private void refresh() {
    myTimer.cancel();
    myTimer = new Timer();
  }

  /**
   * this method responsible for adding the player to  theUI
   */
  private void getPlayers() {
    FlowPane usersFlowPane = new FlowPane();
    for (String user : myPlayers) {
      Text name = new Text(user + " ");
      name.getStyleClass().add("smalltitle");
      usersFlowPane.getChildren().add(name);
    }
    mySecondPane.setTop(usersFlowPane);
  }

  /**
   * this used to check the  sent messages in the chat
   *
   * @param msg string
   */
  private void check(String msg) {
    if (msg.startsWith("/")) {
      String[] parts = msg.split(" ", 2);
      if (msg.contains("/nick")) {
        myCommunicator.send("NICK " + parts[1]);
        myLogger.info("Name changed to {}", parts[1]);
      }
    } else {
      myCommunicator.send("MSG " + msg);
      myLogger.info("Message {} sent", msg);
    }
  }

  /**
   * this method responsible for the recived messages in the chat.
   *
   * @param msg string
   */
  private void receiveMSG(String msg) {
    myLogger.info("Message: {} has been received", msg);
    String[] components = msg.split(":");
    //if(components.length < 2) return;
    Text messageText = new Text(components[0] + ": " + components[1]);
    myLogger.info("my messageText is: " + messageText);
    myMessagesVBox.getChildren().add(messageText);
    refresh();
  }


  /**
   * {@inheritDoc} *   Initialise the Lobby scene
   */
  @Override
  public void initialise() {
    Multimedia.playBackgroundMusic("lobby.mp3");
    scene.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        if (joinedCheck) {
          myCommunicator.send("PART");
        }
        clearAndShowMenu();  // Always return to the main menu

      }

    });

    myCommunicator.addListener(
        (message) -> Platform.runLater(
            () -> this.getMessage(message.trim())));
    myRefresh();

  }

  /**
   * builds the lobby scene
   */
  public void build() {
    myLogger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("lobby-background");
    root.getChildren().add(menuPane);

    myMainPane = new BorderPane();
    menuPane.getChildren().add(myMainPane);

    Text multiplayerText = new Text("Multiplayer");
    BorderPane.setAlignment(multiplayerText, Pos.CENTER);
    multiplayerText.setTextAlignment(TextAlignment.CENTER);
    multiplayerText.getStyleClass().add("bigtitle");
    VBox titleEtc = new VBox(8);
    titleEtc.getChildren().add(multiplayerText);
    titleEtc.setAlignment(Pos.CENTER);
    if (!myCurrentChanel.equals(null)) {
      Text currentChannel = new Text();
      currentChannel.textProperty().bind(myCurrentChanel);
      currentChannel.getStyleClass().add("smalltitle");
      titleEtc.getChildren().add(currentChannel);
    }
    myMainPane.setTop(titleEtc);

    myGamesVBox = new VBox();
    myGamesVBox.setSpacing(20);

    myMainPane.setLeft(myGamesVBox);

    Text currentGamesText = new Text("Current Games");
    currentGamesText.setTextAlignment(TextAlignment.CENTER);
    currentGamesText.getStyleClass().add("title");
    myGamesVBox.getChildren().add(currentGamesText);
    myGamesVBox.setPadding(new Insets(10, 10, 10, 10));

    var hostNewGameButton = new Button("Host New Game");
    hostNewGameButton.setAlignment(Pos.CENTER);
    hostNewGameButton.getStyleClass().add("host-button");
    myGamesVBox.getChildren().add(hostNewGameButton);

    hostNewGameButton.setOnAction(event -> {
      if (textFieldOpen) {
        return;
      }
      if (joinedCheck) {
        return;
      }
      createChannel();
    });
  }

}
