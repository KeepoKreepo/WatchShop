// WatchDatabase.java
package com.example.watchshop.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.watchshop.FavouriteWatchDao;
import com.example.watchshop.model.FavouriteWatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {FavouriteWatch.class}, version = 1)
public abstract class WatchDatabase extends RoomDatabase {
    public abstract FavouriteWatchDao favouriteWatchDao();

    private static volatile WatchDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static WatchDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WatchDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    WatchDatabase.class, "watch_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
