package com.example.pemomovie.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.ui.main.DetailActivity;
import com.example.pemomovie.utils.FavoriteManager;

import java.util.List;

public class SeeAllAdapter extends RecyclerView.Adapter<SeeAllAdapter.SeeAllViewHolder> {

    private List<MediaItemDto> movieList;
    private Context context;

    public SeeAllAdapter(Context context, List<MediaItemDto> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public SeeAllViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.poster_detail_item, parent, false);
        return new SeeAllViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeeAllViewHolder holder, int position) {
        MediaItemDto movie = movieList.get(position);

        // Tên phim
        holder.txtMovieName.setText(movie.getTitle() != null ? movie.getTitle() : "Đang cập nhật");

        // Điểm đánh giá
        if (holder.txtRate != null) {
            holder.txtRate.setText(movie.getVoteAverage() != null
                    ? String.format("%.1f", movie.getVoteAverage())
                    : "N/A");
        }

        // Số tập / loại phim
        if (holder.txtEpisode != null) {
            if ("TV_SHOW".equals(movie.getMediaType())) {
                holder.txtEpisode.setText("Phim bộ");
            } else {
                holder.txtEpisode.setText("Full");
            }
        }

        // Ảnh poster
        String imageUrl = movie.getPosterUrl() != null ? movie.getPosterUrl() : movie.getBackdropUrl();
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.yn)
                .centerCrop()
                .into(holder.imgPoster);

        // Trạng thái nút yêu thích
        updateFavoriteButton(holder.btnFavorite, movie.getId());

        // Toggle yêu thích
        holder.btnFavorite.setOnClickListener(v -> {
            boolean isAdded = FavoriteManager.toggleFavorite(context, movie);
            if (isAdded) {
                holder.btnFavorite.setImageResource(R.drawable.ic_heart);
                holder.btnFavorite.setColorFilter(Color.parseColor("#FF1493"));
                Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                holder.btnFavorite.setImageResource(R.drawable.ic_heart);
                holder.btnFavorite.setColorFilter(Color.parseColor("#AAAAAA"));
                Toast.makeText(context, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            }
        });

        // Click vào thẻ → sang DetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("MOVIE_ID", movie.getId());
            context.startActivity(intent);
        });
    }

    private void updateFavoriteButton(ImageButton btn, Long movieId) {
        if (FavoriteManager.isFavorite(context, movieId)) {
            btn.setImageResource(R.drawable.ic_heart);
            btn.setColorFilter(Color.parseColor("#FF1493"));
        } else {
            btn.setImageResource(R.drawable.ic_heart);
            btn.setColorFilter(Color.parseColor("#AAAAAA"));
        }
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public void updateData(List<MediaItemDto> newList) {
        this.movieList = newList;
        notifyDataSetChanged();
    }

    public static class SeeAllViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtMovieName, txtRate, txtEpisode;
        ImageButton btnFavorite;

        public SeeAllViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtMovieName = itemView.findViewById(R.id.txtMovieName);
            txtRate = itemView.findViewById(R.id.txtRate);
            txtEpisode = itemView.findViewById(R.id.txtEpisode);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
