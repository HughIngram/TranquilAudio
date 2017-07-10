package com.tranquilaudio.tranquilaudio_app.model;

import java.util.List;

/**
 * Interface to help reading from the file system.
 */
public interface AudioSceneLoader {

    /**
     * Get the List of available patterns.
     * @return the list of patterns.
     */
    List<AudioScene> getSceneList();

    /**
     * Gets the given Audio Scene.
     * @param sceneId the scene ID.
     */
    AudioScene getScene(long sceneId);

}
