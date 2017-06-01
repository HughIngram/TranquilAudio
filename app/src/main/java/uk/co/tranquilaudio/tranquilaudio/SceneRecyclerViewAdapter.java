package uk.co.tranquilaudio.tranquilaudio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import uk.co.tranquilaudio.tranquilaudio.content.Scene;

/**
 * Created by hugh on 25/05/17.
 */
public class SceneRecyclerViewAdapter
        extends RecyclerView.Adapter<SceneRecyclerViewAdapter.ViewHolder> {

    private SceneListActivity sceneListActivity;
    //TODO refactor
    final SceneListActivity activity;
    private final List<Scene> mValues;

    public SceneRecyclerViewAdapter(
            SceneListActivity sceneListActivity, List<Scene> items, SceneListActivity activity) {
        this.sceneListActivity = sceneListActivity;
        mValues = items;
        this.activity = activity;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scene_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(SceneDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                    SceneDetailFragment fragment = new SceneDetailFragment();
                    fragment.setArguments(arguments);
                    sceneListActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.scene_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, SceneDetailActivity.class);
                    intent.putExtra(SceneDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Scene mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
