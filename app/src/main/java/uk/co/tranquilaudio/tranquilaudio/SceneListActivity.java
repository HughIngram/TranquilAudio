package uk.co.tranquilaudio.tranquilaudio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

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
    private ContentLoader contentLoader;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_list);
        contentLoader = new ContentLoader(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        final FloatingActionButton fab
                = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Intent intent = new Intent(
                        getApplicationContext(), AudioPlayerService.class);
                startService(intent);
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

        final Intent mediaPlayerIntent = new Intent(this, AudioPlayerService.class);
        mediaPlayerIntent.setAction("PLAY");
        bindService(mediaPlayerIntent, this, Context.BIND_AUTO_CREATE);
        // TODO make a notification and bring the service to the fg
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        recyclerView.setAdapter(
                new SceneRecyclerViewAdapter(this, contentLoader.getItems()));
    }

    // TODO implement a BroadcastReceiver

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder binder) {
        audioPlayerService = ((AudioPlayerService.MyBinder) binder).getService();
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
    }

    boolean isTwoPane() {
        return isTwoPane;
    }
}
