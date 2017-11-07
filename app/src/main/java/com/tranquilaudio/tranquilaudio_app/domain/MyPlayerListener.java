package com.tranquilaudio.tranquilaudio_app.domain;

import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

/**
 * Listener for ExoPlayer events.
 */
final class MyPlayerListener implements Player.EventListener {

    private final AudioPlayerService audioService;

    MyPlayerListener(final AudioPlayerService audioPlayerService) {
        audioService = audioPlayerService;
    }

    @Override
    public void onTimelineChanged(
            final Timeline timeline, final Object manifest) { }

    @Override
    public void onTracksChanged(final TrackGroupArray trackGroups,
                                final TrackSelectionArray trackSelections) { }

    @Override
    public void onLoadingChanged(final boolean isLoading) { }

    @Override
    public void onPlayerStateChanged(final boolean playWhenReady,
                                     final int playbackState) {
        audioService.publishStatus();
    }

    @Override
    public void onRepeatModeChanged(final int repeatMode) { }

    @Override
    public void onPlayerError(final ExoPlaybackException error) {
        Log.e("MyPlayerListener", "Playback error", error);
    }

    @Override
    public void onPositionDiscontinuity() { }

    @Override
    public void onPlaybackParametersChanged(
            final PlaybackParameters playbackParameters) { }
}
