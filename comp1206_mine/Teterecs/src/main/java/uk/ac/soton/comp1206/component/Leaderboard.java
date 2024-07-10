package uk.ac.soton.comp1206.component;

import javafx.collections.ListChangeListener;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

/**
 * <p>Leaderboard class.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class Leaderboard extends ScoresList {

  /**
   * <p>Constructor for Leaderboard.</p>
   */
  public Leaderboard() {
    super();
    currentScoreList.addListener(
        (ListChangeListener.Change<? extends Pair<String, Integer>> c) -> this.update());


  }

  /**
   * {@inheritDoc} used to update the leaderboard and UI
   */
  @Override
  public void update() {
    getChildren().clear(); // Clear the current UI elements to refresh the display

    // Iterate over the scores in currentScoreList to create and add new UI elements
    for (Pair<String, Integer> myPair : currentScoreList) {
      Text name = new Text(myPair.getKey() + ": ");
      Text score = new Text(myPair.getValue().toString());
      score.getStyleClass().add("leaderboardtext");
      name.getStyleClass().add("leaderboardtext");

      VBox scoreEntry = new VBox(name, score); // Create a new VBox to hold the name and score Texts
      getChildren().add(scoreEntry); // Add the scoreEntry to the ScoresList's children

      // Limit the number of displayed scores to 10
      if (getChildren().size() == 10) {
        break;
      }
    }
  }
}

