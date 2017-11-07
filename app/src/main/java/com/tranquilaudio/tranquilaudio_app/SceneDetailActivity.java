package com.tranquilaudio.tranquilaudio_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.tranquilaudio.tranquilaudio_app.domain.AudioPlayerService;
import com.tranquilaudio.tranquilaudio_app.data.AudioScene;
import com.tranquilaudio.tranquilaudio_app.domain.AudioSceneLoader;
import com.tranquilaudio.tranquilaudio_app.domain.AudioSceneLoaderImpl;
import com.tranquilaudio.tranquilaudio_app.domain.MediaControlClient;
import com.tranquilaudio.tranquilaudio_app.domain.PlayerStatus;
import com.tranquilaudio.tranquilaudio_app.domain.SystemWrapperForModelImpl;
import com.tranquilaudio.tranquilaudio_app.view.MediaControlBar;

/**
 * An activity representing a single AudioScene detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link SceneListActivity}.
 */
public final class SceneDetailActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private MediaControlBar mediaControlBar;
    private AudioScene visibleScene;    // the scene shown in this activity
    private AudioSceneLoader loader;

    private PlayerStatus status;    // update these fields onReceive
    private AudioScene playingScene;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_detail);
        final Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                fabClick();
            }
        });

        loader = new AudioSceneLoaderImpl(new SystemWrapperForModelImpl(this));
        final long sceneId = getIntent()
                .getLongExtra(SceneDetailFragment.ARG_ITEM_ID,
                        MediaControlClient.DEFAULT_SCENE);
        visibleScene = loader.getScene(sceneId);
        final View mediaControlLayout = findViewById(R.id.media_controller);
        final MediaControlBar.Callbacks cb = new MediaControlBar.Callbacks() {

            @Override
            public void pause() {
                getTranquilApp().getMediaControlClient().pause();
            }

            @Override
            public void resume() {
                getTranquilApp().getMediaControlClient().resume();
            }

        };
        mediaControlBar = new MediaControlBar(mediaControlLayout, this, cb);

        // Show the Up button in the action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            final Bundle arguments = new Bundle();
            arguments.putLong(SceneDetailFragment.ARG_ITEM_ID, sceneId);
            final SceneDetailFragment fragment = new SceneDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.scene_detail_container, fragment)
                    .commit();
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private TranquilAudioApplication getTranquilApp() {
        return (TranquilAudioApplication) getApplication();
    }

    private void fabClick() {
        if (playingScene.getId() == visibleScene.getId()) {
            // user is looking at the playing track - let them pause / resume it
            if (status == PlayerStatus.PLAYING) {
                getTranquilApp().getMediaControlClient().pause();
            } else {
                getTranquilApp().getMediaControlClient().resume();
            }
        } else {
            // user is looking at a different track. Play it.
            getTranquilApp().getMediaControlClient().loadScene(visibleScene
                    .getId());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
        getTranquilApp().broadcastAudioPlayerServiceStatus();
    }

    private void registerBroadcastReceiver() {
        registerReceiver(playerStatusReceiver, new IntentFilter(
                AudioPlayerService.BROADCAST_PLAYER_STATUS_ACTION));
    }

    private BroadcastReceiver playerStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final long trackId = intent.getLongExtra(AudioPlayerService
                    .SCENE_ID_KEY, 0);
            playingScene = loader.getScene(trackId);
            status = (PlayerStatus) intent.getSerializableExtra(
                    AudioPlayerService.PLAYER_STATUS_EXTRA_KEY);
            mediaControlBar.updateView(status, playingScene);
            updateFloatingActionButton();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(playerStatusReceiver);
    }

    private void updateFloatingActionButton() {
        if (visibleScene.getId() == playingScene.getId()) {
            // user is looking at the playing track - let them pause / resume it
            if (status == PlayerStatus.PLAYING) {
                fab.setImageDrawable(getDrawable(
                        android.R.drawable.ic_media_pause));
            } else {
                fab.setImageDrawable(getDrawable(
                        android.R.drawable.ic_media_play));
            }
        } else {
            // user is looking at a different track. Play it.
            fab.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation
            // .html#up-vs-back
            //
            navigateUpTo(new Intent(this, SceneListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
