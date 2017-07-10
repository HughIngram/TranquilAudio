package com.tranquilaudio.tranquilaudio_app.model;

import android.content.Context;
import android.content.Intent;

import com.tranquilaudio.tranquilaudio_app.AudioPlayerService;

/**
 * Handles client-side interaction with AudioPlayerService.
 */
public final class MediaControlClient {

    private final Context context;

    /**
     * Default constructor.
     * @param context the context.
     */
    public MediaControlClient(final Context context) {
        this.context = context;
    }

    // should this be exposed?
    // should this class instead expose actions like pause() and play() ??
    /**
     * Pause or resume playback.
     *
     * @param goalAction the goal action.
     */
    public void publishMediaControlIntent(final String goalAction) {
        final Intent intent = new Intent(context, AudioPlayerService.class);
        intent.setAction(goalAction);
        context.startService(intent);
    }

    // should I remove all references to the AudioPlayerService elsewhere?

    // getPlayerStatus()
}
