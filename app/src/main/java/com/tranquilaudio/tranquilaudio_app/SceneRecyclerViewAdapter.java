package com.tranquilaudio.tranquilaudio_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tranquilaudio.tranquilaudio_app.data.AudioScene;

import java.util.List;

/**
 * Adapter for AudioScene elements.
 */
final class SceneRecyclerViewAdapter
        extends RecyclerView.Adapter<SceneRecyclerViewAdapter.ViewHolder> {

    private final SceneListActivity sceneListActivity;
    private final List<AudioScene> scenes;

    /**
     * Constructor.
     * @param sceneListActivity the holding activity.
     * @param scenes scene scenes.
     */
    SceneRecyclerViewAdapter(final SceneListActivity sceneListActivity,
                             final List<AudioScene> scenes) {
        this.sceneListActivity = sceneListActivity;
        this.scenes = scenes;
    }


    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent,
                                         final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scene_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = scenes.get(position);
        holder.mContentView.setText(scenes.get(position).getTitle());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (sceneListActivity.isTwoPane()) {
                    final Bundle arguments = new Bundle();
                    arguments.putLong(
                            SceneDetailFragment.ARG_ITEM_ID,
                            holder.mItem.getId());
                    final SceneDetailFragment fragment
                            = new SceneDetailFragment();
                    fragment.setArguments(arguments);
                    sceneListActivity
                            .getSupportFragmentManager().beginTransaction()
                            .replace(R.id.scene_detail_container, fragment)
                            .commit();
                } else {
                    final Context context = v.getContext();
                    final Intent intent
                            = new Intent(context, SceneDetailActivity.class);
                    intent.putExtra(
                            SceneDetailFragment.ARG_ITEM_ID,
                            holder.mItem.getId());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return scenes.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return position;
    }

    /**
     * View holder for list items.
     */
    final class ViewHolder extends RecyclerView.ViewHolder {

        private final View mView;
        private final TextView mContentView;
        private AudioScene mItem;

        ViewHolder(final View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.title);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
