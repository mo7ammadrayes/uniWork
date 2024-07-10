package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * <p>LineClearedListener interface.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public interface LineClearedListener {

  /**
   * <p>lineClearing.</p>
   *
   * @param x a which is HashSet object
   */
   void lineClearing(HashSet<GameBlockCoordinate> x);
}
