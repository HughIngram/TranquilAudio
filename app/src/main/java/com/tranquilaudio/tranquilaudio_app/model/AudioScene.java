package com.tranquilaudio.tranquilaudio_app.model;


/**
 * A dummy item representing a piece of title.
 */
public final class AudioScene {

    private final long id;
    private final String title;
    private final String details;
    private String audioResource;

    // TODO:
    // image file path

    public AudioScene(final long id, final String title, final String details,
                      final String audioResource) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.setAudioResource(audioResource);
    }

    public long getId() {
        return id;
    }

    // TODO eliminate this - use getLong and putLong() for SceneDetailFragment
    /**
     * Get the ID as a String.
     * @return the ID.
     */
    public String getIdString() {
        return Long.toString(id);
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    /**
     * Get the file name of the audio resource (in res/raw)
     * @return the audio resource file name.
     */
    public String getAudioResource() {
        return audioResource;
    }

    /**
     * Set the file neame of the audio resource (in res/raw)
     * @param audioResource the audio resource file name.
     */
    public void setAudioResource(final String audioResource) {
        this.audioResource = audioResource;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
