package com.example.watchshop;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchshop.adapter.FavouriteWatchAdapter;

import java.util.Objects;

public class FavouritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavouriteWatchAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Initialize views
        recyclerView = findViewById(R.id.favourites_recycler_view);
        emptyView = findViewById(R.id.empty_view);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavouriteWatchAdapter(this);
        recyclerView.setAdapter(adapter);

        // Initialize repository
        FavouriteRepository repository = new FavouriteRepository(getApplication());

        // Observe favorites
        repository.getAllFavorites().observe(this, favorites -> {
            if (favorites != null) {
                adapter.setFavorites(favorites);

                if (!favorites.isEmpty()) {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
