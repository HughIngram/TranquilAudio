package uk.co.tranquilaudio.tranquilaudio;


import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to the details of scenes.
 * Currently data is obtained from ....
 * In future it will be obtained over the network and/or cached locally
 * and accessed via a content provider.
 */
final class ContentLoader {

    /**
     * An array of sample (dummy) items.
     */
    private final List<Scene> items = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    private final Map<String, Scene> itemMap = new HashMap<>();

    ContentLoader(final Context context) {
        addItem(new Scene(1, context.getString(R.string.scene_title_1),
                context.getString(R.string.scene_details_1),
                R.raw.sound_clip_1));
        addItem(new Scene(2, context.getString(R.string.scene_title_2),
                context.getString(R.string.scene_details_1),
                R.raw.sound_clip_1));
        addItem(new Scene(3, context.getString(R.string.scene_title_3),
                context.getString(R.string.scene_details_1),
                R.raw.sound_clip_1));
        addItem(new Scene(4, context.getString(R.string.scene_title_4),
                context.getString(R.string.scene_details_1),
                R.raw.sound_clip_1));
        addItem(new Scene(5, context.getString(R.string.scene_title_5),
                context.getString(R.string.scene_details_1),
                R.raw.sound_clip_1));
    }

    List<Scene> getItems() {
        return items;
    }

    Map<String, Scene> getItemMap() {
        return itemMap;
    }

    private void addItem(final Scene item) {
        items.add(item);
        itemMap.put(item.getIdString(), item);
    }

}
