package com.example.watchshop;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.watchshop.adapter.WatchAdapter;
import com.example.watchshop.model.Watch;
import com.example.watchshop.network.ApiClient;
import com.example.watchshop.network.ApiService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private WatchAdapter adapter;
    private ProgressBar progressBar;
    private List<Watch> allWatches = new ArrayList<>();

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
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        RecyclerView recyclerView = findViewById(R.id.watches_recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WatchAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Load watches
        loadWatches();
    }

    private void loadWatches() {

        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Watch>> call = apiService.getWatches();

        call.enqueue(new Callback<List<Watch>>() {
            @Override
            public void onResponse(@NonNull Call<List<Watch>> call, @NonNull Response<List<Watch>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allWatches = response.body();

                    // Store watches in WatchManager
                    WatchManager.getInstance().setWatches(allWatches);

                    displayRandomWatches();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load watches", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Watch>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);

                String errorMessage;
                if (Objects.requireNonNull(t.getMessage()).contains("Unable to resolve host") ||
                        t.getMessage().contains("No address associated")) {
                    errorMessage = "Cannot connect to server. Please check your internet connection.";
                } else {
                    errorMessage = "Network error: " + t.getMessage();
                }

                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRandomWatches() {
//        // If a sort preference is active, use that instead of random
//        if (currentSortOption != null) {
//            sortAndDisplayWatches();
//            return;
//        }

        // Original shuffle logic if no sort selected
        List<Watch> shuffledWatches = new ArrayList<>(allWatches);
        Collections.shuffle(shuffledWatches);

        // Take first 5 watches or less if there are fewer than 5
        int limitCount = Math.min(5, shuffledWatches.size());
        List<Watch> limitedWatches = shuffledWatches.subList(0, limitCount);

        // Update adapter
        adapter.setWatches(limitedWatches);
    }

    private void sortAndDisplayWatches() {
        if (allWatches == null || allWatches.isEmpty()) {
            return;
        }

        List<Watch> sortedWatches = new ArrayList<>(allWatches);

        switch (currentSortOption) {
            case PRICE_LOW_HIGH:
                sortedWatches.sort(Comparator.comparingDouble(Watch::getPrice_Range));
                break;
            case PRICE_HIGH_LOW:
                sortedWatches.sort((w1, w2) ->
                        Double.compare(w2.getPrice_Range(), w1.getPrice_Range()));
                break;
            case BRAND_AZ:
                sortedWatches.sort((w1, w2) ->
                        w1.getBrand().compareToIgnoreCase(w2.getBrand()));
                break;
            case BRAND_ZA:
                sortedWatches.sort((w1, w2) ->
                        w2.getBrand().compareToIgnoreCase(w1.getBrand()));
                break;
        }

        // Take first 5 watches or less if there are fewer than 5
        int limitCount = Math.min(5, sortedWatches.size());
        List<Watch> limitedWatches = sortedWatches.subList(0, limitCount);

        // Update adapter
        adapter.setWatches(limitedWatches);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        getMenuInflater().inflate(R.menu.menu_sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favourites) {
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.sort_price_low_high) {
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
}