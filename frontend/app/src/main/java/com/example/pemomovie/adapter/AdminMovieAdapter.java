package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.List;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.ViewHolder> {

    private final Context context;
    private List<MediaItemDto> movies = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MediaItemDto movie);
        void onDeleteClick(MediaItemDto movie);
    }

    public AdminMovieAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMovies(List<MediaItemDto> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItemDto movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvType.setText(movie.getMediaType() != null ? movie.getMediaType() : "Movie");
        
        if (movie.isDeleted()) {
            holder.tvStatus.setText("Deleted");
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            holder.btnDelete.setImageResource(R.drawable.ic_save); // Save/Restore icon
            holder.btnDelete.setColorFilter(Color.parseColor("#10B981")); // Green to restore
        } else {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setTextColor(Color.parseColor("#10B981")); // Green
            holder.btnDelete.setImageResource(R.drawable.ic_close);
            holder.btnDelete.setColorFilter(Color.parseColor("#EF4444")); // Red to delete
        }

        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.bg_poster_rounded)
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(movie));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(movie));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster, btnDelete;
        TextView tvTitle, tvStatus, tvType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}
