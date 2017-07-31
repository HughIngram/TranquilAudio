package com.tranquilaudio.tranquilaudio_app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;

import com.tranquilaudio.tranquilaudio_app.model.AudioPlayerService;
import com.tranquilaudio.tranquilaudio_app.model.AudioScene;
import com.tranquilaudio.tranquilaudio_app.model.PlayerStatus;

/**
 * Class to build the ongoing Notification.
 */
public final class NotificationBuilder {


    // corresponding request codes for the above action strings
    private static final int REQUEST_PLAY = 11;
    private static final int REQUEST_PAUSE = 22;

    /**
     * Get a notification.
     *
     * @param playerStatus the player status to show.
     * @param scene the playing scene.
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
        final NotificationCompat.Action action;
        if (playerStatus == PlayerStatus.PLAYING) {
            mediaControlIntent = playbackAction(REQUEST_PAUSE, context);
            action = new NotificationCompat.Action.Builder(
                    R.drawable.ic_pause_black_24dp,
                    "pause", mediaControlIntent)
                    .build();

        } else {
            mediaControlIntent = playbackAction(REQUEST_PLAY, context);
            action = new NotificationCompat.Action.Builder(
                    R.drawable.ic_play_arrow_black_24dp,
                    "resume", mediaControlIntent)
                    .build();
        }

        final NotificationCompat.MediaStyle notificationStyle
                = new NotificationCompat.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0);

        return new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(scene.getTitle())
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setTicker("Test ticker test")
                .setStyle(notificationStyle)
                .addAction(action)
                .build();
    }

    private PendingIntent playbackAction(final int actionNumber,
                                         final Context context) {
        final Intent playbackAction
                = new Intent(context, AudioPlayerService.class);
        switch (actionNumber) {
            case REQUEST_PLAY:
                playbackAction.setAction(AudioPlayerService.RESUME_ACTION);
                return PendingIntent.getService(
                        context, actionNumber, playbackAction, 0);
            case REQUEST_PAUSE:
                playbackAction.setAction(AudioPlayerService.PAUSE_ACTION);
                return PendingIntent.getService(
                        context, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }
}
