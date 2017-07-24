package com.tranquilaudio.tranquilaudio_app;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tranquilaudio.tranquilaudio_app.model.AudioScene;
import com.tranquilaudio.tranquilaudio_app.model.AudioSceneLoader;
import com.tranquilaudio.tranquilaudio_app.model.AudioSceneLoaderImpl;
import com.tranquilaudio.tranquilaudio_app.model.SystemWrapperForModelImpl;


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
    private AudioScene mItem;

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
            final AudioSceneLoader loader = new AudioSceneLoaderImpl(
                    new SystemWrapperForModelImpl(this.getContext()));
            mItem = loader.getScene(getArguments().getLong(ARG_ITEM_ID));

            final Activity activity = this.getActivity();
            final CollapsingToolbarLayout appBarLayout
                    = (CollapsingToolbarLayout) activity
                    .findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getTitle());
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.scene_detail, container, false);

        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.scene_detail))
                    .setText(mItem.getDetails());
            ((TextView) rootView.findViewById(R.id.scene_title))
                    .setText(mItem.getTitle());
        }

        return rootView;
    }
}
