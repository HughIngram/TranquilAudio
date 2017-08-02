package com.tranquilaudio.tranquilaudio_app.model;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.tranquilaudio.tranquilaudio_app.NotificationBuilder;
import com.tranquilaudio.tranquilaudio_app.TranquilAudioApplication;

import java.io.IOException;

/**
 * Wrapper class for the Android MediaPlayer.
 */
public final class AudioPlayerService extends Service {

    /**
     * The intent action for resume (as in resume) intents.
     */
    public static final String RESUME_ACTION
            = "com.tranquilaudio.tranquilaudio_app.ACTION_PLAY";

    /**
     * The intent action for pausing.
     */
    public static final String PAUSE_ACTION
            = "com.tranquilaudio.tranquilaudio_app.ACTION_PAUSE";

    /**
     * The intent action for loading and playing a new track.
     */
    static final String LOAD_NEW_TRACK_ACTION
            = "com.tranquilaudio.tranquilaudio_app.ACTION_LOAD_TRACK";

    /**
     * The intent action for closing the Audio Playback service.
     * Unlike the other actions, this one is watched by the Application.
     */
    public static final String CLOSE_ACTION
            = "com.tranquilaudio.tranquilaudio_app.ACTION_CLOSE";

    /**
     * The intent action for requesting this Service to broadcast its status.
     */
    public static final String REQUEST_STATUS_ACTION
            = "com.tranquilaudio.tranquilaudio_app.ACTION_REQUEST_STATUS";
    /**
     * The intent extra key corresponding to the ID of an audio track.
     */
    public static final String SCENE_ID_KEY
            = "com.tranquilaudio.tranquilaudio_app.SCENE_ID_KEY";

    /**
     * The intent action key for pause / resume status update notifications.
     */
    public static final String BROADCAST_PLAYER_STATUS_ACTION
            = "com.tranquilaudio.tranquiladuio_app.ACTION_UPDATE_PLAYER_STATUS";

    /**
     * The intent extras key for BROADCAST_PLAYER_STATUS_ACTION.
     */
    public static final String PLAYER_STATUS_EXTRA_KEY
            = "com.tranquilaudio.tranquilaudio_app.STATUS_KEY";

    private static final int ONGOING_NOTIFICATION_ID = 12345;

    private static final String TAG = "AudioPlayerService";

    private MediaPlayer mediaPlayer;
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private AudioScene currentScene;
    private AudioSceneLoader loader;


    @Override
    public void onCreate() {
        super.onCreate();
        loader = new AudioSceneLoaderImpl(new SystemWrapperForModelImpl(this));
        currentScene = loader.getScene(MediaControlClient.DEFAULT_SCENE);
    }

    // This gets called BEFORE the service has been bound.
    @Override
    public IBinder onBind(final Intent intent) {
        final Uri audioTrack = currentScene.getAudioURI(this);
        this.mediaPlayer
                = MediaPlayer.create(getApplicationContext(), audioTrack);
        initMediaSession();
        loadMedia(MediaControlClient.DEFAULT_SCENE);
        final NotificationBuilder builder = new NotificationBuilder();
        startForeground(ONGOING_NOTIFICATION_ID, builder
                .buildNotification(PlayerStatus.PAUSED, currentScene, mediaSession, this));
        return new Binder();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        final String action = intent.getAction();
        switch (action) {
            case RESUME_ACTION:
                playMedia();
                break;
            case PAUSE_ACTION:
                pauseMedia();
                break;
            case LOAD_NEW_TRACK_ACTION:
                final Long audioTrackId = intent.getLongExtra(
                        SCENE_ID_KEY, MediaControlClient.DEFAULT_SCENE);
                loadMedia(audioTrackId);
                playMedia();
                break;
            case CLOSE_ACTION:
                pauseMedia();
                broadcastPlayerStatus();
                requestClose();
                stopForeground(true);
                stopSelf();
                break;
            case REQUEST_STATUS_ACTION:
                broadcastPlayerStatus();
                break;
            default:
                throw new RuntimeException("unrecognised action");
        }
        return START_REDELIVER_INTENT;
    }

    private void requestClose() {
        ((TranquilAudioApplication) getApplication()).closeService();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy called.");
    }

    private void initMediaSession() {
        if (mediaSessionManager != null) {
            return;
        }
        mediaSessionManager = (MediaSessionManager)
                getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(
                getApplicationContext(), "AudioPlayer");
        mediaSession.setActive(true);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                playMedia();
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
            }

            @Override
            public void onStop() {
                super.onStop();
                // removeNotification()
                // stopSelf()
            }
        });
    }

    private void pauseMedia() {
        mediaPlayer.pause();
        publishResult();
    }

    // i.e. resume
    private void playMedia() {
        mediaPlayer.start();
        publishResult();
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        stopForeground(true);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaSessionManager = null;
        stopSelf();
        return super.onUnbind(intent);
    }

    /**
     * Load the given Audio Scene.
     *
     * @param audioSceneId the ID of the Audio Scene to load.
     */
    private void loadMedia(final long audioSceneId) {
        currentScene = loader.getScene(audioSceneId);
        final Uri audioTrack = currentScene.getAudioURI(this);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), audioTrack);
            mediaPlayer.prepare();
            publishResult();
        } catch (final IOException e) {
            throw new RuntimeException("could not load media");
        }
    }

    /**
     * Broadcasts the new status of the media player.
     */
    private void publishResult() {
        broadcastPlayerStatus();
        updateNotification();
    }

    private void broadcastPlayerStatus() {
        final Intent intent = new Intent(BROADCAST_PLAYER_STATUS_ACTION);
        intent.putExtra(PLAYER_STATUS_EXTRA_KEY, getStatus());
        intent.putExtra(SCENE_ID_KEY, getPlayingTrackID());
        sendBroadcast(intent);
    }

    // this is missing updates for the track title.
    private void updateNotification() {
        // TODO move this guff not notificationBuilder
        final NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationBuilder builder = new NotificationBuilder();

        mNotificationManager.notify(ONGOING_NOTIFICATION_ID,
                builder.buildNotification(
                        getStatus(), currentScene, mediaSession, this));
    }

    /**
     * Get the current status of media playback.
     *
     * @return the status.
     */
    private PlayerStatus getStatus() {
        if (mediaPlayer == null) {
            return PlayerStatus.STOPPED;
        } else if (!mediaPlayer.isPlaying()) {
            return PlayerStatus.PAUSED;
        } else {
            return PlayerStatus.PLAYING;
        }
    }

    /**
     * Gets the ID of the currently loaded media.
     *
     * @return the ID.
     */
    private long getPlayingTrackID() {
        return currentScene.getId();
    }

}
