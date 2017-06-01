package uk.co.tranquilaudio.tranquilaudio;

import android.support.annotation.RawRes;

/**
 * A dummy item representing a piece of content.
 */
final class Scene {

    private final long id;
    public final String content;
    public final String details;

    @RawRes int audioResource;

    // TODO:
    // image file path

    Scene(final long id, final String content, final String details,
                 @RawRes final int audioResource) {
        this.id = id;
        this.content = content;
        this.details = details;
        this.audioResource = audioResource;
    }

    long getId() {
        return id;
    }

    String getIdString() {
        return Long.toString(id);
    }

    @Override
    public String toString() {
        return content;
    }
}
