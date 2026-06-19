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

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.MediaDetailResponse;
import com.example.pemomovie.dto.SubtitleDto;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.pemomovie.CommentFragment;
import com.example.pemomovie.R;
import com.example.pemomovie.RateFragment;

public class PlayActivity extends AppCompatActivity {

    private ImageButton btnFullscreen;
    private ImageButton btnSubtitle;
    private boolean subtitleEnabled = false;

    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private ApiService apiService;
    private Long movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        playerView = findViewById(R.id.playerView);
        apiService = ApiClient.getClient().create(ApiService.class);

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
                subtitleEnabled = !subtitleEnabled;
                toggleSubtitle();
                updateSubtitleButton();
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

        movieId = getIntent().getLongExtra("MOVIE_ID", -1L);
        if (movieId != -1L) {
            fetchVideoAndPlay();
        } else {
            Toast.makeText(this, "Không tìm thấy ID phim!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchVideoAndPlay() {
        // Lấy đường dẫn video
        apiService.getPlayableVideoUrl(movieId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String videoUrl = response.body().string().trim();
                        fetchSubtitlesAndSetupPlayer(videoUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(PlayActivity.this, "Lỗi đọc đường dẫn video", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PlayActivity.this, "Không thể lấy link video (có thể do chưa có VIP)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("PlayActivity", "Lỗi lấy video: " + t.getMessage());
                Toast.makeText(PlayActivity.this, "Lỗi kết nối khi lấy video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSubtitlesAndSetupPlayer(String videoUrl) {
        // Lấy chi tiết phim để lấy phụ đề
        apiService.getMediaDetail(movieId).enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SubtitleDto> subtitles = response.body().getSubtitles();
                    setupExoPlayer(videoUrl, subtitles);
                } else {
                    // Nếu lỗi lấy chi tiết, vẫn phát video không có phụ đề
                    setupExoPlayer(videoUrl, null);
                }
            }

            @Override
            public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                Log.e("PlayActivity", "Lỗi lấy chi tiết phim: " + t.getMessage());
                setupExoPlayer(videoUrl, null);
            }
        });
    }

    private void setupExoPlayer(String videoUrl, List<SubtitleDto> subtitles) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(exoPlayer);
        }

        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
                .setUri(Uri.parse(videoUrl));

        // Thiết lập phụ đề nếu có
        if (subtitles != null && !subtitles.isEmpty()) {
            ImmutableList.Builder<MediaItem.SubtitleConfiguration> subtitleConfigs = ImmutableList.builder();
            for (SubtitleDto sub : subtitles) {
                String subUrl = sub.getFileUrl();
                if (subUrl != null && !subUrl.trim().isEmpty()) {
                    // Định dạng mimeType dựa theo đuôi file
                    String mimeType = subUrl.toLowerCase().endsWith(".srt") ? MimeTypes.APPLICATION_SUBRIP : MimeTypes.TEXT_VTT;
                    MediaItem.SubtitleConfiguration config = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subUrl))
                            .setMimeType(mimeType)
                            .setLanguage(sub.getLanguage() != null ? sub.getLanguage() : "vi")
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build();
                    subtitleConfigs.add(config);
                }
            }
            mediaItemBuilder.setSubtitleConfigurations(subtitleConfigs.build());
        }

        exoPlayer.setMediaItem(mediaItemBuilder.build());
        exoPlayer.prepare();
        exoPlayer.play();

        toggleSubtitle(); // Cập nhật trạng thái text track hiện tại
    }

    private void toggleSubtitle() {
        if (exoPlayer == null) return;
        exoPlayer.setTrackSelectionParameters(
                exoPlayer.getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !subtitleEnabled)
                        .build()
        );
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
