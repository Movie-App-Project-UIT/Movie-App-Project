package com.example.pemomovie.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.pemomovie.R;

public class DialogHelper {

    public interface OnConfirmListener {
        void onConfirm();
    }

    public static void showSuccessDialog(Context context, String title, String message, OnConfirmListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        ImageView icon = dialog.findViewById(R.id.dialogIcon);
        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText(title);
        tvMessage.setText(message);
        
        // Success Styling
        icon.setImageResource(R.drawable.ic_check_circle_filled);
        icon.setColorFilter(Color.parseColor("#10B981")); // Green/emerald tint
        icon.setVisibility(View.VISIBLE);

        btnCancel.setVisibility(View.GONE); // Hide cancel button for success messages
        btnConfirm.setText("OK");
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirm();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    public static void showErrorDialog(Context context, String title, String message, OnConfirmListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        ImageView icon = dialog.findViewById(R.id.dialogIcon);
        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText(title);
        tvMessage.setText(message);
        
        // Error Styling
        icon.setImageResource(R.drawable.ic_close);
        icon.setColorFilter(Color.parseColor("#EF4444")); // Red tint
        icon.setVisibility(View.VISIBLE);

        btnCancel.setVisibility(View.GONE); // Hide cancel button for error alerts
        btnConfirm.setText("OK");
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onConfirm();
            }
        });

        dialog.setCancelable(true);
        dialog.show();
    }

    public static Dialog showConfirmDialog(Context context, String title, String message, String confirmText, String cancelText, OnConfirmListener confirmListener, Runnable cancelListener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_confirm);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
        ImageView icon = dialog.findViewById(R.id.dialogIcon);
        Button btnCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnDialogConfirm);

        tvTitle.setText(title);
        tvMessage.setText(message);
        
        // Use default warning/question icon or hide it
        icon.setImageResource(R.drawable.ic_close);
        icon.setColorFilter(Color.parseColor("#EAB308")); // Yellow/warning tint
        icon.setVisibility(View.VISIBLE);

        btnConfirm.setText(confirmText != null ? confirmText : "Đồng ý");
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (confirmListener != null) {
                confirmListener.onConfirm();
            }
        });

        btnCancel.setText(cancelText != null ? cancelText : "Hủy");
        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (cancelListener != null) {
                cancelListener.run();
            }
        });

        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }
}
