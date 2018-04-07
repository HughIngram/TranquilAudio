package com.tranquilaudio.tranquilaudio_app.domain;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.tranquilaudio.tranquilaudio_app.NotificationBuilder;
import com.tranquilaudio.tranquilaudio_app.TranquilAudioApplication;
import com.tranquilaudio.tranquilaudio_app.data.AudioScene;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

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

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private AudioScene currentScene;
    private AudioSceneLoader loader;
    private AudioManager am;
    private SimpleExoPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        loader = new AudioSceneLoaderImpl(new SystemWrapperForModelImpl(this));
        currentScene = ((TranquilAudioApplication) getApplication())
                .getLastPlayed();
        initAudioManager();
    }

    private final AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(final int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            // Permanent loss of audio focus
                            mediaSession.getController()
                                    .getTransportControls().pause();
                            break;
                        case AUDIOFOCUS_LOSS_TRANSIENT:
                            mediaSession.getController().getTransportControls()
                                    .pause();
                            break;
                        case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            mediaSession.getController().adjustVolume(
                                    AudioManager.ADJUST_LOWER, 0);
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            mediaSession.getController().adjustVolume(
                                    AudioManager.ADJUST_RAISE, 0);
                            break;
                        default:
                            Log.e(TAG, "Unknown focus change received");
                            break;
                    }
                }
            };

    private void initAudioManager() {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    // This gets called BEFORE the service has been bound.
    @Override
    public IBinder onBind(final Intent intent) {
        initMediaSession();
        final NotificationBuilder builder = new NotificationBuilder();
        startForeground(ONGOING_NOTIFICATION_ID, builder
                .buildNotification(PlayerStatus.PAUSED, currentScene,
                        mediaSession, this));
        return new Binder();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        final String action = intent.getAction();
        switch (action) {
            case RESUME_ACTION:
                mediaSession.getController().getTransportControls().play();
                break;
            case PAUSE_ACTION:
                mediaSession.getController().getTransportControls().pause();
                break;
            case LOAD_NEW_TRACK_ACTION:
                final Long audioTrackId = intent.getLongExtra(
                        SCENE_ID_KEY, MediaControlClient.DEFAULT_SCENE);
                final String id = Long.toString(audioTrackId);
                mediaSession.getController().getTransportControls()
                        .playFromMediaId(id, null);
                break;
            case CLOSE_ACTION:
                mediaSession.getController().getTransportControls().pause();
                requestClose();
                break;
            case REQUEST_STATUS_ACTION:
                broadcastPlayerStatus();
                break;
            default:
                throw new RuntimeException("unrecognised action");
        }
        return START_REDELIVER_INTENT;
    }

    // this is necessary since a service can not unbind itself.
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
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);

        final TrackSelector trackSelector = new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(
                getApplicationContext(), trackSelector);
        player.addListener(new MyPlayerListener(this));
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        final Uri audioTrack = currentScene.getAudioURI(this);
        final MediaSource source = genMediaSource(audioTrack);
        player.prepare(source);

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
            public void onPlayFromMediaId(
                    final String mediaId, final Bundle extras) {
                loadMedia(Long.parseLong(mediaId));
                playMedia();
                super.onPlayFromMediaId(mediaId, extras);
            }

        });
    }

    private MediaSource genMediaSource(final Uri uri) {
        final DataSpec dataSpec = new DataSpec(uri);
        final RawResourceDataSource rawResourceDataSource
                = new RawResourceDataSource(getApplicationContext());
        try {
            rawResourceDataSource.open(dataSpec);
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            Log.e(TAG, "failed to open raw data src");
        }
        final DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return rawResourceDataSource;
            }
        };

        return new ExtractorMediaSource(rawResourceDataSource.getUri(),
                factory, Mp3Extractor.FACTORY, null, null);
    }

    private void pauseMedia() {
        player.setPlayWhenReady(false);
        am.abandonAudioFocus(afChangeListener);
    }

    // i.e. resume
    private void playMedia() {
        final int result = am.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            player.setPlayWhenReady(true);
        } else {
            Log.e(TAG, "Audio focus not granted");
        }
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        stopForeground(true);
        mediaSession.setActive(false);
        mediaSession.release();
        player.release();
        player = null;
        mediaSessionManager = null;
        broadcastPlayerStatus();
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
        final MediaSource source = genMediaSource(audioTrack);
        player.stop();
        player.prepare(source);
    }

    /**
     * Broadcasts the new status of the media player.
     */
    void publishStatus() {
        broadcastPlayerStatus();
        updateNotification();
    }

    private void broadcastPlayerStatus() {
        final Intent intent = new Intent(BROADCAST_PLAYER_STATUS_ACTION);
        intent.putExtra(PLAYER_STATUS_EXTRA_KEY, getPlayerStatus());
        intent.putExtra(SCENE_ID_KEY, getPlayingTrackID());
        sendBroadcast(intent);
    }

    private void updateNotification() {
        final NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationBuilder builder = new NotificationBuilder();
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID,
                builder.buildNotification(
                        getPlayerStatus(), currentScene, mediaSession, this));
    }

    private PlayerStatus getPlayerStatus() {
        if (player == null) {
            return PlayerStatus.STOPPED;
        }
        final int playbackState = player.getPlaybackState();
        if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING) {
            if (player.getPlayWhenReady()) {
                return PlayerStatus.PLAYING;
            } else {
                return PlayerStatus.PAUSED;
            }
        } else if (playbackState == Player.STATE_IDLE) {
            return PlayerStatus.PAUSED;
        } else {
            return PlayerStatus.STOPPED;
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
