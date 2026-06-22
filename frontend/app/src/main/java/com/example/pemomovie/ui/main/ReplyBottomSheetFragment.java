package com.example.pemomovie.ui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pemomovie.R;
import com.example.pemomovie.adapter.CommentAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.ReviewRequestDto;
import com.example.pemomovie.dto.ReviewResponseDto;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReplyBottomSheetFragment extends BottomSheetDialogFragment {

    private ReviewResponseDto rootComment;
    private Long movieId;
    private ApiService apiService;
    private CommentAdapter replyAdapter;
    private List<ReviewResponseDto> flattenedReplies = new ArrayList<>();
    private Long replyingToReviewId = null;

    private RecyclerView rvReplies;
    private EditText etReplyContent;
    private ImageButton btnSendReply;
    private View includeParentComment;

    public ReplyBottomSheetFragment(ReviewResponseDto rootComment, Long movieId) {
        this.rootComment = rootComment;
        this.movieId = movieId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reply_bottom_sheet, container, false);
        apiService = ApiClient.getApiService();

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        includeParentComment = view.findViewById(R.id.includeParentComment);
        rvReplies = view.findViewById(R.id.rvReplies);
        etReplyContent = view.findViewById(R.id.etReplyContent);
        btnSendReply = view.findViewById(R.id.btnSendReply);
        ImageView ivCurrentUserAvatar = view.findViewById(R.id.ivCurrentUserAvatar);

        setupCurrentUserAvatar(ivCurrentUserAvatar);
        setupParentComment();
        setupRepliesRecyclerView();

        btnSendReply.setOnClickListener(v -> {
            String content = etReplyContent.getText().toString().trim();
            if (!content.isEmpty()) {
                postReply(content);
            }
        });

        return view;
    }

    private void setupCurrentUserAvatar(ImageView iv) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            String photoUrl = currentUser.getPhotoUrl().toString().trim();
            if (photoUrl.startsWith("\"") && photoUrl.endsWith("\"")) {
                photoUrl = photoUrl.substring(1, photoUrl.length() - 1);
            }
            Glide.with(this).load(photoUrl).placeholder(R.drawable.ic_avatar).circleCrop().into(iv);
        }
    }

    private void setupParentComment() {
        ImageView imgAvatar = includeParentComment.findViewById(R.id.imgAvatar);
        TextView txtUserName = includeParentComment.findViewById(R.id.txtUserName);
        TextView txtDate = includeParentComment.findViewById(R.id.txtDate);
        TextView txtComment = includeParentComment.findViewById(R.id.txtComment);
        TextView txtReply = includeParentComment.findViewById(R.id.txtReply);
        TextView txtReport = includeParentComment.findViewById(R.id.txtReport);

        txtUserName.setText(rootComment.getUser() != null ? rootComment.getUser().getUsername() : "User");
        txtComment.setText(rootComment.getContent());
        if (rootComment.getUser() != null && rootComment.getUser().getAvatarUrl() != null) {
            Glide.with(this).load(rootComment.getUser().getAvatarUrl()).placeholder(R.drawable.ic_avatar).circleCrop().into(imgAvatar);
        }
        if (rootComment.getCreatedAt() != null) {
            txtDate.setText(rootComment.getCreatedAt().substring(0, 10));
        }
        
        txtReply.setText("Trả lời");
        txtReply.setOnClickListener(v -> {
            replyingToReviewId = rootComment.getId();
            etReplyContent.setHint("Trả lời @" + txtUserName.getText() + "...");
            etReplyContent.requestFocus();
            showKeyboard(etReplyContent);
        });

        txtReport.setVisibility(View.GONE); // Hide report on parent in bottom sheet
    }

    private void setupRepliesRecyclerView() {
        flattenedReplies.clear();
        flattenReplies(flattenedReplies, rootComment.getReplies());

        replyAdapter = new CommentAdapter(requireContext(), flattenedReplies, new CommentAdapter.OnCommentActionClickListener() {
            @Override
            public void onReplyClick(ReviewResponseDto comment) {
                replyingToReviewId = comment.getId();
                etReplyContent.setHint("Trả lời @" + (comment.getUser() != null ? comment.getUser().getUsername() : "User") + "...");
                etReplyContent.requestFocus();
                showKeyboard(etReplyContent);
            }

            @Override
            public void onReportClick(ReviewResponseDto comment) {
                // Not implementing report in replies for simplicity, or can call activity method
                Toast.makeText(getContext(), "Tính năng báo cáo đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });

        rvReplies.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReplies.setAdapter(replyAdapter);
    }

    private void flattenReplies(List<ReviewResponseDto> dest, List<ReviewResponseDto> source) {
        if (source == null) return;
        for (ReviewResponseDto r : source) {
            dest.add(r);
            flattenReplies(dest, r.getReplies());
        }
    }

    private void postReply(String content) {
        Long parentId = replyingToReviewId != null ? replyingToReviewId : rootComment.getId();
        ReviewRequestDto req = new ReviewRequestDto(movieId, null, parentId, content);
        apiService.postReview(req).enqueue(new Callback<ReviewResponseDto>() {
            @Override
            public void onResponse(Call<ReviewResponseDto> call, Response<ReviewResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    etReplyContent.setText("");
                    etReplyContent.setHint("Thêm phản hồi...");
                    replyingToReviewId = null;
                    hideKeyboard(etReplyContent);
                    
                    // Add new reply to list and update
                    flattenedReplies.add(response.body());
                    replyAdapter.notifyDataSetChanged();
                    rvReplies.scrollToPosition(flattenedReplies.size() - 1);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi đăng phản hồi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReviewResponseDto> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
