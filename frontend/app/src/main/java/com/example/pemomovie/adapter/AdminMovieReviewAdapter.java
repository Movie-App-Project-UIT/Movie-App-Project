package com.example.pemomovie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.ReviewResponseDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminMovieReviewAdapter extends RecyclerView.Adapter<AdminMovieReviewAdapter.ViewHolder> {
    private List<ReviewResponseDto> reviews = new ArrayList<>();

    public void setReviews(List<ReviewResponseDto> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_movie_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewResponseDto review = reviews.get(position);
        
        if (review.getUser() != null) {
            holder.tvUsername.setText(review.getUser().getUsername());
            Glide.with(holder.itemView.getContext())
                 .load(review.getUser().getAvatarUrl())
                 .placeholder(R.drawable.ic_profile)
                 .circleCrop()
                 .into(holder.ivAvatar);
        } else {
            holder.tvUsername.setText("Người dùng ẩn danh");
            holder.ivAvatar.setImageResource(R.drawable.ic_profile);
        }
        
        holder.tvContent.setText(review.getContent());

        try {
            if (review.getCreatedAt() != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    LocalDateTime dateTime = LocalDateTime.parse(review.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    holder.tvDate.setText(dateTime.format(formatter));
                } else {
                    holder.tvDate.setText(review.getCreatedAt());
                }
            } else {
                holder.tvDate.setText("");
            }
        } catch (Exception e) {
            holder.tvDate.setText(review.getCreatedAt());
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername, tvDate, tvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}
