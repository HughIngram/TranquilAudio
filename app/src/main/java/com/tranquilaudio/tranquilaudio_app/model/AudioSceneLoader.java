package com.tranquilaudio.tranquilaudio_app.model;

import java.util.List;

/**
 * Interface to help reading from the file system.
 */
public interface AudioSceneLoader {

    /**
     * The default scene for when you hit play with nothing selected.
     *
     */
    long DEFAULT_SCENE_ID = 1;

    /**
     * Get the List of available patterns.
     * @return the list of patterns.
     */
    List<AudioScene> getSceneList();

    /**
     * Gets the given Audio Scene.
     * @param sceneId the scene ID.
     */
    AudioScene loadScene(long sceneId);

    // TODO open audio files

    // TODO open image files

}
