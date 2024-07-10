package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display
 * of the contents of the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside
 * the grid.
 * <p>
 * The Grid should be linked to a GameBoard for it's display.
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class Grid {

  private static final Logger logger = LogManager.getLogger(Grid.class.getName());

  /**
   * The number of columns in this grid
   */
  private final int cols;

  /**
   * The number of rows in this grid
   */
  private final int rows;

  /**
   * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
   */
  private final SimpleIntegerProperty[][] grid;

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    //Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * <p>canPlayPiece.</p>
   *
   * @param theGamePiece a {@link uk.ac.soton.comp1206.game.GamePiece} object
   * @param x            a int
   * @param y            a int
   * @return a boolean
   */
  public boolean canPlayPiece(GamePiece theGamePiece, int x, int y) {
    x -= 1;
    y -= 1;
    int[][] gamePieceGrid = theGamePiece.getBlocks();
    for (int i = 0; i < gamePieceGrid.length; i++) {
      for (int j = 0; j < gamePieceGrid[i].length; j++) {
        // Skip empty blocks of the piece
        if (gamePieceGrid[i][j] == 0) {
          continue;
        }
        // Check if the piece goes out of the grid bounds
        if (x + i >= getRows() || y + j >= getCols() || x + i < 0 || y + j < 0) {
          return false;
        }
        // Check if the grid position is already occupied
        if (get(x + i, y + j) != 0) {
          return false;
        }
      }
    }
    return true;
  }


  /**
   * <p>playPiece.</p>
   *
   * @param theGamePiece a  object of GamePiece
   * @param x            a int
   * @param y            a int
   */
  public void playPiece(GamePiece theGamePiece, int x, int y) {
    if (canPlayPiece(theGamePiece, x, y)) {
      x -= 1;
      y -= 1;
      logger.info("Placing the piece " + theGamePiece.toString() + " into our grid ");
      int[][] blocks = theGamePiece.getBlocks();
      for (int i = 0; i < blocks.length; i++) {
        for (int j = 0; j < blocks[i].length; j++) {
          if (blocks[i][j] != 0) {
            set(x + i, y + j, blocks[i][j]); // Correctly place each block of the piece on the grid
          }
        }
      }

    }

  }

  /**
   * used to claer the grid
   * <p>clear.</p>
   */
  public void clear() {
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        grid[x][y].set(0);
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x     column
   * @param y     row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      //Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      //No such index
      return -1;
    }
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

}
