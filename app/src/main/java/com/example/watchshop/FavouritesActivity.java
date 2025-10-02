package com.example.watchshop; // Make sure this matches your package name

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Import your specific adapter, models, and manager
import com.example.watchshop.adapter.FavouriteWatchAdapter;
import com.example.watchshop.model.FavouriteWatch;
import com.example.watchshop.model.Watch;
import com.example.watchshop.manager.WatchManager; // Assuming WatchManager path

// Firebase Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot; // Keep this import for lambda signature

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FavouritesActivity extends AppCompatActivity {

    private static final String TAG = "FavouritesActivity"; // Tag for logging

    private RecyclerView recyclerView;
    private FavouriteWatchAdapter adapter;
    private TextView emptyView; // TextView to display when the list is empty or user is logged out

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration favoriteListenerRegistration; // To manage the Firestore listener lifecycle

    // List to hold the IDs fetched from Firestore
    private List<String> currentFavoriteIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites); // Ensure this layout file exists

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- Toolbar Setup ---
        Toolbar toolbar = findViewById(R.id.toolbar); // Ensure R.id.toolbar exists
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Favorites"); // Or load from R.string

        // --- Initialize Views ---
        recyclerView = findViewById(R.id.favourites_recycler_view); // Ensure R.id.favourites_recycler_view exists
        emptyView = findViewById(R.id.empty_view); // Ensure R.id.empty_view exists

        // --- Setup RecyclerView ---
        setupRecyclerView();

        // Note: Data loading is now triggered in onStart to handle lifecycle correctly
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start listening for Firestore updates when the activity becomes visible.
        // This ensures data is fresh if the user navigates away and back, or logs in.
        Log.d(TAG, "onStart: Starting to load favorites from Firestore.");
        loadFavoritesFromFirestore();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Detach the Firestore listener when the activity is no longer visible
        // to prevent memory leaks and unnecessary background processing.
        if (favoriteListenerRegistration != null) {
            Log.d(TAG, "onStop: Removing Firestore listener.");
            favoriteListenerRegistration.remove();
        }
    }

    /**
     * Sets up the RecyclerView with its LayoutManager and Adapter.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Create the adapter (it initializes its own Firebase instances now or could receive them)
        adapter = new FavouriteWatchAdapter(this);
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup complete.");
    }

    /**
     * Checks login status and attaches a Firestore listener to the user's favorites collection.
     * Handles the case where the user is not logged in.
     */
    private void loadFavoritesFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "User " + userId + " logged in. Attaching Firestore listener.");

            // Show loading state initially
            updateUIForLoading();

            // Reference to the user's specific "favorites" subcollection
            CollectionReference favCollectionRef = db.collection("users").document(userId)
                    .collection("favorites");

            // Remove any existing listener before attaching a new one (important for onStart)
            if (favoriteListenerRegistration != null) {
                favoriteListenerRegistration.remove();
            }

            // Attach the listener for real-time updates
            favoriteListenerRegistration = favCollectionRef.addSnapshotListener(this, (snapshots, error) -> {
                // This lambda runs on the main thread by default

                if (error != null) {
                    Log.e(TAG, "Firestore listen error:", error);
                    Toast.makeText(this, "Error loading favorites", Toast.LENGTH_SHORT).show();
                    updateUIForEmptyList("Error loading favorites."); // Show error message
                    return;
                }

                if (snapshots != null) {
                    // Process the snapshot containing favorite document IDs
                    currentFavoriteIds.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        if (doc.getId() != null && !doc.getId().isEmpty()) {
                            currentFavoriteIds.add(doc.getId()); // The document ID *is* the watchId
                        } else {
                            Log.w(TAG, "Favorite document found with null or empty ID. Skipping.");
                        }
                    }
                    Log.d(TAG, "Firestore listener received " + currentFavoriteIds.size() + " favorite IDs.");
                    // Now fetch the detailed watch information for these IDs
                    fetchWatchDetailsForFavorites();
                } else {
                    Log.w(TAG, "Firestore snapshot was null (no error). Treating as empty.");
                    currentFavoriteIds.clear();
                    fetchWatchDetailsForFavorites(); // Process empty list
                }
            });

        } else {
            // Handle user not logged in
            Log.d(TAG, "User not logged in. Displaying logged-out message.");
            updateUIForEmptyList("Please log in to see your favorites.");
            // Ensure listener is detached if user logs out
            if (favoriteListenerRegistration != null) {
                favoriteListenerRegistration.remove();
            }
        }
    }

    /**
     * Fetches full watch details for the list of favorite IDs obtained from Firestore.
     * Uses WatchManager as the primary source for details. Updates the RecyclerView adapter.
     */
    private void fetchWatchDetailsForFavorites() {
        List<FavouriteWatch> detailedFavorites = new ArrayList<>();

        if (currentFavoriteIds.isEmpty()) {
            Log.d(TAG, "No favorite IDs to fetch details for. Updating UI for empty list.");
            updateUIForEmptyList("You haven't added any favorites yet.");
            return;
        }

        Log.d(TAG, "Fetching details for " + currentFavoriteIds.size() + " favorite IDs using WatchManager.");
        WatchManager watchManager = WatchManager.getInstance(); // Assuming singleton access

        for (String watchId : currentFavoriteIds) {
            Watch fullWatch = watchManager.getWatchById(watchId); // Attempt to get details
            if (fullWatch != null) {
                // Convert the full Watch object to a FavouriteWatch object for the adapter
                // Ensure the FavouriteWatch constructor matches these parameters
                String imageUrl = (fullWatch.getImages() != null && !fullWatch.getImages().isEmpty()) ?
                        fullWatch.getImages().get(0) : ""; // Safely get first image URL
                try {
                    FavouriteWatch fav = new FavouriteWatch(
                            fullWatch.get_Id(),      // Pass ID
                            fullWatch.getBrand(),    // Pass Brand
                            fullWatch.getModel(),    // Pass Model
                            fullWatch.getPrice_Range(), // Pass Price
                            imageUrl                 // Pass Image URL
                    );
                    detailedFavorites.add(fav);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating FavouriteWatch object for ID: " + watchId, e);
                    // Decide if you want to skip or add a placeholder
                }
            } else {
                // Watch details were not found in WatchManager for this ID
                Log.w(TAG, "Watch details not found in WatchManager for favorite ID: " + watchId + ". Skipping this item.");
                // Optionally, you could try an API fallback here or add a placeholder FavouriteWatch object
            }
        }

        Log.d(TAG, "Successfully created " + detailedFavorites.size() + " FavouriteWatch objects for adapter.");
        // Update the adapter with the final list of detailed favorites
        adapter.setFavorites(detailedFavorites);

        // Update UI visibility based on whether the final list has items
        if (detailedFavorites.isEmpty()) {
            // This could happen if IDs existed in Firestore but details weren't found
            Log.d(TAG, "Final detailed favorites list is empty. Updating UI.");
            updateUIForEmptyList("Could not load details for your favorites.");
        } else {
            Log.d(TAG, "Displaying " + detailedFavorites.size() + " favorites in RecyclerView.");
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the UI to show a loading state.
     */
    private void updateUIForLoading() {
        recyclerView.setVisibility(View.GONE); // Hide list while loading
        emptyView.setText("Loading favorites..."); // Show loading message
        emptyView.setVisibility(View.VISIBLE);
        // Clear adapter data from previous loads
        if (adapter != null) {
            adapter.setFavorites(new ArrayList<>());
        }
    }


    /**
     * Helper method to update UI visibility and message when the favorites list is empty,
     * the user is logged out, or an error occurs.
     * @param message The message to display in the emptyView TextView.
     */
    private void updateUIForEmptyList(String message) {
        recyclerView.setVisibility(View.GONE); // Hide the list
        emptyView.setText(message); // Set the informative message
        emptyView.setVisibility(View.VISIBLE); // Show the message view
        // Clear adapter data to ensure consistency
        if (adapter != null) {
            adapter.setFavorites(new ArrayList<>());
        }
        Log.d(TAG, "Updated UI for empty/error state with message: " + message);
    }

    /**
     * Handles the Up button navigation in the toolbar.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Standard behavior: navigate back
        return true;
    }
}
