package uk.co.tranquilaudio.tranquilaudio.content;

/**
 * A dummy item representing a piece of content.
 */
public class Scene {

    public final String id;
    public final String content;
    public final String details;

    // TODO:
    // audio file path? should go in /res
    // image file path

    public Scene(String id, String content, String details) {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    @Override
    public String toString() {
        return content;
    }
}
