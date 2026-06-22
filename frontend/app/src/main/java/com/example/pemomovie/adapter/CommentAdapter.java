package com.example.pemomovie.adapter;

import android.content.Context;
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

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface OnCommentActionClickListener {
        void onReplyClick(ReviewResponseDto comment);
        void onReportClick(ReviewResponseDto comment);
    }

    private final Context context;
    private final List<ReviewResponseDto> reviewList;
    private final OnCommentActionClickListener actionListener;

    public CommentAdapter(Context context, List<ReviewResponseDto> reviewList, OnCommentActionClickListener actionListener) {
        this.context = context;
        this.reviewList = reviewList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        ReviewResponseDto review = reviewList.get(position);
        
        if (review.getUser() != null) {
            holder.txtUserName.setText(review.getUser().getUsername() != null ? review.getUser().getUsername() : "User");
            if (review.getUser().getAvatarUrl() != null && !review.getUser().getAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(review.getUser().getAvatarUrl())
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(holder.imgAvatar);
            } else {
                holder.imgAvatar.setImageResource(R.drawable.ic_avatar);
            }
        } else {
            holder.txtUserName.setText("User");
            holder.imgAvatar.setImageResource(R.drawable.ic_avatar);
        }

        holder.txtComment.setText(review.getContent());

        if (review.getCreatedAt() != null) {
            try {
                // Backend returns "2024-01-01T12:00:00" string or similar
                String dateStr = review.getCreatedAt();
                if (dateStr.contains("T")) {
                    dateStr = dateStr.split("T")[0];
                }
                holder.txtDate.setText(dateStr);
            } catch (Exception e) {
                holder.txtDate.setText("");
            }
        } else {
            holder.txtDate.setText("");
        }

        // CommentAdapter now only displays root comments, so no indentation needed.
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        params.setMarginStart(0);
        holder.txtReplyingTo.setVisibility(View.GONE);
        holder.itemView.setLayoutParams(params);

        if (holder.txtReply != null) {
            int replyCount = review.getReplies() != null ? review.getReplies().size() : 0;
            if (replyCount > 0) {
                holder.txtReply.setText("Trả lời (" + replyCount + ")");
            } else {
                holder.txtReply.setText("Trả lời");
            }
            holder.txtReply.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onReplyClick(review);
            });
        }
        if (holder.txtReport != null) {
            holder.txtReport.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onReportClick(review);
            });
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtUserName, txtDate, txtComment, txtReply, txtReport, txtReplyingTo;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtComment = itemView.findViewById(R.id.txtComment);
            txtReply = itemView.findViewById(R.id.txtReply);
            txtReport = itemView.findViewById(R.id.txtReport);
            txtReplyingTo = itemView.findViewById(R.id.txtReplyingTo);
        }
    }
}
