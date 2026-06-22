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
import android.widget.Button;
import android.widget.Toast;
import com.example.pemomovie.api.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        if (review.getReportCount() > 0) {
            holder.tvReportCount.setVisibility(View.VISIBLE);
            holder.tvReportCount.setText("Bị báo cáo: " + review.getReportCount() + " lần");
        } else {
            holder.tvReportCount.setVisibility(View.GONE);
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (review.getParentId() != null) {
            params.setMarginStart(120); // indent 120 pixels for replies
        } else {
            params.setMarginStart(0);
        }
        holder.itemView.setLayoutParams(params);

        holder.btnDelete.setOnClickListener(v -> {
            android.app.Dialog dialog = new android.app.Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog_confirm_delete);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            android.widget.Button btnCancelDelete = dialog.findViewById(R.id.btnCancelDelete);
            android.widget.Button btnConfirmDelete = dialog.findViewById(R.id.btnConfirmDelete);

            btnCancelDelete.setOnClickListener(v1 -> dialog.dismiss());
            btnConfirmDelete.setOnClickListener(v1 -> {
                ApiClient.getApiService().deleteReviewAdmin(review.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            java.util.Set<Long> idsToRemove = new java.util.HashSet<>();
                            idsToRemove.add(review.getId());
                            boolean added;
                            do {
                                added = false;
                                for (ReviewResponseDto r : reviews) {
                                    if (!idsToRemove.contains(r.getId()) && r.getParentId() != null && idsToRemove.contains(r.getParentId())) {
                                        idsToRemove.add(r.getId());
                                        added = true;
                                    }
                                }
                            } while (added);

                            java.util.Iterator<ReviewResponseDto> iterator = reviews.iterator();
                            while (iterator.hasNext()) {
                                if (idsToRemove.contains(iterator.next().getId())) {
                                    iterator.remove();
                                }
                            }
                            notifyDataSetChanged();
                            Toast.makeText(v.getContext(), "Đã xóa bình luận", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(v.getContext(), "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(v.getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            });
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername, tvDate, tvContent, tvReportCount;
        android.widget.ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvReportCount = itemView.findViewById(R.id.tvReportCount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
