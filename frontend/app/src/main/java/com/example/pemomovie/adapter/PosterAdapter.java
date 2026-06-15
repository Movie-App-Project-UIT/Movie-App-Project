package com.example.pemomovie.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.ui.main.DetailActivity;

import java.util.List;

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {

    private Context context;
    private List<MediaItemDto> posterList;

    public PosterAdapter(Context context, List<MediaItemDto> posterList) {
        this.context = context;
        this.posterList = posterList;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.poster_item, parent, false);
        return new PosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        MediaItemDto movie = posterList.get(position);
        
        Glide.with(context)
             .load(movie.getPosterUrl())
             .placeholder(R.drawable.yn) // fallback placeholder
             .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            // Pass movie id to DetailActivity
            intent.putExtra("MOVIE_ID", movie.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posterList == null ? 0 : posterList.size();
    }

    public static class PosterViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;

        public PosterViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
        }
    }
}
