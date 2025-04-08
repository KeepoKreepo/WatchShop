package com.example.watchshop;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.watchshop.model.FavouriteWatch;
import com.example.watchshop.model.Watch;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class WatchDetailActivity extends AppCompatActivity {
    private Watch watch;
    private ViewFlipper imageFlipper;
    private TextView brandText, modelText, priceText, descriptionText;
    private TextView movementText, caseMaterialText, braceletMaterialText;
    private TextView waterResistanceText, diameterText;
    private FavouriteRepository repository;
    private boolean isFavorite = false;
    private FloatingActionButton fabFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_detail);

        // Get watch from intent
        watch = (Watch) getIntent().getSerializableExtra("watch");
        if (watch == null) {
            finish();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Watch Wonder");

        // Initialize views
        initViews();

        // Set up data
        setWatchDetails();

        // Set up image flipper
        setupImageFlipper();

        // Initialize repository
        repository = new FavouriteRepository(getApplication());

        // Initialize favorite button
        fabFavorite = findViewById(R.id.fab_favourite);

        // Check if watch is favorite
        repository.getFavorite(watch.get_Id()).observe(this, favoriteWatch -> {
            isFavorite = (favoriteWatch != null);
            updateFavoriteIcon();
        });

        // Set favorite button click listener
        fabFavorite.setOnClickListener(v -> toggleFavourite());
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favourite);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favourite_border);
        }
    }

    // In WatchDetailActivity.java
    private void toggleFavourite() {
        Log.d("WatchDetailActivity", "Watch ID: " + watch.get_Id());
        Log.d("WatchDetailActivity", "Favorite status: " + isFavorite);
        if (watch == null || watch.get_Id() == null) {
            Toast.makeText(this, "Cannot update favorite: Invalid watch data", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (isFavorite) {
                // First update UI immediately for better user experience
                isFavorite = false;
                updateFavoriteIcon();
                // Then perform the database operation
                repository.deleteById(watch.get_Id());
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            } else {
                // First update UI immediately for better user experience
                isFavorite = true;
                updateFavoriteIcon();

                // Get first image URL or empty string if no images
                String imageUrl = watch.getImages() != null && !watch.getImages().isEmpty() ?
                        watch.getImages().get(0) : "";

                FavouriteWatch favoriteWatch = new FavouriteWatch(
                        watch.get_Id(),
                        watch.getBrand(),
                        watch.getModel(),
                        watch.getPrice_Range(),
                        imageUrl);

                repository.insert(favoriteWatch);
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("WatchDetailActivity", "Error toggling favorite: " + e.getMessage(), e);
            Toast.makeText(this, "Error updating favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        imageFlipper = findViewById(R.id.image_flipper);
        brandText = findViewById(R.id.brand_text);
        modelText = findViewById(R.id.model_text);
        priceText = findViewById(R.id.price_text);
        descriptionText = findViewById(R.id.description_text);
        movementText = findViewById(R.id.movement_text);
        caseMaterialText = findViewById(R.id.case_material_text);
        braceletMaterialText = findViewById(R.id.bracelet_material_text);
        waterResistanceText = findViewById(R.id.water_resistance_text);
        diameterText = findViewById(R.id.diameter_text);
    }

    private void setWatchDetails() {
        brandText.setText(watch.getBrand());
        modelText.setText(watch.getModel());

        // Format price with currency symbol
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        priceText.setText(formatter.format(watch.getPrice_Range()));

        // Set other details
        movementText.setText(watch.getMovement() != null ? watch.getMovement() : "Not specified");
        caseMaterialText.setText(watch.getCase_Material() != null ? watch.getCase_Material() : "Not specified");
        braceletMaterialText.setText(watch.getBracelet_Material() != null ? watch.getBracelet_Material() : "Not specified");
        waterResistanceText.setText(watch.getWater_Resistance_M() != null ? watch.getWater_Resistance_M() : "Not specified");
        diameterText.setText(watch.getDiameter() != null ? watch.getDiameter() : "Not specified");
        descriptionText.setText(watch.getDescription());
    }

    private void setupImageFlipper() {
        // Clear any existing views
        imageFlipper.removeAllViews();

        // Add all images to ViewFlipper
        if (watch.getImages() != null && !watch.getImages().isEmpty()) {
            for (String imageUrl : watch.getImages()) {
                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageFlipper.addView(imageView);

                // Load image with Glide
                Glide.with(this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(imageView);
            }

            // Start flipping if there is more than one image
            if (watch.getImages().size() > 1) {
                imageFlipper.setAutoStart(true);
                imageFlipper.setFlipInterval(3000);
                imageFlipper.startFlipping();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}