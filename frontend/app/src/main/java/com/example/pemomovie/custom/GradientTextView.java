package com.example.pemomovie.custom;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.ViewTreeObserver;
import android.widget.TextView;

public class GradientTextView {

    // Gradient ngang với 2 màu
    public static void applyHorizontalGradient(TextView textView, int startColor, int endColor) {
        textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Gỡ listener ngay sau lần chạy đầu để tránh gọi lặp lại
                textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float width = textView.getWidth();
                if (width <= 0) return;

                Shader shader = new LinearGradient(
                        0, 0, width, 0, // chạy ngang từ trái sang phải
                        new int[]{startColor, endColor},
                        null,
                        Shader.TileMode.CLAMP
                );
                textView.getPaint().setShader(shader);
                textView.invalidate(); // Vẽ lại để áp dụng shader
            }
        });
    }

    // Gradient ngang với nhiều màu (hồng -> tím -> xanh)
    public static void applyHorizontalGradient(TextView textView, int[] colors) {
        textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float width = textView.getWidth();
                if (width <= 0) return;

                Shader shader = new LinearGradient(
                        0, 0, width, 0,
                        colors,
                        null,
                        Shader.TileMode.CLAMP
                );
                textView.getPaint().setShader(shader);
                textView.invalidate();
            }
        });
    }
}
