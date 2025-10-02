// WatchManager.java
package com.example.watchshop.manager;

import com.example.watchshop.model.Watch;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchManager {
    private static WatchManager instance;
    private final Map<String, Watch> watchesById = new HashMap<>();

    private WatchManager() {}

    public static synchronized WatchManager getInstance() {
        if (instance == null) {
            instance = new WatchManager();
        }
        return instance;
    }

    public void setWatches(List<Watch> watches) {
        for (Watch watch : watches) {
            // Use _id as the key since we know that's the MongoDB ID
            if (watch.get_Id() != null) {
                watchesById.put(watch.get_Id(), watch);
            }
        }
    }

    public Watch getWatchById(String id) {
        return watchesById.get(id);
    }
}