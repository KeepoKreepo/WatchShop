package com.example.watchshop; // Use your package name

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Import your specific classes
import com.example.watchshop.adapter.WatchAdapter;
import com.example.watchshop.network.ApiClient;
import com.example.watchshop.network.ApiService;
import com.example.watchshop.manager.WatchManager;
import com.example.watchshop.model.Watch;

// Firebase Auth import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Tag for logging

    private WatchAdapter adapter;
    private ProgressBar progressBar;
    private List<Watch> allWatches = new ArrayList<>();
    private FirebaseAuth mAuth; // Firebase Auth instance

    // Keep your SortOption enum and currentSortOption variable
    private enum SortOption {
        PRICE_LOW_HIGH,
        PRICE_HIGH_LOW,
        BRAND_AZ,
        BRAND_ZA
    }
    private SortOption currentSortOption = SortOption.BRAND_AZ; // Default sort

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure this layout exists

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is logged in, if not, redirect to LoginActivity
        // This check is crucial if MainActivity could somehow be reached without logging in
        // Although LoginActivity's onStart should prevent this in normal flow.
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, redirecting to LoginActivity.");
            navigateToLogin();
            return; // Stop further execution in onCreate
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar); // Ensure R.id.toolbar exists
        setSupportActionBar(toolbar);
        // Optional: Set title
        // getSupportActionBar().setTitle("Watch Shop");

        // Initialize views
        RecyclerView recyclerView = findViewById(R.id.watches_recycler_view); // Ensure R.id.watches_recycler_view exists
        progressBar = findViewById(R.id.progress_bar); // Ensure R.id.progress_bar exists

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Assuming WatchAdapter constructor takes context and initial empty list
        adapter = new WatchAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Load watches (from API)
        loadWatches();
    }

    // --- Menu Handling ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        // Handle Favourites click
        if (id == R.id.action_favourites) { // Use the ID from main_menu.xml
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);
            return true;
        }
        // --- Handle Logout Click ---
        else if (id == R.id.action_logout) { // Use the new ID from main_menu.xml
            logoutUser();
            return true;
        }
        // Handle sorting clicks (keep your existing logic)
        else if (id == R.id.sort_price_low_high) {
            currentSortOption = SortOption.PRICE_LOW_HIGH;
            sortAndDisplayWatches();
            return true;
        } else if (id == R.id.sort_price_high_low) {
            currentSortOption = SortOption.PRICE_HIGH_LOW;
            sortAndDisplayWatches();
            return true;
        } else if (id == R.id.sort_brand_az) {
            currentSortOption = SortOption.BRAND_AZ;
            sortAndDisplayWatches();
            return true;
        } else if (id == R.id.sort_brand_za) {
            currentSortOption = SortOption.BRAND_ZA;
            sortAndDisplayWatches();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // --- Logout Method ---
    private void logoutUser() {
        Log.d(TAG, "logoutUser: Signing out user.");
        mAuth.signOut(); // Sign out from Firebase Authentication
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin(); // Navigate back to LoginActivity
    }

    // --- Navigation Method (to Login) ---
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Clear the back stack and start LoginActivity as a new task
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish MainActivity so user cannot press back to return here
    }


    // --- Your existing methods (loadWatches, displayRandomWatches, sortAndDisplayWatches) ---
    // Keep these methods as they were, no changes needed here for logout.

    private void loadWatches() {
        // ... (your existing API call logic using Retrofit) ...
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Watch>> call = apiService.getWatches();

        call.enqueue(new Callback<List<Watch>>() {
            @Override
            public void onResponse(@NonNull Call<List<Watch>> call, @NonNull Response<List<Watch>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allWatches = response.body();
                    WatchManager.getInstance().setWatches(allWatches); // Assuming WatchManager usage
                    displayRandomWatches(); // Or sortAndDisplayWatches based on preference
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load watches", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Watch>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "API Call Failed: ", t);
                String errorMessage;
                // Basic network error checking
                if (t.getMessage() != null && (t.getMessage().contains("Unable to resolve host") || t.getMessage().contains("No address associated"))) {
                    errorMessage = "Cannot connect to server. Please check internet.";
                } else {
                    errorMessage = "Network error: " + t.getMessage();
                }
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayRandomWatches() {
//        // ... (your existing logic to shuffle/limit/sort and update adapter) ...
//        if (currentSortOption != null) {
//            sortAndDisplayWatches();
//            return;
//        }
        if (allWatches == null || allWatches.isEmpty()) return;

        List<Watch> shuffledWatches = new ArrayList<>(allWatches);
        Collections.shuffle(shuffledWatches);
        int limitCount = Math.min(5, shuffledWatches.size());
        List<Watch> limitedWatches = shuffledWatches.subList(0, limitCount);
        adapter.setWatches(limitedWatches); // Assuming adapter has setWatches method
    }

    private void sortAndDisplayWatches() {
        // ... (your existing sorting logic) ...
        if (allWatches == null || allWatches.isEmpty()) return;

        List<Watch> sortedWatches = new ArrayList<>(allWatches);
        // Apply sorting based on currentSortOption
        switch (currentSortOption) {
            case PRICE_LOW_HIGH:
                sortedWatches.sort(Comparator.comparingDouble(Watch::getPrice_Range)); // Check getter name
                break;
            case PRICE_HIGH_LOW:
                sortedWatches.sort((w1, w2) -> Double.compare(w2.getPrice_Range(), w1.getPrice_Range())); // Check getter name
                break;
            case BRAND_AZ:
                sortedWatches.sort((w1, w2) -> w1.getBrand().compareToIgnoreCase(w2.getBrand()));
                break;
            case BRAND_ZA:
                sortedWatches.sort((w1, w2) -> w2.getBrand().compareToIgnoreCase(w1.getBrand()));
                break;
        }

        int limitCount = Math.min(5, sortedWatches.size());
        List<Watch> limitedWatches = sortedWatches.subList(0, limitCount);
        adapter.setWatches(limitedWatches);
    }

}
