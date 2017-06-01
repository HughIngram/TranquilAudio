package uk.co.tranquilaudio.tranquilaudio;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Scene detail screen.
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
     * The dummy content this fragment is presenting.
     */
    private Scene mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SceneDetailFragment() {
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ContentLoader loader = new ContentLoader(this.getContext());

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = loader.getItemMap().get(getArguments().getString(ARG_ITEM_ID));

            final Activity activity = this.getActivity();
            final CollapsingToolbarLayout appBarLayout
                    = (CollapsingToolbarLayout) activity
                    .findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.content);
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.scene_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.scene_detail)).setText(mItem.details);
        }

        return rootView;
    }
}
