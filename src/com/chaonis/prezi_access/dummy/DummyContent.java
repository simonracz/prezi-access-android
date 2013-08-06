package com.chaonis.prezi_access.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chaonis.prezi_access.PreziItem;

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
	public static List<PreziItem> ITEMS = new ArrayList<PreziItem>();
	
	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, PreziItem> ITEM_MAP = new HashMap<String, PreziItem>();

	public static void addItem(PreziItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.dummyId, item);
	}

}
