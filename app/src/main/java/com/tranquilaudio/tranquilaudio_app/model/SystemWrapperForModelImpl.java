package com.tranquilaudio.tranquilaudio_app.model;

import android.content.Context;
import android.support.annotation.RawRes;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of the System wrapper for the Model.
 */
public final class SystemWrapperForModelImpl implements SystemWrapperForModel {

    private final Context activity;

    public SystemWrapperForModelImpl(final Context activity) {
        this.activity = activity;
    }

    @Override
    public String getStringFromRawResource(final @RawRes int resId) {
        try {
            final InputStream is = activity.getResources().openRawResource(resId);
            final String statesText = convertStreamToString(is);
            is.close();
            return statesText;
        } catch (final IOException e) {
            Log.e("", "ugh");
            return null;
        }
    }

    private String convertStreamToString(final InputStream is) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = is.read();
        while (i != -1) {
            baos.write(i);
            i = is.read();
        }
        return baos.toString();
    }
}
