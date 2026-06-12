package com.example.pemomovie.custom;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

public class GlowTextView extends androidx.appcompat.widget.AppCompatTextView {
    public GlowTextView(Context context) {
        super(context);
        init();
    }

    public GlowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GlowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        // bật software layer để hỗ trợ blur chữ
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private int strokeColor = Color.parseColor("#1B53B1");

//    public void setStrokeColor(int color) {
//        this.strokeColor = color;
//        invalidate(); // lấy màu
//    }

    @Override
    protected void onDraw(Canvas canvas){
        Paint paint = getPaint();
        String text = getText().toString();

        paint.setTextAlign(Paint.Align.CENTER);
        float x = getWidth() / 2f;
        float y = getHeight() / 2f - ((paint.descent() + paint.ascent()) / 2);

        //stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        paint.setColor(strokeColor);
        paint.setMaskFilter(new BlurMaskFilter(15f, BlurMaskFilter.Blur.OUTER));
        canvas.drawText(text, x, y, paint);

        //chữ
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setMaskFilter(null);
        canvas.drawText(text, x, y, paint);
    }
}
