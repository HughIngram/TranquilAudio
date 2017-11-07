package com.tranquilaudio.tranquilaudio_app.data;


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
    private String location;

    /**
     * Constructor for AudioScene.
     * @param id .
     * @param title .
     * @param details .
     * @param audioResource .
     */
    public AudioScene(final long id, final String title, final String details,
                      final String audioResource, final String location) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.audioResource = audioResource;
        this.location = location;
    }

    /**
     *
     * @return .
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return .
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return .
     */
    public String getDetails() {
        return details;
    }

    /**
     *
     * @return .
     */
    public String getLocation() {
        return location;
    }

    /**
     * Get the URI of the audio file associated with this scene.
     *
     * @param context the context.
     * @return the URI of the audio file.
     */
    public Uri getAudioURI(final Context context) {
        final int id = context.getResources().getIdentifier(
                audioResource, "raw", context.getPackageName());
        return RawResourceDataSource.buildRawResourceUri(id);
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
