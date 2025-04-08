// FavoriteWatch.java
package com.example.watchshop.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavouriteWatch {
    @PrimaryKey
    @NonNull
    private String watchId;
    private String brand;
    private String model;
    private double priceRange;
    private String imageUrl;

    // Constructor
    public FavouriteWatch(@NonNull String watchId, String brand, String model, double priceRange, String imageUrl) {
        this.watchId = watchId;
        this.brand = brand;
        this.model = model;
        this.priceRange = priceRange;
        this.imageUrl = imageUrl;
    }

    // Getters
    @NonNull
    public String getWatchId() {
        return watchId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public double getPriceRange() {
        return priceRange;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setWatchId(@NonNull String watchId) {
        this.watchId = watchId;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPriceRange(double priceRange) {
        this.priceRange = priceRange;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}