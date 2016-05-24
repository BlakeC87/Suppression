package firestormsoftwarestudio.suppression;

/**
 * Created by Blake on 2/13/2016.
 */
import android.content.Context;
import android.media.MediaPlayer;

public class Music {
    private static MediaPlayer mp = null;

    /** Stop old song and start new one */
    public static void play(Context context, int resource) {
        stop(context);

        // Start music only if not disabled in preferences
        if (Prefs.getMusic(context)) {
            mp = MediaPlayer.create(context, resource);
            mp.setLooping(true);
            mp.setVolume(0.5f, 0.5f);
            mp.start();
        }
    }

    /** Stop the music */
    public static void stop(Context context) {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }
}


