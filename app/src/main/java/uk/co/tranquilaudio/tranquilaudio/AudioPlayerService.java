package uk.co.tranquilaudio.tranquilaudio;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.Log;

// TODO this service should be run in the foreground
// as both a 'started' and a 'bound' service.
// the service will need to be stopped manually.
/**
 * Wrapper class for the Android MediaPlayer.
 */
public final class AudioPlayerService extends Service {

    private static final String TAG = "AudioPlayerService";

    MediaPlayer mediaPlayer;
    @RawRes final int DEFAULT_AUDIO = R.raw.sound_clip_1;

    private final IBinder mBinder = new MyBinder();

    // TODO communicate from the activity -> service with start command
    // communicate from service -> activity with broadcast recievers
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer == null) {
            this.mediaPlayer = MediaPlayer.create(getApplicationContext(), DEFAULT_AUDIO);

            // TODO note that Services run in the same thread as the activity which called them.
            /*  TODO the below will be needed for playback outside of res/raw
            this.mediaPlayer = new MediaPlayer();
            mediaPlayer.setSource...
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {

                }
            });
            */
        } else {
            pausePlay();
        }


        return START_REDELIVER_INTENT;
    }

    // When does this get called?
    @Override
    public IBinder onBind(Intent intent) {
        pausePlay();
        return mBinder;
    }

    public class MyBinder extends Binder {
        AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }


    // TODO private method to broadcast state of teh MediaPlayer

    @Override
    public void onDestroy() {
        // how
//        mediaPlayer.release();
    }

    private void pausePlay() {
        if (mediaPlayer != null) {  // reeee
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }



    // public final void setMedia( .. )

    private final boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    private final void start() {
        mediaPlayer.start();
    }

    private final void pause() {
        mediaPlayer.pause();
    }

}
