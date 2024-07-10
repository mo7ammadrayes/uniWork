package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/**
 * <p>GameOverListener interface.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public interface GameOverListener {

  /**
   * <p>onGameOver.</p>
   *
   * @param game and it is a Game object
   */
  void onGameOver(Game game);
}
