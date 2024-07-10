package uk.ac.soton.comp1206.component;

import java.util.HashSet;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClicked;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A GameBoard is a visual component to represent the visual GameBoard. It extends a GridPane to
 * hold a grid of GameBlocks.
 * <p>
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming
 * block. It also be linked to an external grid, for the main game board.
 * <p>
 * The GameBoard is only a visual representation and should not contain game logic or model logic in
 * it, which should take place in the Grid.
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class GameBoard extends GridPane {

  private static final Logger logger = LogManager.getLogger(GameBoard.class);
  private GameBlock currentHoverBlock;

  /**
   * Number of columns in the board
   */
  private final int cols;

  /**
   * Number of rows in the board
   */
  private final int rows;

  /**
   * The visual width of the board - has to be specified due to being a Canvas
   */
  private final double width;

  /**
   * The visual height of the board - has to be specified due to being a Canvas
   */
  private final double height;

  /**
   * The grid this GameBoard represents
   */
  final Grid grid;

  /**
   * The blocks inside the grid
   */
  GameBlock[][] blocks;

  /**
   * The listener to call when a specific block is clicked
   */
  private BlockClickedListener blockClickedListener;
  private RightClicked myRightClicked;

  /**
   * Create a new GameBoard, based off a given grid, with a visual width and height.
   *
   * @param grid   linked grid
   * @param width  the visual width
   * @param height the visual height
   */
  public GameBoard(Grid grid, double width, double height) {
    this.cols = grid.getCols();
    this.rows = grid.getRows();
    this.width = width;
    this.height = height;
    this.grid = grid;

    //Build the GameBoard
    build();
  }

  /**
   * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows,
   * along with the visual width and height.
   *
   * @param cols   number of columns for internal grid
   * @param rows   number of rows for internal grid
   * @param width  the visual width
   * @param height the visual height
   */
  public GameBoard(int cols, int rows, double width, double height) {
    this.cols = cols;
    this.rows = rows;
    this.width = width;
    this.height = height;
    this.grid = new Grid(cols, rows);

    //Build the GameBoard
    build();
  }

  /**
   * Get a specific block from the GameBoard, specified by it's row and column
   *
   * @param x column
   * @param y row
   * @return game block at the given column and row
   */
  public GameBlock getBlock(int x, int y) {
    return blocks[x][y];
  }

  /**
   * Build the GameBoard by creating a block at every x and y column and row
   */
  protected void build() {
    logger.info("Building grid: {} x {}", cols, rows);

    setMaxWidth(width);
    setMaxHeight(height);

    setGridLinesVisible(true);

    blocks = new GameBlock[cols][rows];

    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        createBlock(x, y);
      }
    }
  }

  /**
   * Create a block at the given x and y position in the GameBoard
   *and setting the mouse events
   * @param x column
   * @param y row
   * @return a {@link uk.ac.soton.comp1206.component.GameBlock} object
   */
  protected GameBlock createBlock(int x, int y) {
    var blockWidth = width / cols;
    var blockHeight = height / rows;

    //Create a new GameBlock UI component
    GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

    //Add to the GridPane
    add(block, x, y);

    //Add to our block directory
    blocks[x][y] = block;

    //Link the GameBlock component to the corresponding value in the Grid
    block.bind(grid.getGridProperty(x, y));

    block.setOnMouseClicked(
        (e) -> {
          if (e.getButton() == MouseButton.PRIMARY) {
            this.blockClicked(block);
          } else {
            this.rightClicked(block);
          }

        });
    block.setOnMouseEntered(
        (e) -> this.hover(block));
    block.setOnMouseExited(
        (e) -> this.notHover(block));




    return block;
  }


  /**
   * Sets the RightClickedListener for the GameBoard
   *
   * @param rightClickedListener which is RightClicked object.
   */
  public void setOnRightClicked(RightClicked rightClickedListener) {
    this.myRightClicked = rightClickedListener;
  }

  /**
   * When a Block is RightClicked, this wil trigger
   *
   * @param block of GameBlock object
   */
  private void rightClicked(GameBlock block) {
    logger.info("Block Right clicked: {}", block);

    if(myRightClicked != null) {
      myRightClicked.setOnRightClicked(block);
    }
  }


  /**
   * Set the listener to handle an event when a block is clicked
   *
   * @param listener listener to add
   */
  public void setOnBlockClick(BlockClickedListener listener) {
    this.blockClickedListener = listener;
  }

  /**
   * Triggered when a block is clicked. Call the attached listener.
   *
   * @param block block clicked on
   */
  private void blockClicked(GameBlock block) {
    logger.info("Block clicked: {}", block);

    if (blockClickedListener != null) {
      blockClickedListener.blockClicked(block);
    }
  }

  /**
     * this method used to Indicate  the center of a block.
     */
    public void centerIndicator (){
    this.getBlock(1,1).setCentre(true);
      logger.info("set the center of the block");

    }
    /**
     * <p>fadeOut.</p>
     *
     * @param myCoordinate a {@link java.util.HashSet} object
     */
    public void fadeOut(HashSet<GameBlockCoordinate >myCoordinate){
    logger.info("fading out for each coordinates");
    for (GameBlockCoordinate n:myCoordinate){
     getBlock(n.getX(), n.getY()).fadeOut();
    }
    }

  /**
   * hover method used to set the hover depenidg on the gameBlock
   * @param  block GameBlock
   */
  public void hover(GameBlock block) {
    if (!(this instanceof PieceBoard)) {
      // refresh the hover block
      if (this.currentHoverBlock != null) {
        this.notHover(this.currentHoverBlock);
      }
      this.currentHoverBlock = block;
      block.mySetHover(true);
    }
  }

  /**
   * set the unhoving block's hover to false;
   *
   * @param block GameBlock
   */
  public void notHover(GameBlock block) {
    block.mySetHover(false);
  }

  /**
     * <p>Getter for the field <code>grid</code>.</p>
     *
     * @return a {@link uk.ac.soton.comp1206.game.Grid} object
     */
    public Grid getGrid(){
    return grid;
    }
  }


