package com.example.watchshop; // Replace with your actual package name

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.watchshop.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp; // Import Timestamp

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.example.watchshop.model.Watch;

public class WatchDetailActivity extends AppCompatActivity {

    private Watch watch;
    private ViewFlipper imageFlipper;
    private TextView brandText, modelText, priceText, descriptionText;
    private TextView movementText, caseMaterialText, braceletMaterialText;
    private TextView waterResistanceText, diameterText;
    // private FavouriteRepository repository; // No longer needed for favorite toggle
    private boolean isFavorite = false; // Reflects Firestore state now
    private FloatingActionButton fabFavorite;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_detail);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get watch from intent
        watch = (Watch) getIntent().getSerializableExtra("watch");
        if (watch == null || watch.get_Id() == null) { // Also check ID early
            Toast.makeText(this, "Error loading watch details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // Consider setting title dynamically e.g., getSupportActionBar().setTitle(watch.getBrand() + " " + watch.getModel());
        getSupportActionBar().setTitle("Watch Wonder");


        // Initialize views
        initViews();

        // Set up data
        setWatchDetails();

        // Set up image flipper
        setupImageFlipper();

        // Initialize favorite button
        fabFavorite = findViewById(R.id.fab_favourite);

        // --- Check favorite status using Firestore ---
        checkFavoriteStatus();

        // Set favorite button click listener
        fabFavorite.setOnClickListener(v -> toggleFavourite());
    }

    private void checkFavoriteStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String watchId = watch.get_Id(); // Already checked watch and ID non-null in onCreate

            DocumentReference favRef = db.collection("users").document(userId)
                    .collection("favorites").document(watchId);

            // Listen for real-time changes
            favRef.addSnapshotListener(this, (snapshot, error) -> {
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error);
                    fabFavorite.setEnabled(false); // Disable button on error
                    Toast.makeText(this, "Could not check favorite status", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null) {
                    isFavorite = snapshot.exists(); // Update flag based on Firestore
                    updateFavoriteIcon();
                    fabFavorite.setEnabled(true); // Ensure button is enabled
                    Log.d("Firestore", "Current favorite status for " + watchId + ": " + isFavorite);
                } else {
                    Log.d("Firestore", "Favorite snapshot is null (no error)");
                    isFavorite = false; // Assume not favorite if snapshot is null without error
                    updateFavoriteIcon();
                    fabFavorite.setEnabled(true);
                }
            });
        } else {
            // User not logged in
            Log.w("Firestore", "User not logged in. Cannot check favorite status.");
            fabFavorite.setEnabled(false);
            // Optionally hide the button or show a login prompt if clicked
            // fabFavorite.setVisibility(View.GONE);
        }
    }


    private void updateFavoriteIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favourite); // Filled heart icon
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favourite_border); // Border heart icon
        }
    }

    private void toggleFavourite() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Check login status first
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to manage favorites", Toast.LENGTH_SHORT).show();
            // Consider navigating to a login screen:
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            return;
        }

        // Watch and ID null checks already done in onCreate, but double-check just in case.
        if (watch == null || watch.get_Id() == null) {
            Toast.makeText(this, "Cannot update favorite: Invalid watch data", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String watchId = watch.get_Id();
        DocumentReference favRef = db.collection("users").document(userId)
                .collection("favorites").document(watchId);

        // Determine action based on the CURRENT state (driven by Firestore listener)
        if (isFavorite) {
            // --- Action: Remove from Favorites ---
            favRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Favorite successfully removed.");
                        Toast.makeText(WatchDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                        // UI will update via the snapshot listener automatically
                        // No need to manually set isFavorite or call updateFavoriteIcon here
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error removing favorite", e);
                        Toast.makeText(WatchDetailActivity.this, "Error removing favorite", Toast.LENGTH_SHORT).show();
                        // The listener will likely still reflect the old state, so UI should be correct.
                    });
        } else {
            // --- Action: Add to Favorites ---
            // Create data to store (e.g., a timestamp for when it was added)
            Map<String, Object> favData = new HashMap<>();
            favData.put("addedAt", Timestamp.now()); // Use Firestore Timestamp

            favRef.set(favData) // Use set() to create the document or overwrite if it somehow exists
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Favorite successfully added.");
                        Toast.makeText(WatchDetailActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                        // UI will update via the snapshot listener automatically
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error adding favorite", e);
                        Toast.makeText(WatchDetailActivity.this, "Error adding favorite", Toast.LENGTH_SHORT).show();
                        // The listener will likely still reflect the old state, so UI should be correct.
                    });
        }
    }


    // --- Helper methods (initViews, setWatchDetails, setupImageFlipper) ---
    // Assume these are mostly unchanged unless they relied on FavouriteRepository

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
        // fabFavorite is initialized in onCreate
    }

    private void setWatchDetails() {
        brandText.setText(watch.getBrand());
        modelText.setText(watch.getModel());

        // Format price with currency symbol
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        priceText.setText(formatter.format(watch.getPrice_Range())); // Ensure getPrice_Range() exists

        // Set other details safely
        movementText.setText(watch.getMovement() != null ? watch.getMovement() : "Not specified");
        caseMaterialText.setText(watch.getCase_Material() != null ? watch.getCase_Material() : "Not specified"); // Check getter names
        braceletMaterialText.setText(watch.getBracelet_Material() != null ? watch.getBracelet_Material() : "Not specified"); // Check getter names
        waterResistanceText.setText(watch.getWater_Resistance_M() != null ? watch.getWater_Resistance_M() : "Not specified"); // Check getter names
        diameterText.setText(watch.getDiameter() != null ? watch.getDiameter() : "Not specified");
        descriptionText.setText(watch.getDescription() != null ? watch.getDescription() : "No description available.");
    }

    private void setupImageFlipper() {
        imageFlipper.removeAllViews(); // Clear previous images if any

        if (watch.getImages() != null && !watch.getImages().isEmpty()) {
            for (String imageUrl : watch.getImages()) {
                ImageView imageView = new ImageView(this);
                // Set layout params if needed, e.g., to MATCH_PARENT
                imageView.setLayoutParams(new ViewFlipper.LayoutParams(
                        ViewFlipper.LayoutParams.MATCH_PARENT,
                        ViewFlipper.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageFlipper.addView(imageView);

                // Load image with Glide
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder) // Add a placeholder drawable
                        .error(R.drawable.ic_error) // Add an error drawable
                        .centerCrop()
                        .into(imageView);
            }

            // Add flipping animation and timing
            if (watch.getImages().size() > 1) {
                imageFlipper.setInAnimation(this, android.R.anim.fade_in);
                imageFlipper.setOutAnimation(this, android.R.anim.fade_out);
                imageFlipper.setFlipInterval(3000); // 3 seconds
                imageFlipper.setAutoStart(true);
                imageFlipper.startFlipping();
            }
        } else {
            // Handle case with no images, maybe show a placeholder
            ImageView placeholderView = new ImageView(this);
            placeholderView.setLayoutParams(new ViewFlipper.LayoutParams(
                    ViewFlipper.LayoutParams.MATCH_PARENT,
                    ViewFlipper.LayoutParams.MATCH_PARENT));
            placeholderView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            placeholderView.setImageResource(R.drawable.ic_placeholder); // Use your placeholder
            imageFlipper.addView(placeholderView);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        // Handles the back arrow in the toolbar
        onBackPressed(); // Standard behavior
        return true;
    }
}
