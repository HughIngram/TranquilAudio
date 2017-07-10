package com.tranquilaudio.tranquilaudio_app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.tranquilaudio.tranquilaudio_app.model.PlayerStatus;

/**
 * An activity representing a single AudioScene detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link SceneListActivity}.
 */
public final class SceneDetailActivity extends AppCompatActivity
        implements ServiceConnection {

    private AudioPlayerService audioPlayerService;
    private FloatingActionButton fab;
    private long sceneId; // the ID of the scene associated with this activity

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        sceneId = getIntent().getLongExtra(SceneDetailFragment.ARG_ITEM_ID,
                AudioPlayerService.DEFAULT_SCENE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                fabClick();
            }
        });

        // Show the Up button in the action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
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
            // TODO why does rotation cause the title to change?
        }

        final Intent mediaPlayerIntent
                = new Intent(this, AudioPlayerService.class);
        bindService(mediaPlayerIntent, this, Context.BIND_AUTO_CREATE);

        registerReceiver(playerStatusReceiver, new IntentFilter(
                AudioPlayerService.BROADCAST_PLAYER_STATUS_ACTION));
    }

    @Override
    public void onServiceConnected(final ComponentName name,
                                   final IBinder binder) {
        audioPlayerService
                = ((AudioPlayerService.MyBinder) binder).getService();
        updatePausePlayButton();
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
    }

    private void fabClick() {
        final PlayerStatus status
                = audioPlayerService.getStatus();
        final long currentlyPlayingTrack = audioPlayerService.getPlayingTrack();
        final Intent intent = new Intent(
                getApplicationContext(), AudioPlayerService.class);

        if (currentlyPlayingTrack == sceneId) {
            // user is looking at the playing track - let them pause / resume it
            if (status == PlayerStatus.PLAYING) {
                intent.setAction(AudioPlayerService.PAUSE_ACTION);
            } else {
                intent.setAction(AudioPlayerService.PLAY_ACTION);
            }
        } else {
            // user is looking at a different track. Play it.
            intent.setAction(AudioPlayerService.LOAD_NEW_TRACK_ACTION);
            intent.putExtra(AudioPlayerService.SCENE_ID_KEY, sceneId);
        }
        startService(intent);
        // TODO there's some shared logic between this class and the
        // ListActivity perhaps they should share a presenter??
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (audioPlayerService != null) {
            updatePausePlayButton();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    // when rotating, this gets called before onResume()
    private BroadcastReceiver playerStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            updatePausePlayButton();
        }
    };

    private void updatePausePlayButton() {
        final PlayerStatus status
                = audioPlayerService.getStatus();
        final long currentlyPlayingTrack = audioPlayerService.getPlayingTrack();

        if (currentlyPlayingTrack == sceneId) {
            // user is looking at the playing track - let them pause / resume it
            if (status == PlayerStatus.PLAYING) {
                fab.setImageDrawable(getResources()
                        .getDrawable(android.R.drawable.ic_media_pause));
            } else {
                fab.setImageDrawable(getResources()
                        .getDrawable(android.R.drawable.ic_media_play));
            }
        } else {
            // user is looking at a different track. Play it.
            fab.setImageDrawable(getResources()
                    .getDrawable(android.R.drawable.ic_media_play));
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
