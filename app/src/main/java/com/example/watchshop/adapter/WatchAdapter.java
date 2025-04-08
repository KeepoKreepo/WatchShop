package com.example.watchshop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.watchshop.R;
import com.example.watchshop.WatchDetailActivity;
import com.example.watchshop.model.Watch;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.WatchViewHolder> {
    private List<Watch> watches;
    private final Context context;

    public WatchAdapter(Context context, List<Watch> watches) {
        this.context = context;
        this.watches = watches;
    }

    @NonNull
    @Override
    public WatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watch, parent, false);
        return new WatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchViewHolder holder, int position) {
        Watch watch = watches.get(position);
        Log.d("WatchAdapter", "Pre-intent Watch ID: " + watch.get_Id());

        holder.brandTextView.setText(watch.getBrand());
        holder.modelTextView.setText(watch.getModel());

        // Format price with currency symbol
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        holder.priceTextView.setText(formatter.format(watch.getPrice_Range()));

        // Load the first image from the images list
        if (watch.getImages() != null && !watch.getImages().isEmpty()) {
            Glide.with(context)
                    .load(watch.getImages().get(0))
                    .centerCrop()
                    .into(holder.watchImageView);
        }

        // Set click listener for the card
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WatchDetailActivity.class);
            intent.putExtra("watch", watch);
            context.startActivity(intent);
        });
        Log.d("WatchDetailActivity", "Watch ID: " + watch.get_Id());
    }

    @Override
    public int getItemCount() {
        return watches.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setWatches(List<Watch> watches) {
        this.watches = watches;
        notifyDataSetChanged();
    }

    static class WatchViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView watchImageView;
        TextView brandTextView;
        TextView modelTextView;
        TextView priceTextView;

        WatchViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            watchImageView = itemView.findViewById(R.id.watch_image);
            brandTextView = itemView.findViewById(R.id.brand_text);
            modelTextView = itemView.findViewById(R.id.model_text);
            priceTextView = itemView.findViewById(R.id.price_text);
        }
    }
}