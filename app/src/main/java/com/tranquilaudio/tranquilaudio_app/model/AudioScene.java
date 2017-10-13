package com.tranquilaudio.tranquilaudio_app.model;


import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.upstream.RawResourceDataSource;

/**
 * A dummy item representing a piece of title.
 *
 * Do not allow this class to expose where audio / image files are coming from.
 */
public final class AudioScene {

    private final long id;
    private final String title;
    private final String details;
    private String audioResource;

    AudioScene(final long id, final String title, final String details,
                      final String audioResource) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.audioResource = audioResource;
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return
     */
    public String getDetails() {
        return details;
    }

    /**
     * Get the URI of the audio file associated with this scene.
     *
     * @param context the context.
     * @return the URI of the audio file.
     */
    Uri getAudioURI(final Context context) {
        final int id = context.getResources().getIdentifier(
                audioResource, "raw", context.getPackageName());
        return RawResourceDataSource.buildRawResourceUri(id);
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
