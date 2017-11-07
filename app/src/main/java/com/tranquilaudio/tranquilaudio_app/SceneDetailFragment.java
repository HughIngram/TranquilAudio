package com.tranquilaudio.tranquilaudio_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tranquilaudio.tranquilaudio_app.domain.AudioPlayerService;
import com.tranquilaudio.tranquilaudio_app.data.AudioScene;
import com.tranquilaudio.tranquilaudio_app.domain.AudioSceneLoader;
import com.tranquilaudio.tranquilaudio_app.domain.AudioSceneLoaderImpl;
import com.tranquilaudio.tranquilaudio_app.domain.PlayerStatus;
import com.tranquilaudio.tranquilaudio_app.domain.SystemWrapperForModelImpl;


/**
 * A fragment representing a single AudioScene detail screen.
 * This fragment is either contained in a {@link SceneListActivity}
 * in two-pane mode (on tablets) or a {@link SceneDetailActivity}
 * on handsets.
 */
public final class SceneDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The scene this fragment is presenting.
     */
    private AudioScene visibleScene;

    private AudioScene playingScene;

    private AudioSceneLoader loader;

    private PlayerStatus status;

    private ImageButton button;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SceneDetailFragment() {
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            loader = new AudioSceneLoaderImpl(
                    new SystemWrapperForModelImpl(this.getContext()));
            visibleScene = loader.getScene(getArguments().getLong(ARG_ITEM_ID));
        }
        getTranquilApp().broadcastAudioPlayerServiceStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(playerStatusReceiver);
    }

    private void registerBroadcastReceiver() {
        getContext().registerReceiver(playerStatusReceiver, new IntentFilter(
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
            updatePausePlayButton();
        }
    };

    private void updatePausePlayButton() {
        if (visibleScene.getId() == playingScene.getId()) {
            // user is looking at the playing track - let them pause / resume it
            if (status == PlayerStatus.PLAYING) {
                button.setBackground(getContext().getDrawable(
                        android.R.drawable.ic_media_pause));
            } else {
                button.setBackground(getContext().getDrawable(
                        android.R.drawable.ic_media_play));
            }
        } else {
            // user is looking at a different track. Play it.
            button.setBackground(getContext().getDrawable(android.R
                    .drawable.ic_media_play));
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(
                R.layout.fragment_scene_detail, container, false);

        if (visibleScene != null) {
            ((TextView) rootView.findViewById(R.id.scene_detail_body))
                    .setText(visibleScene.getDetails());
            ((TextView) rootView.findViewById(R.id.scene_title))
                    .setText(visibleScene.getTitle());
            ((TextView) rootView.findViewById(R.id.scene_location))
                    .setText(visibleScene.getLocation());

        }

        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        final RelativeLayout headerView =
                 getActivity().findViewById(R.id.scene_header);
        button = headerView.findViewById(R.id.btn_pause_play);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                fabClick();
            }
        });
        final CollapsingToolbarLayout appBarLayout
                = getActivity().findViewById(R.id
                .toolbar_layout);
        if (appBarLayout != null) {
            headerView.setVisibility(View.GONE);
            appBarLayout.setTitle(visibleScene.getTitle());
        }
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

    private TranquilAudioApplication getTranquilApp() {
        return (TranquilAudioApplication) getActivity().getApplication();
    }
}
