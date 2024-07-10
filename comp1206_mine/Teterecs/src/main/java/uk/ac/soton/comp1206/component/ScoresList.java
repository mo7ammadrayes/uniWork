package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * <p>ScoresList class.</p>
 * used to represnt the scores in the scores scene
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class ScoresList extends VBox {

  SimpleListProperty<Pair<String, Integer>> currentScoreList;

  /**
   * <p>Constructor for ScoresList.</p>
   */
  public ScoresList() {
    this.getStyleClass().add("scorelist");
    this.setSpacing(2);

    currentScoreList = new SimpleListProperty<>(FXCollections.observableArrayList());
    currentScoreList.addListener((observable, oldValue, newValue) -> {
      update(); // Call update to refresh the UI based on the new list
    });

  }


  /**
   * <p>Getter for the field <code>currentScoreList</code>.</p>
   *
   * @return a {@link javafx.beans.property.SimpleListProperty} object
   */
  public SimpleListProperty<Pair<String, Integer>> getCurrentScoreList() {
    return this.currentScoreList;
  }

  /**
   * <p>update the UI .</p>
   */
  public void update() {
    getChildren().clear(); // Clear the current UI elements to refresh the display

    // Iterate over the scores in currentScoreList to create and add new UI elements
    for (Pair<String, Integer> myPair : currentScoreList) {
      Text name = new Text(myPair.getKey() + ": ");
      Text score = new Text(myPair.getValue().toString());
      score.getStyleClass().add("smalltitle");
      name.getStyleClass().add("smalltitle");

      VBox scoreEntry = new VBox(name, score); // Create a new VBox to hold the name and score Texts
      getChildren().add(scoreEntry); // Add the scoreEntry to the ScoresList's children

      // Limit the number of displayed scores to 10
      if (getChildren().size() == 10) {
        break;
      }
    }
  }

  /**
   * <p>reveal the scoresList with some effects.</p>
   */
  public void reveal() {
    FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), this);
    fadeTransition.setFromValue(0); // Start fully transparent
    fadeTransition.setToValue(1); // End fully opaque
    fadeTransition.play();
  }

}
