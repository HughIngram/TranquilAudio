package uk.co.tranquilaudio.tranquilaudio.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Scene> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Scene> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 5;

    static {
        // TODO put dummy data in xml
        addItem(new Scene("1", "Reigate Hill, England", makeDetails(1)) {});
        addItem(new Scene("2", "Redhill, England", makeDetails(2)) {});
        addItem(new Scene("3", "Aylestone Meadows, Leicester, England", makeDetails(3)) {});
        addItem(new Scene("4", "Foxton Lochs, Leicestershire, England", makeDetails(4)) {});
        addItem(new Scene("5", "Box Hill, England", makeDetails(5)) {});
    }

    private static void addItem(Scene item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Scene createDummyItem(int position) {
        return new Scene(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
