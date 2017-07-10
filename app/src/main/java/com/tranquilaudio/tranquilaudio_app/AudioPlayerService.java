package com.tranquilaudio.tranquilaudio_app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.tranquilaudio.tranquilaudio_app.model.AudioScene;
import com.tranquilaudio.tranquilaudio_app.model.AudioSceneLoader;
import com.tranquilaudio.tranquilaudio_app.model.AudioSceneLoaderImpl;
import com.tranquilaudio.tranquilaudio_app.model.PlayerStatus;
import com.tranquilaudio.tranquilaudio_app.model.SystemWrapperForModelImpl;

import java.io.IOException;

/**
 * Wrapper class for the Android MediaPlayer.
 */
public final class AudioPlayerService extends Service {

    /**
     * The intent action for play (as in resume) intents.
     */
    public static final String PLAY_ACTION
            = "com.tranquilaudio.tranquilaudio_app.ACTION_PLAY";

    /**
     * The intent action for pausing.
     */
    public static final String PAUSE_ACTION
            = "com.tranquilaudio.tranquilaudio_app.ACTION_PAUSE";

    /**
     * The intent action for loading and playing a new track.
     */
    public static final String LOAD_NEW_TRACK_ACTION
            = "com.tranquilaudio.tranquilaudio_app.ACTION_LOAD_TRACK";

    /**
     * The intent extra key corresponding to the ID of the track to play.
     */
    public static final String SCENE_ID_KEY
            = "com.tranquilaudio.tranquilaudio_app.SCENE_ID_KEY";

    /**
     * Use the scene with this ID by default.
     */
    public static final long DEFAULT_SCENE = 1;

    // corresponding request codes for the above action strings
    private static final int REQUEST_PLAY = 11;
    private static final int REQUEST_PAUSE = 22;

    /**
     * The intent action key for pause / play status update notifications.
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

    private final IBinder mBinder = new MyBinder();

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        final String action = intent.getAction();
        switch (action) {
            case PLAY_ACTION:
                playMedia();
                break;
            case PAUSE_ACTION:
                pauseMedia();
                break;
            case LOAD_NEW_TRACK_ACTION:
                final Long audioTrackId
                        = intent.getLongExtra(SCENE_ID_KEY, DEFAULT_SCENE);
                loadMedia(audioTrackId);
                break;
            default:
                throw new RuntimeException("unrecognised action");
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        loader = new AudioSceneLoaderImpl(new SystemWrapperForModelImpl(this));
        currentScene = loader.getScene(DEFAULT_SCENE);
        final Uri audioTrack = currentScene.getAudioURI(this);
        this.mediaPlayer
                = MediaPlayer.create(getApplicationContext(), audioTrack);
        initMediaSession();
        startForeground(ONGOING_NOTIFICATION_ID,
                buildNotification(PlayerStatus.PAUSED));
        return mBinder;
    }

    private Notification buildNotification(final PlayerStatus playerStatus) {
        // the intent for when you click on the main body of the notification
        final Intent notificationIntent
                = new Intent(this, SceneListActivity.class);
        final PendingIntent pendingIntent
                = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // the intent for the pause / play button in the notification
        final PendingIntent mediaControlIntent;
        final NotificationCompat.Action action;
        if (playerStatus == PlayerStatus.PLAYING) {
            mediaControlIntent = playbackAction(REQUEST_PAUSE);
            action = new NotificationCompat.Action.Builder(
                    R.drawable.ic_pause_black_24dp,
                    "pause", mediaControlIntent)
                    .build();

        } else {
            mediaControlIntent = playbackAction(REQUEST_PLAY);
            action = new NotificationCompat.Action.Builder(
                    R.drawable.ic_play_arrow_black_24dp,
                    "play", mediaControlIntent)
                    .build();
        }

        final NotificationCompat.MediaStyle notificationStyle
                = new NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0);

        return new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Test content")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setTicker("Test ticker test")
                .setStyle(notificationStyle)
                .addAction(action)
                .build();
    }

    private PendingIntent playbackAction(final int actionNumber) {
        final Intent playbackAction
                = new Intent(this, AudioPlayerService.class);
        switch (actionNumber) {
            case REQUEST_PLAY:
                playbackAction.setAction(PLAY_ACTION);
                return PendingIntent.getService(
                        this, actionNumber, playbackAction, 0);
            case REQUEST_PAUSE:
                playbackAction.setAction(PAUSE_ACTION);
                return PendingIntent.getService(
                        this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
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
        publishResult(PlayerStatus.PAUSED);
    }

    // play or resume
    private void playMedia() {
        mediaPlayer.start();
        publishResult(PlayerStatus.PLAYING);
    }

    /**
     * Load and play the given Audio Scene.
     *
     * @param audioSceneId the ID of the Audio Scene to play.
     */
    private void loadMedia(final long audioSceneId) {
        currentScene = loader.getScene(audioSceneId);
        final Uri audioTrack = currentScene.getAudioURI(this);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), audioTrack);
            mediaPlayer.prepare();
            mediaPlayer.start();
            publishResult(PlayerStatus.PLAYING);
        } catch (final IOException e) {
            throw new RuntimeException("could not load media");
        }
    }

    /**
     * Broadcasts the new status of the media player.
     */
    private void publishResult(final PlayerStatus status) {
        broadcastPlayerStatus(status);
        updateNotification(status);
    }

    private void broadcastPlayerStatus(final PlayerStatus status) {
        final Intent intent = new Intent(BROADCAST_PLAYER_STATUS_ACTION);
        intent.putExtra(PLAYER_STATUS_EXTRA_KEY, status);
        sendBroadcast(intent);
    }

    private void updateNotification(final PlayerStatus status) {
        final NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID,
                buildNotification(status));
    }

    /**
     * Get the current status of media playback.
     *
     * @return the status.
     */
    public PlayerStatus getStatus() {
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
    public long getPlayingTrack() {
        return currentScene.getId();
    }

}
