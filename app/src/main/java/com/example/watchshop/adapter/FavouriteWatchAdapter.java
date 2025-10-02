package com.example.watchshop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.watchshop.FavouriteRepository;
import com.example.watchshop.WatchDetailActivity;
import com.example.watchshop.manager.WatchManager;
import com.example.watchshop.model.FavouriteWatch;
import com.example.watchshop.R;
import com.example.watchshop.model.Watch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavouriteWatchAdapter extends RecyclerView.Adapter<FavouriteWatchAdapter.FavoriteViewHolder> {
    private List<FavouriteWatch> favorites = new ArrayList<>();
    private final Context context;

    public FavouriteWatchAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watch_favourite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavouriteWatch watch = favorites.get(position);

        holder.brandTextView.setText(watch.getBrand());
        holder.modelTextView.setText(watch.getModel());


        // Format price with currency symbol
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        holder.priceTextView.setText(formatter.format(watch.getPriceRange()));

        // Load image with Glide
        if (!TextUtils.isEmpty(watch.getImageUrl())) {
            Glide.with(context)
                    .load(watch.getImageUrl())
                    .centerCrop()
                    .into(holder.watchImageView);
        }

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            new FavouriteRepository(((AppCompatActivity) context).getApplication())
                    .deleteById(watch.getWatchId());
        });

        holder.itemView.setOnClickListener(v -> {
            navigateToDetail(watch);
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFavorites(List<FavouriteWatch> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView watchImageView;
        TextView brandTextView;
        TextView modelTextView;
        TextView priceTextView;
        ImageButton deleteButton;

        FavoriteViewHolder(View itemView) {
            super(itemView);
            watchImageView = itemView.findViewById(R.id.watch_image);
            brandTextView = itemView.findViewById(R.id.brand_text);
            modelTextView = itemView.findViewById(R.id.model_text);
            priceTextView = itemView.findViewById(R.id.price_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    private void navigateToDetail(FavouriteWatch favouriteWatch) {
        // Try to get the full watch details from WatchManager
        Watch watch = WatchManager.getInstance().getWatchById(favouriteWatch.getWatchId());

        if (watch != null) {
            // We have the full watch details - navigate to detail activity
            Intent intent = new Intent(context, WatchDetailActivity.class);
            intent.putExtra("watch", watch);
            context.startActivity(intent);
        } else {
            // We don't have the details in memory - create a minimal Watch object
            Watch minimalWatch = new Watch();
            // Assuming you have a setter for _id
            minimalWatch.set_Id(favouriteWatch.getWatchId());
            minimalWatch.setBrand(favouriteWatch.getBrand());
            minimalWatch.setModel(favouriteWatch.getModel());
            minimalWatch.setPrice_Range(favouriteWatch.getPriceRange());

            // Add image to a list if available
            if (!TextUtils.isEmpty(favouriteWatch.getImageUrl())) {
                List<String> images = new ArrayList<>();
                images.add(favouriteWatch.getImageUrl());
                minimalWatch.setImages(images);
            }

            // Navigate with minimal details
            Intent intent = new Intent(context, WatchDetailActivity.class);
            intent.putExtra("watch", minimalWatch);
            context.startActivity(intent);
        }
    }
}
