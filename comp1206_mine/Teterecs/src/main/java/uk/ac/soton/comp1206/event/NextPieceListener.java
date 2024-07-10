package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * <p>NextPieceListener interface.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public interface NextPieceListener {

  /**
   * <p>onNextPiece.</p>
   *
   * @param nextPiece which is a GamePiece object
   */
  void onNextPiece(GamePiece nextPiece);

}
