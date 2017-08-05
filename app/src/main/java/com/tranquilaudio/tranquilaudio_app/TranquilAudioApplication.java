package com.tranquilaudio.tranquilaudio_app;

import android.app.Application;

import com.tranquilaudio.tranquilaudio_app.model.AudioScene;
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
        mediaControlClient.close();
    }

    /**
     * Broadcast an intent to request the AudioPlayerService to broadcast its
     * status.
     */
    public void broadcastAudioPlayerServiceStatus() {
        mediaControlClient.requestStatus();
    }

    /**
     *  Close the AudioPlayerService.
     */
    public void closeService() {
        mediaControlClient.close();
    }

    /**
     * Get the track which was playing when the Service was last disconnected.
     * @return
     */
    public AudioScene getLastPlayed() {
        return mediaControlClient.getLastPlayed();
    }

}
