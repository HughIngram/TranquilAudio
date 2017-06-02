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

    /**
     * The intent filter for pause / play status notifications.
     */
    public static final String NOTIFICATION
            = "com.tranquilaudio.tranquiladuio_app";

    /**
     * The intent extras key for NOTIFICATION.
     */
    public static final String NOTIFICATION_KEY = "STATUS";

    /**
     * Possible statuses for Media playback.
     */
    enum PlayerStatus { PAUSED, PLAYING }

    private static final String TAG = "AudioPlayerService";

    private MediaPlayer mediaPlayer;
    @RawRes private static final int DEFAULT_AUDIO = com.tranquilaudio
            .tranquilaudio_app.R.raw.sound_clip_1;

    private final IBinder mBinder = new MyBinder();

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

    @Override
    public void onDestroy() {
        // can I call this without breaking playback?
//        mediaPlayer.release();
    }

    /**
     * Pause or play the Audio track.
     *
     * @return the media playback status.
     */
    public void pausePlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            publishResult(PlayerStatus.PAUSED);
        } else {
            mediaPlayer.start();
            publishResult(PlayerStatus.PLAYING);
        }
    }

    private void publishResult(final PlayerStatus status) {
        final Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(NOTIFICATION_KEY, status);
        sendBroadcast(intent);
    }

}
