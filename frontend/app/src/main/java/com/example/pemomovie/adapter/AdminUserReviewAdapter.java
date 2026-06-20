package com.example.pemomovie.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pemomovie.R;
import com.example.pemomovie.dto.AdminUserDetailDto;
import java.util.ArrayList;
import java.util.List;

public class AdminUserReviewAdapter extends RecyclerView.Adapter<AdminUserReviewAdapter.ViewHolder> {
    private List<AdminUserDetailDto.ReviewDto> reviews = new ArrayList<>();

    public void setReviews(List<AdminUserDetailDto.ReviewDto> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUserDetailDto.ReviewDto review = reviews.get(position);
        holder.txtReviewDate.setText(review.getCreatedAt());
        holder.txtReviewContent.setText(review.getContent());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtReviewDate, txtReviewContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReviewDate = itemView.findViewById(R.id.txtReviewDate);
            txtReviewContent = itemView.findViewById(R.id.txtReviewContent);
        }
    }
}
