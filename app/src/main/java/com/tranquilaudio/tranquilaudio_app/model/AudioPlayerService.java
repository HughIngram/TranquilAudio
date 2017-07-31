package com.tranquilaudio.tranquilaudio_app.model;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.tranquilaudio.tranquilaudio_app.NotificationBuilder;

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
        final Uri audioTrack = currentScene.getAudioURI(this);
        this.mediaPlayer
                = MediaPlayer.create(getApplicationContext(), audioTrack);
        initMediaSession();
        loadMedia(MediaControlClient.DEFAULT_SCENE);
        final NotificationBuilder builder = new NotificationBuilder();
        // TODO wait until playback starts before displaying the notification.
        startForeground(ONGOING_NOTIFICATION_ID, builder
                .buildNotification(PlayerStatus.PAUSED, currentScene, mediaSession, this));
        registerBroadcastListener();
    }

    // receives requests for the current state of the service to be broadcast.
    private BroadcastReceiver statusRequestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            broadcastCurrentStatus();
        }
    };

    private void registerBroadcastListener() {
        registerReceiver(statusRequestReceiver, new IntentFilter("TEST"));  // TODO
    }


    // This gets called BEFORE the service has been bound.
    @Override
    public IBinder onBind(final Intent intent) {
        return new MyBinder();
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
            default:
                throw new RuntimeException("unrecognised action");
        }
        return START_REDELIVER_INTENT;
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
        if (mediaPlayer.isPlaying()) {
            return PlayerStatus.PLAYING;
        } else {
            return PlayerStatus.PAUSED;
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

    /**
     * Sends a broadcast describing the current state of the service.
     */
    public void broadcastCurrentStatus() {
        broadcastPlayerStatus();
    }

}
