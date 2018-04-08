package com.tranquilaudio.tranquilaudio_app.view

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.support.v7.app.AppCompatActivity


object ActivityUtils {

    /**
     * Do all the boilerplate stuff that must be done for every activity.
     */
    @JvmStatic
    fun initActivity(activity: AppCompatActivity) {
        activity.volumeControlStream = AudioManager.STREAM_MUSIC
        if (!isTablet(activity)) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun isTablet(activity: AppCompatActivity): Boolean {
        return activity.resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

}