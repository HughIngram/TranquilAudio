package com.tranquilaudio.tranquilaudio_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;

import com.tranquilaudio.tranquilaudio_app.domain.AudioPlayerService;
import com.tranquilaudio.tranquilaudio_app.data.AudioScene;
import com.tranquilaudio.tranquilaudio_app.domain.PlayerStatus;

/**
 * Class to build the ongoing Notification.
 */
public final class NotificationBuilder {

    // corresponding request codes for the above action strings
    private static final int REQUEST_PLAY = 11;
    private static final int REQUEST_PAUSE = 22;
    private static final int REQUEST_END = 33;

    /**
     * Get a notification.
     *
     * @param playerStatus the player status to show.
     * @param scene        the playing scene.
     * @param session      the MediaSession.
     * @param context      the application context.
     * @return the built notification.
     */
    public Notification buildNotification(final PlayerStatus playerStatus,
                                          final AudioScene scene,
                                          final MediaSessionCompat session,
                                          final Context context) {
        // the intent for when you click on the main body of the notification
        final Intent notificationIntent
                = new Intent(context, SceneListActivity.class);
        final PendingIntent pendingIntent
                = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        // the intent for the pause / resume button in the notification
        final PendingIntent mediaControlIntent;
        final NotificationCompat.Action pausePlayAction;
        if (playerStatus == PlayerStatus.PLAYING) {
            mediaControlIntent = playbackAction(REQUEST_PAUSE, context);
            pausePlayAction = new NotificationCompat.Action.Builder(
                    R.drawable.ic_pause_black_24dp,
                    "pause", mediaControlIntent)
                    .build();

        } else {
            mediaControlIntent = playbackAction(REQUEST_PLAY, context);
            pausePlayAction = new NotificationCompat.Action.Builder(
                    R.drawable.ic_play_arrow_black_24dp,
                    "resume", mediaControlIntent)
                    .build();
        }
        final PendingIntent closeIntent = playbackAction(REQUEST_END, context);
        final NotificationCompat.Action closeAction
                = new NotificationCompat.Action.Builder(
                R.drawable.ic_close_black_24dp,
                "close", closeIntent).build();

        MediaStyle mediaStyle = new MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0, 1);
        initChannels(context);
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(scene.getTitle())
                .setContentText("Tap to open the app.")
                .setSmallIcon(R.drawable.play_circle_white_24dp)
                .setContentIntent(pendingIntent)
                .setTicker("Test ticker test")
                .setStyle(mediaStyle)
                .addAction(pausePlayAction)
                .addAction(closeAction)
                .build();
    }

    private PendingIntent playbackAction(final int actionNumber,
                                         final Context context) {
        final Intent playbackIntent
                = new Intent(context, AudioPlayerService.class);
        switch (actionNumber) {
            case REQUEST_PLAY:
                playbackIntent.setAction(AudioPlayerService.RESUME_ACTION);
                break;
            case REQUEST_PAUSE:
                playbackIntent.setAction(AudioPlayerService.PAUSE_ACTION);
                break;
            case REQUEST_END:
                playbackIntent.setAction(AudioPlayerService.CLOSE_ACTION);
                return PendingIntent.getService(
                        context, actionNumber, playbackIntent, PendingIntent
                                .FLAG_CANCEL_CURRENT);
            default:
                return null;
        }
        return PendingIntent.getService(
                context, actionNumber, playbackIntent, PendingIntent
                        .FLAG_UPDATE_CURRENT);
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    private static String CHANNEL_ID = "this is a channel id";
    private static String CHANNEL_NAME = "Tranquil Audio";

}
