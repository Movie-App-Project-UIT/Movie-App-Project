package com.example.pemomovie.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.MediaItemDto;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<MediaItemDto> bannerMovies;

    public BannerAdapter(Context context, List<MediaItemDto> bannerMovies) {
        this.context = context;
        this.bannerMovies = bannerMovies;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        if (bannerMovies == null || bannerMovies.isEmpty()) return;
        
        int realPosition = position % bannerMovies.size();
        MediaItemDto movie = bannerMovies.get(realPosition);
        
        holder.movieTitle.setText(movie.getTitle());
        
        Glide.with(context)
             .load(movie.getPosterUrl())
             .placeholder(R.drawable.yn)
             .into(holder.bannerImage);
    }

    @Override
    public int getItemCount() {
        return bannerMovies != null && !bannerMovies.isEmpty() ? Integer.MAX_VALUE : 0;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        TextView movieTitle;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
            movieTitle = itemView.findViewById(R.id.movieTitle);
        }
    }
}
