package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * <p>PieceBoard class.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class PieceBoard extends GameBoard {


  /**
   * <p>Constructor for PieceBoard.</p>
   *
   * @param cols   a int
   * @param rows   a int
   * @param width  a double
   * @param height a double
   */
  public PieceBoard(int cols, int rows, double width, double height) {
    super(cols, rows, width, height);
  }


  /**
   * <p>showPiece.</p>
   * this method used to show the pieces in the specifed PieceBoards
   *
   * @param piece a {@link uk.ac.soton.comp1206.game.GamePiece} object
   */
  public void showPiece(GamePiece piece) {
    grid.clear();
    // Calculate start positions to center the piece in the 3x3 grid

    // Place piece blocks on the grid, adjusted for centering
    for (int i = 0; i < piece.getBlocks().length; i++) {
      for (int j = 0; j < piece.getBlocks()[i].length; j++) {
        if (piece.getBlocks()[i][j] != 0) {
          grid.set(i, j, piece.getBlocks()[i][j]);
        }
      }
    }
  }
}
