package com.example.pemomovie.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.MediaItemDto;

import java.util.List;

public class AdminTopMovieAdapter extends RecyclerView.Adapter<AdminTopMovieAdapter.ViewHolder> {

    private Context context;
    private List<MediaItemDto> movies;
    private boolean isTrending;

    public AdminTopMovieAdapter(Context context, List<MediaItemDto> movies, boolean isTrending) {
        this.context = context;
        this.movies = movies;
        this.isTrending = isTrending;
    }

    public void updateData(List<MediaItemDto> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_top_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItemDto movie = movies.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvMovieTitle.setText(movie.getTitle());

        if (position == 0) holder.tvRank.setTextColor(Color.parseColor("#F59E0B")); // Gold
        else if (position == 1) holder.tvRank.setTextColor(Color.parseColor("#9CA3AF")); // Silver
        else holder.tvRank.setTextColor(Color.parseColor("#B45309")); // Bronze
        
        if (isTrending) {
            holder.tvStat.setText(movie.getViewCount() + " Lượt xem");
        } else {
            holder.tvStat.setText(String.format("%.1f TMDB", movie.getVoteAverage()));
        }
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvMovieTitle, tvStat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvStat = itemView.findViewById(R.id.tvStat);
        }
    }
}
