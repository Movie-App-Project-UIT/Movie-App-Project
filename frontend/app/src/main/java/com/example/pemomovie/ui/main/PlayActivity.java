package com.example.pemomovie.ui.main;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.ui.PlayerView;

import com.example.pemomovie.CommentFragment;
import com.example.pemomovie.R;
import com.example.pemomovie.RateFragment;

public class PlayActivity extends AppCompatActivity {

    private ImageButton btnFullscreen;
    private ImageButton btnSubtitle;
    private boolean subtitleEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        PlayerView playerView = findViewById(R.id.playerView);

        // Button Fullscreen
        btnFullscreen =
                playerView.findViewById(R.id.btnFullscreen);

        if (btnFullscreen != null) {

            updateFullscreenIcon();

            btnFullscreen.setOnClickListener(v -> {

                boolean isLandscape =
                        getResources().getConfiguration().orientation
                                == Configuration.ORIENTATION_LANDSCAPE;

                setRequestedOrientation(
                        isLandscape
                                ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                );
            });
        }

        //Button Subtitle
        btnSubtitle = playerView.findViewById(R.id.btnSubtitle);

        if (btnSubtitle != null) {

            updateSubtitleButton();

            btnSubtitle.setOnClickListener(v -> {
//
//                subtitleEnabled = !subtitleEnabled;
//
//                updateSubtitleButton();

                // TODO: bật/tắt phụ đề thật ở đây
            });
        }

        LinearLayout contentLayout = findViewById(R.id.svInfo);
        FrameLayout fragmentContainer = findViewById(R.id.detailFragmentContainer);

        LinearLayout commentFrame = findViewById(R.id.commentFrame);
        LinearLayout rateFrame = findViewById(R.id.ratingFrame);

        // hiển thị Fragment bình luận nếu nhấn vào commentFrame
        commentFrame.setOnClickListener(v ->{

            // ẩn thông tin phim dưới video và hiển thị fragment
            contentLayout.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_up,
                            0
                    )
                    .replace(
                            R.id.detailFragmentContainer,
                            new CommentFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

        // Tương tự với fragmant đánh giá
        rateFrame.setOnClickListener(v ->{

            // ẩn thông tin phim dưới video và hiển thị fragment
            contentLayout.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_up,
                            0
                    )
                    .replace(
                            R.id.detailFragmentContainer,
                            new RateFragment()
                    )
                    .addToBackStack(null)
                    .commit();
        });

//        Long movieId = getIntent().getLongExtra("MOVIE_ID", -1);
//        TextView tvPlayPlaceholder = findViewById(R.id.tvPlayPlaceholder);
//        if (movieId != -1) {
//            tvPlayPlaceholder.setText("Đang phát phim ID: " + movieId);
//        }
    }

    // Đổi icon theo trạng thái màn
    private void updateFullscreenIcon() {

        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {

            btnFullscreen.setImageResource(
                    R.drawable.ic_fullscreen_exit);

        } else {

            btnFullscreen.setImageResource(
                    R.drawable.ic_fullscreen);

        }
    }

    // Đổi trạng thái bật / tắt cc
    private void updateSubtitleButton() {

        if (btnSubtitle == null) return;

        btnSubtitle.setImageResource(
                subtitleEnabled
                        ? R.drawable.ic_subtitle
                        : R.drawable.ic_subtitle_1
        );
    }
}
