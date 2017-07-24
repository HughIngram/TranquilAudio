package com.tranquilaudio.tranquilaudio_app;

import android.app.Application;
import android.content.Intent;

import com.tranquilaudio.tranquilaudio_app.model.MediaControlClient;

/**
 * Application Implementation.
 */
public final class TranquilAudioApplication extends Application {

    private MediaControlClient mediaControlClient;

    /**
     * Get the MediaControlClient.
     * @return the mcc.
     */
    public MediaControlClient getMediaControlClient() {
        return mediaControlClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaControlClient = new MediaControlClient(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * Broadcast an intent to request the AudioPlayerService to broadcast its
     * status.
     */
    public void broadcastAudioPlayerServiceStatus() {
        final Intent intent = new Intent("TEST");
        sendBroadcast(intent);
    }

}
