package uk.co.tranquilaudio.tranquilaudio;

import android.support.annotation.IdRes;
import android.support.annotation.RawRes;

/**
 * A dummy item representing a piece of content.
 */
public final class Scene {

    public final String id;
    public final String content;
    public final String details;

    @RawRes int audioResource;

    // TODO:
    // image file path

    public Scene(String id, String content, String details, @RawRes int audioResource) {
        this.id = id;
        this.content = content;
        this.details = details;
        this.audioResource = audioResource;
    }

    @Override
    public String toString() {
        return content;
    }
}
