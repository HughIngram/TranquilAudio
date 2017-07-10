package com.tranquilaudio.tranquilaudio_app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tranquilaudio.tranquilaudio_app.model.AudioScene;
import com.tranquilaudio.tranquilaudio_app.model.AudioSceneLoader;
import com.tranquilaudio.tranquilaudio_app.model.AudioSceneLoaderImpl;
import com.tranquilaudio.tranquilaudio_app.model.SystemWrapperForModel;
import com.tranquilaudio.tranquilaudio_app.model.SystemWrapperForModelImpl;

import java.util.List;

/**
 * An activity representing a list of SceneItems. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SceneDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public final class SceneListActivity
        extends AppCompatActivity implements ServiceConnection {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean isTwoPane;
    private AudioPlayerService audioPlayerService;
    private FloatingActionButton fab;

    private BroadcastReceiver playerStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final AudioPlayerService.PlayerStatus playerStatus
                    = (AudioPlayerService.PlayerStatus) intent.getExtras()
                    .get(AudioPlayerService.PLAYER_STATUS_EXTRA_KEY);
            updatePausePlayButton(playerStatus);
        }
    };

    // TODO eliminate the playerStatus field?  get playerstatus directly instead
    private void updatePausePlayButton(
            final AudioPlayerService.PlayerStatus playerStatus) {
        if (playerStatus == AudioPlayerService.PlayerStatus.PLAYING) {
            fab.setImageDrawable(getResources()
                    .getDrawable(android.R.drawable.ic_media_pause));
        } else {
            fab.setImageDrawable(getResources()
                    .getDrawable(android.R.drawable.ic_media_play));
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_list);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                fabClick();
            }
        });

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

        final Intent mediaPlayerIntent
                = new Intent(this, AudioPlayerService.class);
        mediaPlayerIntent.setAction(AudioPlayerService.PAUSE_ACTION);
        bindService(mediaPlayerIntent, this, Context.BIND_AUTO_CREATE);

        registerReceiver(playerStatusReceiver, new IntentFilter(
                AudioPlayerService.BROADCAST_PLAYER_STATUS_ACTION));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audioPlayerService != null) {
            updatePausePlayButton(audioPlayerService.getStatus());
        }
    }

    private void fabClick() {
        final Intent intent = new Intent(
                getApplicationContext(), AudioPlayerService.class);
        if (audioPlayerService.getStatus()
                == AudioPlayerService.PlayerStatus.PLAYING) {
            intent.setAction(AudioPlayerService.PAUSE_ACTION);
        } else {
            intent.setAction(AudioPlayerService.PLAY_ACTION);
        }
        startService(intent);
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        final SystemWrapperForModel sys = new SystemWrapperForModelImpl(this);
        final AudioSceneLoader audioSceneLoader = new AudioSceneLoaderImpl(sys);
        final List<AudioScene> scenes = audioSceneLoader.getSceneList();
        recyclerView.setAdapter(new SceneRecyclerViewAdapter(this, scenes));
    }

    @Override
    public void onServiceConnected(final ComponentName name,
                                   final IBinder binder) {
        audioPlayerService
                = ((AudioPlayerService.MyBinder) binder).getService();
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
    }

    boolean isTwoPane() {
        return isTwoPane;
    }
}
