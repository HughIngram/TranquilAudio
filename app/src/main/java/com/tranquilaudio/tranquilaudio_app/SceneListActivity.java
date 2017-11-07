package com.tranquilaudio.tranquilaudio_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tranquilaudio.tranquilaudio_app.domain.AudioPlayerService;
import com.tranquilaudio.tranquilaudio_app.data.AudioScene;
import com.tranquilaudio.tranquilaudio_app.domain.AudioSceneLoader;
import com.tranquilaudio.tranquilaudio_app.domain.AudioSceneLoaderImpl;
import com.tranquilaudio.tranquilaudio_app.domain.PlayerStatus;
import com.tranquilaudio.tranquilaudio_app.domain.SystemWrapperForModel;
import com.tranquilaudio.tranquilaudio_app.domain.SystemWrapperForModelImpl;
import com.tranquilaudio.tranquilaudio_app.view.MediaControlBar;

import java.util.List;

/**
 * An activity representing a list of SceneItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SceneDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public final class SceneListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean isTwoPane;
    private MediaControlBar mediaControlBar;
    private AudioSceneLoader loader;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_list);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        loader = new AudioSceneLoaderImpl(new SystemWrapperForModelImpl(this));

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

        final View recyclerView = findViewById(R.id.scene_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.scene_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            isTwoPane = true;
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private TranquilAudioApplication getTranquilApp() {
        return (TranquilAudioApplication) getApplication();
    }

    private void registerBroadcastReceiver() {
        registerReceiver(playerStatusReceiver, new IntentFilter(
                AudioPlayerService.BROADCAST_PLAYER_STATUS_ACTION));
    }

    private BroadcastReceiver playerStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final PlayerStatus playerStatus = (PlayerStatus) intent
                    .getSerializableExtra(
                            AudioPlayerService.PLAYER_STATUS_EXTRA_KEY);
            final long trackId = intent.getLongExtra(AudioPlayerService
                    .SCENE_ID_KEY, 0);
            final AudioScene playingTrack = loader.getScene(trackId);
            mediaControlBar.updateView(playerStatus, playingTrack);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
        getTranquilApp().broadcastAudioPlayerServiceStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(playerStatusReceiver);
    }


    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        final SystemWrapperForModel sys = new SystemWrapperForModelImpl(this);
        final AudioSceneLoader audioSceneLoader = new AudioSceneLoaderImpl(sys);
        final List<AudioScene> scenes = audioSceneLoader.getSceneList();
        recyclerView.setAdapter(new SceneRecyclerViewAdapter(this, scenes));
    }

    boolean isTwoPane() {
        return isTwoPane;
    }

}
