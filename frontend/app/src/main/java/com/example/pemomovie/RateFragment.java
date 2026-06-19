package com.example.pemomovie;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RateFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public RateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RateFragment newInstance(String param1, String param2) {
        RateFragment fragment = new RateFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edtRateComment = view.findViewById(R.id.edtRateComment);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        Button btnSubmitRate = view.findViewById(R.id.btnSubmitRate);
        ImageButton btnClose = view.findViewById(R.id.btnCloseRate);

        // Đóng fragment đánh giá
        btnClose.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();

            // hiển thị lại scroll view và ẩn fragment
            ScrollView svInfo = requireActivity().findViewById(R.id.svInfo);
            FrameLayout detailContainer = requireActivity().findViewById(R.id.detailFragmentContainer);


            svInfo.setVisibility(View.VISIBLE);
            detailContainer.setVisibility(View.GONE);
        });

        btnSubmitRate.setOnClickListener(v -> {
            String comment = edtRateComment.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (comment.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập bình luận!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rating == 0) {
                Toast.makeText(getContext(), "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: xử lý lưu dữ liệu (ví dụ thêm vào RecyclerView hiển thị danh sách đánh giá)
            Toast.makeText(getContext(),
                    "Đã gửi: " + rating + " sao, bình luận: " + comment,
                    Toast.LENGTH_SHORT).show();

            // Reset sau khi gửi
            edtRateComment.setText("");
            ratingBar.setRating(0);

        });
    }
}