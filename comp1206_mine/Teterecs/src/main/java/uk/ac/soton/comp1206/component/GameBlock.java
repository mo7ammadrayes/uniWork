package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class GameBlock extends Canvas {

  private static final Logger logger = LogManager.getLogger(GameBlock.class);

  /**
   * The set of colours for different pieces
   */
  public static final Color[] COLOURS = {
      Color.TRANSPARENT,
      Color.DEEPPINK,
      Color.RED,
      Color.ORANGE,
      Color.YELLOW,
      Color.YELLOWGREEN,
      Color.LIME,
      Color.GREEN,
      Color.DARKGREEN,
      Color.DARKTURQUOISE,
      Color.DEEPSKYBLUE,
      Color.AQUA,
      Color.AQUAMARINE,
      Color.BLUE,
      Color.MEDIUMPURPLE,
      Color.PURPLE
  };

  private final GameBoard gameBoard;

  private final double width;
  private final double height;

  /**
   * The column this block exists as in the grid
   */
  private final int x;

  /**
   * The row this block exists as in the grid
   */
  private final int y;

  /**
   * The value of this block (0 = empty, otherwise specifies the colour to render as)
   */
  private final IntegerProperty value = new SimpleIntegerProperty(0);

  private BooleanProperty isHovered = new SimpleBooleanProperty();
  private boolean isCentre;
  private AnimationTimer myAnimationTimer;

  /**
   * Create a new single Game Block
   *
   * @param gameBoard the board this block belongs to
   * @param x         the column the block exists in
   * @param y         the row the block exists in
   * @param width     the width of the canvas to render
   * @param height    the height of the canvas to render
   */
  public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
    this.gameBoard = gameBoard;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;

    //A canvas needs a fixed width and height
    setWidth(width);
    setHeight(height);

    //Do an initial paint
    paint();

    //When the value property is updated, call the internal updateValue method
    value.addListener(this::updateValue);

    isHovered.addListener((obs, oldVal, newVal) -> paint());

  }

  /**
   * When the value of this block is updated,
   *
   * @param observable what was updated
   * @param oldValue   the old value
   * @param newValue   the new value
   */
  private void updateValue(ObservableValue<? extends Number> observable, Number oldValue,
      Number newValue) {
    paint();
  }

  /**
   * Handle painting of the block canvas
   */
  public void paint() {
    //If the block is empty, paint as empty
    if (value.get() == 0) {
      paintEmpty();
    } else {
      //If the block is not empty, paint with the colour represented by the value
      paintColor(COLOURS[value.get()]);
    }
    if (isHovered.get()) {
      paintHover();
    }
    if (isCentre) {
      centrePainting();
    }
  }

  /**
   * Paint this canvas empty, but account for hover state.
   */
  public void paintEmpty() {
    var gc = getGraphicsContext2D();

    // Clear the area
    gc.clearRect(0, 0, width, height);

    // Fill with default color
    gc.setFill(Color.color(0, 0, 0, 0.5)); // Assuming empty blocks are white
    gc.fillRect(0, 0, width, height);

    gc.setStroke(Color.WHITE); // Default border color
    gc.setLineWidth(1); // Default border thickness
    //}

    // Draw the border
    gc.strokeRect(0, 0, width, height);

  }

  /**
   * Paint this canvas with the given colour, but account for hover state.
   *
   * @param colour the colour to paint
   */
  public void paintColor(Paint colour) {
    var gc = getGraphicsContext2D();

    // Clear the area
    gc.clearRect(0, 0, width, height);

    // Fill with block color
    gc.setFill(colour);
    gc.fillRect(0, 0, width, height);

// Add a radial gradient for a bulging effect
    RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.5, 0.9, true, CycleMethod.NO_CYCLE,
        new Stop(0, Color.color(1, 1, 1, 0.2)),
        new Stop(1, Color.color(1, 1, 1, 0)));
    gc.setFill(gradient);
    gc.fillOval(0, 0, width, height);

    gc.setFill(Color.color(0.7, 0.7, 1, 0.5));
    gc.fillRect(0, 0, this.width, 3);  // top highlight
    gc.fillRect(0, 0, 3, height);      // left highlight
  }


  /**
   * Get the column of this block
   *
   * @return column number
   */
  public int getX() {
    return x;
  }

  /**
   * Get the row of this block
   *
   * @return row number
   */
  public int getY() {
    return y;
  }

  /**
   * Get the current value held by this block, representing it's colour
   *
   * @return value
   */

  /**
   * Bind the value of this block to another property. Used to link the visual block to a
   * corresponding block in the Grid.
   *
   * @param input property to bind the value to
   */
  public void bind(ObservableValue<? extends Number> input) {
    value.bind(input);
  }

  /**
   * <p>mySetHover.</p>
   * this method set the  field isHoverd and do  pint after changing it
   *
   * @param hover a boolean
   */
  public void mySetHover(boolean hover) {

    isHovered.set(hover);
    paint();
  }

  /**
   * this method for painting the hovered blocks
   */
  private void paintHover() {
    var gc = this.getGraphicsContext2D();
    if (isHovered.get()) {
      gc.setFill(Color.color(1, 1, 1, 0.5));
      gc.fillRect(0, 0, width, height);
    }

  }

  /**
   * <p>setCentre.</p>
   * his method set the  field isCentre
   *
   * @param x a boolean
   */
  public void setCentre(boolean x) {
    this.isCentre = x;
  }

  /**
   * <p>isCentre.</p>
   *
   * @return a boolean
   */

  /**
   * this method for painting the centre of the  blocks
   */
  public void centrePainting() {
    var gc = this.getGraphicsContext2D();
    gc.setFill(Color.color(1, 1, 1, 0.8));
    gc.fillOval(this.width / 4, this.height / 4, this.width / 2, this.height / 2);
  }

  /**
   * fadeOut method .
   */
  public void fadeOut() {
    myAnimationTimer = new AnimationTimer() {
      private double x = 1.0; // Start fully opaque

      @Override
      public void handle(long now) {
        if (x > 0) {
          var gc = getGraphicsContext2D();
          gc.setFill(Color.YELLOW.deriveColor(0, 0, 1, x)); // Apply current x
          gc.fillRect(0, 0, width, height);

          // Decrease x to fade the flash effect
          x -= 0.07; // Adjust this value to control the speed of the fade
        } else {
          this.stop(); // Stop the timer after the flash has faded
          GameBlock.this.paintEmpty();
        }
      }
    };
    myAnimationTimer.start();
  }

}

