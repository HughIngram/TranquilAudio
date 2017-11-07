package com.tranquilaudio.tranquilaudio_app.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tranquilaudio.tranquilaudio_app.R;
import com.tranquilaudio.tranquilaudio_app.data.AudioScene;
import com.tranquilaudio.tranquilaudio_app.domain.PlayerStatus;

/**
 * The bottom bar which controls Media playback.
 */
public final class MediaControlBar {

    private final ImageButton pausePlayButton;
    private final TextView mediaTitle;
    private final Context context;
    private final Callbacks callbacks;

    private PlayerStatus playerStatus;

    @IdRes
    private static final int MEDIA_BUTTON_ID = R.id.media_control_button;

    @IdRes
    private static final int MEDIA_TITLE_ID = R.id.media_title;

    /**
     * Default constructor.
     * @param barView the view associated with this bottom bar.
     * @param context the app context.
     * @param callbacks the button call back actions.
     */
    public MediaControlBar(final View barView, final Context context,
                           final Callbacks callbacks) {
        this.pausePlayButton
                = (ImageButton) barView.findViewById(MEDIA_BUTTON_ID);
        this.mediaTitle = (TextView) barView.findViewById(MEDIA_TITLE_ID);
        this.context = context;
        this.callbacks = callbacks;
        setUpOnClick();
    }

    /**
     * Update the state of the media control bar.
     * @param status yayay
     * @param scene ayaya
     */
    public void updateView(
            final PlayerStatus status, final AudioScene scene) {
        setStatus(status);
        setAudioScene(scene);
        playerStatus = status;
    }

    /**
     * Display the given audio playback status.
     *
     * @param status the status of audio playback to display.
     */
    private void setStatus(final PlayerStatus status) {
        if (status == PlayerStatus.PLAYING) {
            pausePlayButton.setBackground(
                    context.getDrawable(R.drawable.ic_pause_black_24dp));
        } else {
            pausePlayButton.setBackground(
                    context.getDrawable(R.drawable.ic_play_arrow_black_24dp));
        }
    }

    private void setAudioScene(final AudioScene scene) {
        mediaTitle.setText(scene.getTitle());
    }

    private void setUpOnClick() {
        pausePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (playerStatus == PlayerStatus.PLAYING) {
                    callbacks.pause();
                    setStatus(PlayerStatus.PAUSED);
                } else {
                    callbacks.resume();
                    setStatus(PlayerStatus.PLAYING);
                }
            }
        });
    }

    /**
     * Callback methods.
     */
    public interface Callbacks {
        /**
         * Called when the Pause button is pressed.
         */
        void pause();

        /**
         * Called when the Play button is pressed.
         */
        void resume();

    }

}
