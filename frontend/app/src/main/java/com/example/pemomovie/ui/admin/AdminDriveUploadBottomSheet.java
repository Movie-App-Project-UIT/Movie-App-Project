package com.example.pemomovie.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pemomovie.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AdminDriveUploadBottomSheet extends BottomSheetDialogFragment {

    private final String title;
    private final String subtitle;
    private final OnLinkSubmittedListener listener;

    public interface OnLinkSubmittedListener {
        void onLinkSubmitted(String link);
    }

    public AdminDriveUploadBottomSheet(String title, String subtitle, OnLinkSubmittedListener listener) {
        this.title = title;
        this.subtitle = subtitle;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_drive_link, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvSubtitle = view.findViewById(R.id.tvSubtitle);
        EditText etDriveLink = view.findViewById(R.id.etDriveLink);
        View btnSubmit = view.findViewById(R.id.btnSubmit);

        if (title != null) tvTitle.setText(title);
        if (subtitle != null) tvSubtitle.setText(subtitle);

        btnSubmit.setOnClickListener(v -> {
            String link = etDriveLink.getText().toString().trim();
            if (!link.isEmpty()) {
                // Auto parsing or checking logic could be added here if needed
                if (listener != null) {
                    listener.onLinkSubmitted(link);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập link", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
