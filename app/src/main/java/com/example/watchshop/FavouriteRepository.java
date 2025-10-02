package com.example.watchshop; // Use your actual package

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;

import com.example.watchshop.database.WatchDatabase; // Use your actual database path
import com.example.watchshop.model.FavouriteWatch; // Use your actual model path

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


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
                Log.e("FavouriteRepository", "Error deleting by ID: " + e.getMessage());
            }
        });
    }

    public void delete(FavouriteWatch watch) {
        WatchDatabase.databaseWriteExecutor.execute(() -> {
            try {
                favouriteWatchDao.delete(watch);
            } catch (Exception e) {
                Log.e("FavouriteRepository", "Error deleting watch object: " + e.getMessage());
            }
        });
    }

    public List<FavouriteWatch> getAllFavoritesBlocking() {
        // Use the executor service to run the blocking DAO call and get the result
        Callable<List<FavouriteWatch>> callable = favouriteWatchDao::getAllFavoritesBlocking;
        Future<List<FavouriteWatch>> future = WatchDatabase.databaseWriteExecutor.submit(callable);
        try {
            return future.get(); // This waits for the result
        } catch (ExecutionException | InterruptedException e) {
            Log.e("FavouriteRepository", "Error getting blocking favorites", e);
            Thread.currentThread().interrupt(); // Restore interrupt status
            return null;
        }
    }

    public void deleteAllLocalFavorites() {
        WatchDatabase.databaseWriteExecutor.execute(() -> {
            try {
                favouriteWatchDao.deleteAll();
                Log.d("FavouriteRepository", "Deleted all local favorites from Room.");
            } catch (Exception e) {
                Log.e("FavouriteRepository", "Error deleting all local favorites: " + e.getMessage());
            }
        });
    }
}
