package com.example.pemomovie;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class CommentFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public CommentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }

    private com.example.pemomovie.adapter.CommentAdapter commentAdapter;
    private java.util.List<com.example.pemomovie.dto.ReviewResponseDto> reviewList = new java.util.ArrayList<>();
    private Long mediaId = -1L;
    private Long replyingToReviewId = null;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            mediaId = getArguments().getLong("MEDIA_ID", -1L);
        }

        RecyclerView rvComment = view.findViewById(R.id.rvComment);
        rvComment.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        android.widget.EditText etComment = view.findViewById(R.id.etComment);
        
        ImageButton btnCancelReply = view.findViewById(R.id.btnCancelReply);

        commentAdapter = new com.example.pemomovie.adapter.CommentAdapter(requireContext(), reviewList, new com.example.pemomovie.adapter.CommentAdapter.OnCommentActionClickListener() {
            @Override
            public void onReplyClick(com.example.pemomovie.dto.ReviewResponseDto comment) {
                replyingToReviewId = comment.getId();
                etComment.setHint("Trả lời @" + (comment.getUser() != null ? comment.getUser().getUsername() : "User") + "...");
                btnCancelReply.setVisibility(View.VISIBLE);
                etComment.requestFocus();
                // Show keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }

            @Override
            public void onReportClick(com.example.pemomovie.dto.ReviewResponseDto comment) {
                showReportDialog(comment.getId());
            }
        });
        rvComment.setAdapter(commentAdapter);

        ImageButton btnClose = view.findViewById(R.id.btnCloseComment);
        btnClose.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        if (mediaId != -1L) {
            loadComments();
        }

        ImageButton btnSendComment = view.findViewById(R.id.btnSendComment);

        btnCancelReply.setOnClickListener(v -> {
            replyingToReviewId = null;
            etComment.setHint("Nêu cảm nhận của bạn");
            etComment.setText("");
            btnCancelReply.setVisibility(View.GONE);
        });

        btnSendComment.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!content.isEmpty() && mediaId != -1L) {
                postComment(content, etComment);
            }
        });
    }
    
    private void showReportDialog(Long reviewId) {
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_report_comment);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        android.widget.EditText etReportReason = dialog.findViewById(R.id.etReportReason);
        android.widget.Button btnCancel = dialog.findViewById(R.id.btnCancelReport);
        android.widget.Button btnSubmit = dialog.findViewById(R.id.btnSubmitReport);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String reason = etReportReason.getText().toString().trim();
            java.util.Map<String, String> payload = new java.util.HashMap<>();
            payload.put("reason", reason);
            com.example.pemomovie.api.ApiClient.getApiService().reportReview(reviewId, payload).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        android.widget.Toast.makeText(requireContext(), "Báo cáo thành công", android.widget.Toast.LENGTH_SHORT).show();
                        java.util.Set<Long> idsToRemove = new java.util.HashSet<>();
                        idsToRemove.add(reviewId);
                        boolean added;
                        do {
                            added = false;
                            for (com.example.pemomovie.dto.ReviewResponseDto r : reviewList) {
                                if (!idsToRemove.contains(r.getId()) && r.getParentId() != null && idsToRemove.contains(r.getParentId())) {
                                    idsToRemove.add(r.getId());
                                    added = true;
                                }
                            }
                        } while (added);

                        java.util.Iterator<com.example.pemomovie.dto.ReviewResponseDto> iterator = reviewList.iterator();
                        while (iterator.hasNext()) {
                            if (idsToRemove.contains(iterator.next().getId())) {
                                iterator.remove();
                            }
                        }
                        commentAdapter.notifyDataSetChanged();
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Bạn đã báo cáo hoặc có lỗi xảy ra", android.widget.Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    android.widget.Toast.makeText(requireContext(), "Lỗi kết nối", android.widget.Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }

    private void flattenReplies(java.util.List<com.example.pemomovie.dto.ReviewResponseDto> targetList, java.util.List<com.example.pemomovie.dto.ReviewResponseDto> replies) {
        if (replies == null) return;
        for (com.example.pemomovie.dto.ReviewResponseDto r : replies) {
            targetList.add(r);
            flattenReplies(targetList, r.getReplies());
        }
    }

    private void loadComments() {
        com.example.pemomovie.api.ApiClient.getApiService().getReviews(mediaId)
            .enqueue(new retrofit2.Callback<java.util.List<com.example.pemomovie.dto.ReviewResponseDto>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.List<com.example.pemomovie.dto.ReviewResponseDto>> call, retrofit2.Response<java.util.List<com.example.pemomovie.dto.ReviewResponseDto>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        reviewList.clear();
                        for (com.example.pemomovie.dto.ReviewResponseDto root : response.body()) {
                            reviewList.add(root);
                            flattenReplies(reviewList, root.getReplies());
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<java.util.List<com.example.pemomovie.dto.ReviewResponseDto>> call, Throwable t) {}
            });
    }

    private void postComment(String content, android.widget.EditText etComment) {
        com.example.pemomovie.dto.ReviewRequestDto req = new com.example.pemomovie.dto.ReviewRequestDto(mediaId, null, replyingToReviewId, content);
        com.example.pemomovie.api.ApiClient.getApiService().postReview(req)
            .enqueue(new retrofit2.Callback<com.example.pemomovie.dto.ReviewResponseDto>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.pemomovie.dto.ReviewResponseDto> call, retrofit2.Response<com.example.pemomovie.dto.ReviewResponseDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        etComment.setText("");
                        etComment.setHint("Nêu cảm nhận của bạn");
                        replyingToReviewId = null;
                        if (getView() != null) {
                            ImageButton btnCancelReply = getView().findViewById(R.id.btnCancelReply);
                            if (btnCancelReply != null) {
                                btnCancelReply.setVisibility(View.GONE);
                            }
                        }
                        
                        // Để đơn giản, reload lại toàn bộ bình luận để đúng thứ tự
                        loadComments();
                        
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                        }
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Lỗi khi đăng bình luận", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.example.pemomovie.dto.ReviewResponseDto> call, Throwable t) {}
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            ScrollView svInfo = getActivity().findViewById(R.id.svInfo);
            if (svInfo != null) svInfo.setVisibility(View.VISIBLE);
        }
    }
}