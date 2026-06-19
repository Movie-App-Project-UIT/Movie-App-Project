package com.example.pemomovie.adapter;

import android.content.Context;
import android.content.Intent;
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

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<MediaItemDto> favoriteList;
    private Context context;

    public FavoriteAdapter(List<MediaItemDto> favoriteList) {
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        // Trỏ tới file layout do bạn của bạn tạo
        View view = LayoutInflater.from(context).inflate(R.layout.poster_detail_item, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        MediaItemDto movie = favoriteList.get(position);

        // Hiển thị tên phim
        holder.txtTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Đang cập nhật");

        // Gán số tập tạm thời
        holder.txtEpisode.setText("Full");

        // Gán điểm đánh giá
        if (holder.txtRate != null) {
            holder.txtRate.setText(movie.getVoteAverage() != null ? String.valueOf(movie.getVoteAverage()) : "N/A");
        }

        // Ép màu nút tym thành màu hồng rực rỡ do ở màn hình yêu thích thì tất cả đều là đã tym
        holder.btnFavorite.setColorFilter(android.graphics.Color.parseColor("#FF1493"));
        holder.btnFavorite.setImageResource(R.drawable.ic_heart);

        // Hiển thị ảnh Poster dùng Glide
        String imageUrl = movie.getPosterUrl() != null ? movie.getPosterUrl() : movie.getBackdropUrl();
        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_avatar)
                .centerCrop()
                .into(holder.imgPoster);

        // Logic Nút Tym (Bỏ yêu thích)
        holder.btnFavorite.setOnClickListener(v -> {
            // Xóa khỏi bộ nhớ SharedPreferences
            FavoriteManager.toggleFavorite(context, movie);
            
            // Lập tức xóa item này khỏi danh sách Adapter để mượt mà nhất
            favoriteList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, favoriteList.size());
            
            // Cập nhật lại TextView đếm số lượng phim ở màn hình FavoriteActivity
            if (context instanceof android.app.Activity) {
                TextView tvCount = ((android.app.Activity) context).findViewById(R.id.tvCountMovieFav);
                if (tvCount != null) {
                    tvCount.setText(favoriteList.size() + " phim");
                }
            }
            
            Toast.makeText(context, "Đã bỏ yêu thích phim: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Bấm vào Thẻ phim để chuyển qua màn hình Chi tiết (DetailActivity)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("MOVIE_ID", movie.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList != null ? favoriteList.size() : 0;
    }

    // Hàm dùng để cập nhật Data khi Activity gọi lại
    public void updateData(List<MediaItemDto> newFavorites) {
        this.favoriteList = newFavorites;
        notifyDataSetChanged();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle, txtEpisode, txtRate;
        ImageButton btnFavorite;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtMovieName); // Thay vì txtTitle, layout mới dùng txtMovieName
            txtEpisode = itemView.findViewById(R.id.txtEpisode);
            txtRate = itemView.findViewById(R.id.txtRate);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
