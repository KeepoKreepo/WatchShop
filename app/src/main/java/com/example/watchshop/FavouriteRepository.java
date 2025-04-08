package com.example.watchshop;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.watchshop.model.FavouriteWatch;

import java.util.List;

public class FavouriteRepository {
    private final FavouriteWatchDao favouriteWatchDao;
    private final LiveData<List<FavouriteWatch>> allFavourites;

    public FavouriteRepository(Application application) {
        WatchDatabase db = WatchDatabase.getDatabase(application);
        favouriteWatchDao = db.favouriteWatchDao();
        allFavourites = favouriteWatchDao.getAllFavorites();
    }

    public LiveData<List<FavouriteWatch>> getAllFavorites() {
        return allFavourites;
    }

    public LiveData<FavouriteWatch> getFavorite(String id) {
        return favouriteWatchDao.getFavorite(id);
    }

    public void insert(FavouriteWatch watch) {
        WatchDatabase.databaseWriteExecutor.execute(() -> {
            try {
                favouriteWatchDao.insert(watch);
            } catch (Exception e) {
                Log.e("FavouriteRepository", "Error inserting: " + e.getMessage());
            }
        });
    }

    public void deleteById(String id) {
        WatchDatabase.databaseWriteExecutor.execute(() -> {
            try {
                favouriteWatchDao.deleteById(id);
            } catch (Exception e) {
                Log.e("FavouriteRepository", "Error deleting: " + e.getMessage());
            }
        });
    }
}
