package com.tranquilaudio.tranquilaudio_app;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.RawRes;

// TODO this service should be run in the foreground
// the service will need to be stopped manually.
/**
 * Wrapper class for the Android MediaPlayer.
 */
public final class AudioPlayerService extends Service {

    private static final String TAG = "AudioPlayerService";

    private MediaPlayer mediaPlayer;
    @RawRes private static final int DEFAULT_AUDIO = com.tranquilaudio
            .tranquilaudio_app.R.raw.sound_clip_1;

    private final IBinder mBinder = new MyBinder();

    // TODO communicate from the activity -> service with start command
    // communicate from service -> activity with broadcast recievers
    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        pausePlay();
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        this.mediaPlayer = MediaPlayer.create(getApplicationContext(), DEFAULT_AUDIO);
        return mBinder;
    }

    /**
     * Binder for teh AudioPlayerService.
     */
    class MyBinder extends Binder {
        AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    // TODO private method to broadcast state of teh MediaPlayer
    // so that it can be displayed in the UI

    @Override
    public void onDestroy() {
        // can I call this without breaking playback?
//        mediaPlayer.release();
    }

    private void pausePlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    private boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

}
