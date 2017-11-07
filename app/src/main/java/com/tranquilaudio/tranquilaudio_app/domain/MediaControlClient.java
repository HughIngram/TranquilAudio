package com.tranquilaudio.tranquilaudio_app.domain;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.tranquilaudio.tranquilaudio_app.data.AudioScene;

/**
 * Handles client-side communication to the AudioPlayerService.
 */
public final class MediaControlClient {

    /**
     * Use the scene with this ID by default.
     */
    public static final long DEFAULT_SCENE = 1;

    private final Context context;
    private boolean isConnected = false;
    private AudioScene lastPlayed;
    private AudioSceneLoader loader;

    private static final String TAG = "MediaControllerClient";

    /**
     * Default constructor.
     *
     * @param context the context.
     */
    public MediaControlClient(final Context context) {
        this.context = context;
        loader = new AudioSceneLoaderImpl(
                new SystemWrapperForModelImpl(context));
    }

    private final BroadcastReceiver playingTrackReceiver = new
            BroadcastReceiver() {
                @Override
                public void onReceive(
                        final Context context, final Intent intent) {
                    final long trackId = intent.getLongExtra(AudioPlayerService
                            .SCENE_ID_KEY, 0);
                    lastPlayed = loader.getScene(trackId);
                }
            };

    private final BroadcastReceiver becomingNoisyReceiver = new
            BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent
                        intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent
                            .getAction())) {
                        pause();
                    }
                }
            };

    private void setupReceivers() {
        context.registerReceiver(playingTrackReceiver, new IntentFilter(
                AudioPlayerService.BROADCAST_PLAYER_STATUS_ACTION));
        final IntentFilter intentFilter = new IntentFilter(AudioManager
                .ACTION_AUDIO_BECOMING_NOISY);
        context.registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void unregisterReceivers() {
        context.unregisterReceiver(playingTrackReceiver);
        context.unregisterReceiver(becomingNoisyReceiver);
    }

    private final ServiceConnection serviceConnection
            = new ServiceConnection() {
        @Override
        public void onServiceConnected(
                final ComponentName name, final IBinder service) {
            Log.d(TAG, "Service connected");
            requestStatus();
            isConnected = true;
            setupReceivers();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            Log.d(TAG, "Service disconnected?");
            isConnected = false;
            unregisterReceivers();
        }
    };

    private void bindService() {
        final Intent mediaPlayerIntent
                = new Intent(context, AudioPlayerService.class);
        context.bindService(
                mediaPlayerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Resume audio playback.
     */
    public void resume() {
        if (!isConnected) {
            bindService();
        }
        publishMediaControlIntent(AudioPlayerService.RESUME_ACTION, 0);
    }

    /**
     * Pause audio playback.
     */
    public void pause() {
        publishMediaControlIntent(AudioPlayerService.PAUSE_ACTION, 0);
    }

    /**
     * Request the AudioPlayerService to broadcast its status.
     */
    public void requestStatus() {
        publishMediaControlIntent(AudioPlayerService.REQUEST_STATUS_ACTION, 0);
    }

    /**
     * Close the AudioPlayerService.
     */
    public void close() {
        context.unbindService(serviceConnection);
        isConnected = false;
    }

    /**
     * Load and play the scene with the given ID.
     *
     * @param sceneId the ID of the scene to load.
     */
    public void loadScene(final long sceneId) {
        if (!isConnected) {
            bindService();
        }
        publishMediaControlIntent(
                AudioPlayerService.LOAD_NEW_TRACK_ACTION, sceneId);
    }

    private void publishMediaControlIntent(final String goalAction,
                                           final long sceneId) {
        final Intent intent = new Intent(context, AudioPlayerService.class);
        intent.setAction(goalAction);
        if (goalAction.equals(AudioPlayerService.LOAD_NEW_TRACK_ACTION)) {
            intent.putExtra(AudioPlayerService.SCENE_ID_KEY, sceneId);
        }
        // why am I calling startService instead of bindService?
        context.startService(intent);
    }

    /**
     * Get the last played track.
     *
     * @return the last played track.
     */
    public AudioScene getLastPlayed() {
        if (lastPlayed == null) {
            return loader.getScene(DEFAULT_SCENE);
        } else {
            return lastPlayed;
        }
    }

}
