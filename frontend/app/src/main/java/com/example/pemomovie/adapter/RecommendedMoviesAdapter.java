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
import com.example.pemomovie.ui.main.PlayActivity;
import com.example.pemomovie.utils.FavoriteManager;

import java.util.List;

public class RecommendedMoviesAdapter extends RecyclerView.Adapter<RecommendedMoviesAdapter.ViewHolder> {

    private List<MediaItemDto> movieList;
    private Context context;

    public RecommendedMoviesAdapter(List<MediaItemDto> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.poster_genre_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItemDto movie = movieList.get(position);

        // Tên phim
        if (holder.txtTitle != null) {
            holder.txtTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Đang cập nhật");
        }

        // Lượt xem
        if (holder.txtViewCount != null) {
            String views = movie.getViewCount() != null ? movie.getViewCount() + " lượt xem" : "";
            holder.txtViewCount.setText(views);
        }

        // Loại phim (MOVIE / TV_SHOW)
        if (holder.txtEpisodeCount != null) {
            if ("TV_SHOW".equals(movie.getMediaType())) {
                holder.txtEpisodeCount.setText("Phim bộ");
            } else {
                holder.txtEpisodeCount.setText("Phim lẻ");
            }
        }

        // Ảnh poster
        String imageUrl = movie.getPosterUrl() != null ? movie.getPosterUrl() : movie.getBackdropUrl();
        if (holder.imgPoster != null) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_avatar)
                    .centerCrop()
                    .into(holder.imgPoster);
        }

        // Nút yêu thích: set trạng thái ban đầu
        if (holder.btnFavorite != null) {
            updateFavoriteButton(holder.btnFavorite, movie.getId());
            holder.btnFavorite.setOnClickListener(v -> {
                boolean isAdded = FavoriteManager.toggleFavorite(context, movie);
                if (isAdded) {
                    holder.btnFavorite.setImageResource(R.drawable.ic_heart);
                    holder.btnFavorite.setColorFilter(Color.parseColor("#FF1493"));
                    Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                } else {
                    holder.btnFavorite.setImageResource(R.drawable.ic_favorites);
                    holder.btnFavorite.setColorFilter(null);
                    Toast.makeText(context, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Click vào item để phát phim đó
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayActivity.class);
            intent.putExtra("MOVIE_ID", movie.getId());
            context.startActivity(intent);
        });
    }

    private void updateFavoriteButton(ImageButton btn, Long movieId) {
        if (FavoriteManager.isFavorite(context, movieId)) {
            btn.setImageResource(R.drawable.ic_heart);
            btn.setColorFilter(Color.parseColor("#FF1493"));
        } else {
            btn.setImageResource(R.drawable.ic_favorites);
            btn.setColorFilter(null);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle, txtViewCount, txtEpisodeCount;
        ImageButton btnFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgMoviePoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtViewCount = itemView.findViewById(R.id.txtViewCount);
            txtEpisodeCount = itemView.findViewById(R.id.txtEpisodeCount);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
