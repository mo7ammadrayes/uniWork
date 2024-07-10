package uk.ac.soton.comp1206;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>Multimedia class.</p>
 *
 * @author mohamadrayes
 * @version $Id: $Id
 */
public class Multimedia {

  private static MediaPlayer audioPlayer;
  private static MediaPlayer backgroundMusicPlayer;
  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /**
   * <p>playAudio.</p>
   *
   * @param file a {@link java.lang.String} object
   */
  public static void playAudio(String file) {
    try {
      Media audio = new Media(Multimedia.class.getResource("/sounds/" + file).toExternalForm());
      if (audioPlayer != null) {
        audioPlayer.stop(); // Stop previous sound effect if any
      }
      audioPlayer = new MediaPlayer(audio);
      audioPlayer.play();
    } catch (Exception e) {
      logger.error("Error playing audio file: " + file, e);
    }
  }

  /**
   * <p>playBackgroundMusic.</p>
   *
   * @param file a {@link java.lang.String} object
   */
  public static void playBackgroundMusic(String file) {
    try {
      Media bgMusic = new Media(Multimedia.class.getResource("/music/" + file).toExternalForm());
      if (backgroundMusicPlayer != null) {
        backgroundMusicPlayer.stop(); // Stop previous music if any
        backgroundMusicPlayer.dispose(); // Release the resources
      }
      backgroundMusicPlayer = new MediaPlayer(bgMusic);
      backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely
      backgroundMusicPlayer.play();
    } catch (Exception e) {
      logger.error("Error running the background music file: " + file, e);
    }

  }

  /**
   * <p>stopBackgroundMusic.</p>
   */
  public static void stopBackgroundMusic() {
    if (backgroundMusicPlayer != null) {
      backgroundMusicPlayer.stop();
    }
  }
}
