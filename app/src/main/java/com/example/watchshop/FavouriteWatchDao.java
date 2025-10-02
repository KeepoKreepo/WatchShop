package com.example.watchshop; // Use your actual package

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.watchshop.model.FavouriteWatch; // Use your actual model path

import java.util.List;

@Dao
public interface FavouriteWatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavouriteWatch watch);

    @Delete
    void delete(FavouriteWatch watch);

    @Query("SELECT * FROM favorites ORDER BY brand ASC") // Assuming 'favorites' is your table name
    LiveData<List<FavouriteWatch>> getAllFavorites();

    @Query("SELECT * FROM favorites WHERE watchId = :id") // Assuming 'favorites' is your table name
    LiveData<FavouriteWatch> getFavorite(String id);

    @Query("DELETE FROM favorites WHERE watchId = :id") // Assuming 'favorites' is your table name
    void deleteById(String id);

    // --- Methods Added for Migration ---

    /**
     * Gets all favorite watches synchronously.
     * IMPORTANT: Call this from a background thread.
     * @return List of all FavouriteWatch objects.
     */
    @Query("SELECT * FROM favorites") // Assuming 'favorites' is your table name
    List<FavouriteWatch> getAllFavoritesBlocking(); // Non-LiveData version for migration

    /**
     * Deletes all entries from the favorites table.
     * IMPORTANT: Call this from a background thread.
     */
    @Query("DELETE FROM favorites") // Assuming 'favorites' is your table name
    void deleteAll(); // Method to clear the table after migration
}
