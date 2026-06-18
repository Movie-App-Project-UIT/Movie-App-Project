package com.example.pemomovie.custom;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.widget.TextView;

public class GradientTextView {

    // Gradient ngang với 2 màu
    public static void applyHorizontalGradient(TextView textView, int startColor, int endColor) {
        textView.post(() -> {
            Shader shader = new LinearGradient(
                    0, 0, textView.getWidth(), 0, // chạy ngang từ trái sang phải
                    new int[]{startColor, endColor},
                    null,
                    Shader.TileMode.CLAMP
            );
            textView.getPaint().setShader(shader);
        });
    }

    // Gradient ngang với nhiều màu (hồng -> tím -> xanh)
    public static void applyHorizontalGradient(TextView textView, int[] colors) {
        textView.post(() -> {
            Shader shader = new LinearGradient(
                    0, 0, textView.getWidth(), 0,
                    colors,
                    null,
                    Shader.TileMode.CLAMP
            );
            textView.getPaint().setShader(shader);
        });
    }
}
