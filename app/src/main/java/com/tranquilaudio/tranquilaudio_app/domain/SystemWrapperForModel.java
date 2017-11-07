package com.tranquilaudio.tranquilaudio_app.domain;


import android.support.annotation.RawRes;

/**
 * Interface to isolate the Model from Android APIs.
 */
public interface SystemWrapperForModel {


    /**
     * Gets the given raw resource in String format.
     * @param resId the ID of the raw resource.
     * @return the res as a String.
     */
    String getStringFromRawResource(@RawRes int resId);

}
