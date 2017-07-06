package com.tranquilaudio.tranquilaudio_app.model;

import android.support.annotation.RawRes;

import com.google.gson.Gson;
import com.tranquilaudio.tranquilaudio_app.R;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class to read from the Raw resources.
 */
public final class AudioSceneLoaderImpl implements AudioSceneLoader {

//    private static final String SCENES_DIR = "/AudioScenes/";
    @RawRes private static final int SCENES_INFO = R.raw.audio_scenes;
    private static final String TAG = "AudioSceneLoaderImpl";

    private final SystemWrapperForModel system;
    private Gson gson;

    /**
     * Default constructor.
     * @param system a SystemWrapperForModel.
     */
    public AudioSceneLoaderImpl(final SystemWrapperForModel system) {
        this.system = system;
        gson = new Gson();
    }

    @Override
    public ArrayList<AudioScene> getSceneList() {
        final String scenesJson = system.getStringFromRawResource(SCENES_INFO);
        final AudioScene[] scenesArray
                = gson.fromJson(scenesJson, AudioScene[].class);
        return new ArrayList<>(Arrays.asList(scenesArray));
    }

    /* TODO this will be useful when the files need to be cached from REST
    @Override
    public String[] getSceneList() throws FileNotFoundException {
        final File directory = new File(
                Environment.getExternalStorageDirectory(), SCENES_DIR);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "failed to make patterns dir");
            }
        } else {
            Log.d(TAG, "Directory already exists");
        }
        if (directory.listFiles() != null) {
            final File[] files = directory.listFiles();
            final String[] sceneTitles = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                final String filename = files[i].getName();
                final File sceneFile = new File(directory, filename);
                final BufferedReader br
                        = new BufferedReader(new FileReader(sceneFile));
                final AudioScene scene = gson.fromJson(br, AudioScene.class);
                sceneTitles[i] = scene.getTitle();
            }
            return sceneTitles;
        } else {
            throw new FileNotFoundException("Can't list files");
        }
    }
    */

    @Override
    public AudioScene loadScene(final long patternId) {
        final ArrayList<AudioScene> scenes = getSceneList();
        for (AudioScene as : scenes) {
            if (as.getId() == patternId) {
                return as;
            }
        }
        return null;
    }

    AudioScene getDefaultScene() {
        return loadScene(DEFAULT_SCENE_ID);
    }

}
