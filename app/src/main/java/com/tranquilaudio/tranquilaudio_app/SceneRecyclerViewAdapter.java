package com.tranquilaudio.tranquilaudio_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for Scene elements.
 */
final class SceneRecyclerViewAdapter
        extends RecyclerView.Adapter<SceneRecyclerViewAdapter.ViewHolder> {

    private final SceneListActivity sceneListActivity;
    private final List<Scene> scenes;

    /**
     * Constructor.
     * @param sceneListActivity the holding activity.
     * @param scenes scene scenes.
     */
    SceneRecyclerViewAdapter(final SceneListActivity sceneListActivity,
                             final List<Scene> scenes) {
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
        holder.mIdView.setText(scenes.get(position).getIdString());
        holder.mContentView.setText(scenes.get(position).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (sceneListActivity.isTwoPane()) {
                    final Bundle arguments = new Bundle();
                    arguments.putString(
                            SceneDetailFragment.ARG_ITEM_ID,
                            holder.mItem.getIdString());
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
                            holder.mItem.getIdString());

                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public long getItemId(final int position) {
//        return scenes.get(position).getId();
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
        private final TextView mIdView;
        private final TextView mContentView;
        private Scene mItem;

        ViewHolder(final View view) {
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
