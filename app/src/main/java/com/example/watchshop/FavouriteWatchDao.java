package com.example.watchshop;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.watchshop.model.FavouriteWatch;

import java.util.List;

@Dao
public interface  FavouriteWatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavouriteWatch watch);

    @Delete
    void delete(FavouriteWatch watch);

    @Query("SELECT * FROM favorites ORDER BY brand ASC")
    LiveData<List<FavouriteWatch>> getAllFavorites();

    @Query("SELECT * FROM favorites WHERE watchId = :id")
    LiveData<FavouriteWatch> getFavorite(String id);

    @Query("DELETE FROM favorites WHERE watchId = :id")
    void deleteById(String id);

}
