package com.example.pemomovie.ui.main;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.pemomovie.adapter.RecommendedMoviesAdapter;
import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.MediaDetailResponse;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.dto.SubtitleDto;
import com.example.pemomovie.utils.FavoriteManager;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.pemomovie.CommentFragment;
import com.example.pemomovie.R;
import com.example.pemomovie.RateFragment;

@OptIn(markerClass = UnstableApi.class)
public class PlayActivity extends AppCompatActivity {

    private ImageButton btnFullscreen;
    private ImageButton btnSubtitle;
    private boolean subtitleEnabled = false;

    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private ApiService apiService;
    private Long movieId;
    private MediaDetailResponse mediaDetail; // Lưu trữ thông tin phim để khôi phục dữ liệu khi xoay màn hình
    private LinearLayout topInfo;

    // Play/Pause button references (custom controller)
    private View btnPlayCustom;
    private View btnPauseCustom;

    // Recommended movies
    private RecyclerView rvListMovies;
    private RecommendedMoviesAdapter recommendedAdapter;

    // Favorite button
    private ImageButton btnFavorite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Khởi tạo các thành phần giao diện theo hướng màn hình hiện tại
        initViews(getResources().getConfiguration().orientation);

        movieId = getIntent().getLongExtra("MOVIE_ID", -1L);
        if (movieId != -1L) {
            fetchVideoAndPlay();
        } else {
            Toast.makeText(this, "Không tìm thấy ID phim!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchVideoAndPlay() {
        // Lấy đường dẫn video phát được từ API
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
                    Toast.makeText(PlayActivity.this, "Không thể lấy link video (có thể do chưa có VIP)",
                            Toast.LENGTH_SHORT).show();
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
        // Lấy thông tin chi tiết phim để trích xuất phụ đề
        apiService.getMediaDetail(movieId).enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mediaDetail = response.body(); // Lưu trữ thông tin chi tiết phim
                    populateMovieDetails(); // Đổ dữ liệu phim lên các View
                    List<SubtitleDto> subtitles = mediaDetail.getSubtitles();
                    setupExoPlayer(videoUrl, subtitles);
                    // Setup nút yêu thích sau khi có dữ liệu phim
                    setupFavoriteButton();
                    // Tải danh sách phim đề xuất sau khi có thông tin phim
                    fetchRecommendedMovies();
                } else {
                    // Nếu lỗi lấy chi tiết, vẫn phát video không có phụ đề
                    setupExoPlayer(videoUrl, null);
                    fetchRecommendedMovies();
                }
            }

            @Override
            public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                Log.e("PlayActivity", "Lỗi lấy chi tiết phim: " + t.getMessage());
                setupExoPlayer(videoUrl, null);
                fetchRecommendedMovies();
            }
        });
    }

    /**
     * Tải danh sách phim đề xuất từ homepage data
     */
    private void fetchRecommendedMovies() {
        apiService.getHomepageData().enqueue(new Callback<Map<String, List<MediaItemDto>>>() {
            @Override
            public void onResponse(Call<Map<String, List<MediaItemDto>>> call,
                                   Response<Map<String, List<MediaItemDto>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, List<MediaItemDto>> data = response.body();
                    // Lấy bất kỳ section nào có phim, ưu tiên "popular" hoặc lấy section đầu tiên
                    List<MediaItemDto> movies = null;
                    if (data.containsKey("popular")) {
                        movies = data.get("popular");
                    } else if (!data.isEmpty()) {
                        movies = data.values().iterator().next();
                    }

                    if (movies != null && !movies.isEmpty()) {
                        // Loại bỏ phim đang xem khỏi danh sách đề xuất
                        final List<MediaItemDto> filtered = new java.util.ArrayList<>();
                        for (MediaItemDto m : movies) {
                            if (!m.getId().equals(movieId)) {
                                filtered.add(m);
                            }
                        }
                        final List<MediaItemDto> finalMovies = filtered;
                        runOnUiThread(() -> {
                            if (recommendedAdapter != null) {
                                recommendedAdapter.updateData(finalMovies);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<MediaItemDto>>> call, Throwable t) {
                Log.e("PlayActivity", "Lỗi tải phim đề xuất: " + t.getMessage());
            }
        });
    }

    private void setupExoPlayer(String videoUrl, List<SubtitleDto> subtitles) {
        if (exoPlayer == null) {
            // Cấu hình tua lùi 10s và tua tiến 10s mặc định bằng setSeekBackIncrementMs/setSeekForwardIncrementMs
            exoPlayer = new ExoPlayer.Builder(this)
                    .setSeekBackIncrementMs(10000)
                    .setSeekForwardIncrementMs(10000)
                    .build();
            // Thiết lập mặc định ban đầu là tắt phụ đề (không hiện phụ đề khi vừa tải video)
            exoPlayer.setTrackSelectionParameters(
                    exoPlayer.getTrackSelectionParameters()
                            .buildUpon()
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                            .build());
            if (playerView != null) {
                playerView.setPlayer(exoPlayer);
            }

            // Lắng nghe trạng thái play/pause để cập nhật icon nút
            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    updatePlayPauseButtons(isPlaying);
                }
            });
        }

        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
                .setUri(Uri.parse(videoUrl));

        // Thiết lập cấu hình phụ đề nếu có phụ đề đính kèm
        if (subtitles != null && !subtitles.isEmpty()) {
            ImmutableList.Builder<MediaItem.SubtitleConfiguration> subtitleConfigs = ImmutableList.builder();
            for (SubtitleDto sub : subtitles) {
                String subUrl = sub.getFileUrl();
                if (subUrl != null && !subUrl.trim().isEmpty()) {
                    // Xác định định dạng phụ đề dựa vào đuôi file (.srt hoặc .vtt)
                    String mimeType = subUrl.toLowerCase().endsWith(".srt") ? MimeTypes.APPLICATION_SUBRIP
                            : MimeTypes.TEXT_VTT;
                    MediaItem.SubtitleConfiguration config = new MediaItem.SubtitleConfiguration.Builder(
                            Uri.parse(subUrl))
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

        toggleSubtitle(); // Cập nhật trạng thái bật/tắt phụ đề hiện tại

        // Điều chỉnh vị trí phụ đề ban đầu tương thích với thanh điều khiển
        if (playerView != null) {
            adjustSubtitleViewPosition(playerView.isControllerFullyVisible());
        }
    }

    private void toggleSubtitle() {
        if (exoPlayer == null)
            return;
        // Bật hoặc tắt phụ đề của ExoPlayer dựa trên giá trị biến subtitleEnabled
        exoPlayer.setTrackSelectionParameters(
                exoPlayer.getTrackSelectionParameters()
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !subtitleEnabled)
                        .build());
        
        // Ẩn/hiện SubtitleView tương ứng để chắc chắn phụ đề không hiển thị trên màn hình khi tắt
        if (playerView != null && playerView.getSubtitleView() != null) {
            playerView.getSubtitleView().setVisibility(subtitleEnabled ? View.VISIBLE : View.GONE);
        }
    }

    // Đổi icon full screen theo trạng thái xoay của màn hình
    private void updateFullscreenIcon() {
        if (btnFullscreen == null) return;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen);
        }
    }

    // Cập nhật biểu tượng nút Subtitle bật/tắt (CC)
    private void updateSubtitleButton() {
        if (btnSubtitle == null)
            return;
        btnSubtitle.setImageResource(
                subtitleEnabled
                        ? R.drawable.ic_subtitle
                        : R.drawable.ic_subtitle_1);
    }

    /**
     * Cập nhật trạng thái hiển thị nút Play / Pause trên custom controller.
     * Hiển thị ic_pause khi đang phát, ic_play_ui khi đang dừng.
     */
    private void updatePlayPauseButtons(boolean isPlaying) {
        if (btnPlayCustom != null) {
            btnPlayCustom.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
        }
        if (btnPauseCustom != null) {
            btnPauseCustom.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Cập nhật thông tin phim lên giao diện tương ứng của từng orientation
     */
    private void populateMovieDetails() {
        if (mediaDetail == null) return;

        // Thiết lập thông tin phim cho Portrait layout
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText(mediaDetail.getTitle());
        }
        TextView tvViews = findViewById(R.id.tvViews);
        if (tvViews != null) {
            tvViews.setText(mediaDetail.getViewCount() + " lượt xem");
        }
        TextView tvTime = findViewById(R.id.tvTime);
        if (tvTime != null) {
            tvTime.setText(String.valueOf(mediaDetail.getReleaseYear()));
        }

        // Thiết lập thông tin phim cho Landscape layout
        TextView nameMovie = findViewById(R.id.nameMovie);
        if (nameMovie != null) {
            nameMovie.setText(mediaDetail.getTitle());
        }
        TextView tvCountry = findViewById(R.id.tvCountry);
        if (tvCountry != null) {
            tvCountry.setText(mediaDetail.getCountryName());
        }
    }

    /**
     * Setup nút yêu thích dựa theo dữ liệu mediaDetail đã tải
     */
    private void setupFavoriteButton() {
        if (btnFavorite == null || mediaDetail == null) return;

        // Tạo MediaItemDto từ mediaDetail để lưu vào FavoriteManager
        MediaItemDto currentMovie = new MediaItemDto();
        currentMovie.setId(mediaDetail.getId());
        currentMovie.setTitle(mediaDetail.getTitle());
        currentMovie.setPosterUrl(mediaDetail.getPosterUrl());
        currentMovie.setBackdropUrl(mediaDetail.getBackdropUrl());
        currentMovie.setVoteAverage(mediaDetail.getVoteAverage());
        currentMovie.setMediaType(mediaDetail.getMediaType());

        // Hiển thị trạng thái ban đầu
        updateFavoriteIcon();

        // Gán sự kiện click
        btnFavorite.setOnClickListener(v -> {
            boolean isAdded = FavoriteManager.toggleFavorite(this, currentMovie);
            if (isAdded) {
                btnFavorite.setImageResource(R.drawable.ic_heart);
                btnFavorite.setColorFilter(Color.parseColor("#FF1493"));
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorites);
                btnFavorite.setColorFilter(null);
                Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật icon yêu thích theo trạng thái hiện tại
     */
    private void updateFavoriteIcon() {
        if (btnFavorite == null || mediaDetail == null) return;
        if (FavoriteManager.isFavorite(this, mediaDetail.getId())) {
            btnFavorite.setImageResource(R.drawable.ic_heart);
            btnFavorite.setColorFilter(Color.parseColor("#FF1493"));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorites);
            btnFavorite.setColorFilter(null);
        }
    }

    /**
     * Đẩy SubtitleView lên trên thanh điều khiển khi thanh điều khiển đang hiện.
     * @param isControlVisible Trạng thái hiển thị của thanh điều khiển
     */
    private void adjustSubtitleViewPosition(boolean isControlVisible) {
        if (playerView == null) return;
        View subtitleView = playerView.getSubtitleView();
        if (subtitleView != null) {
            ViewGroup.LayoutParams params = subtitleView.getLayoutParams();
            if (params instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) params;
                // Nếu thanh điều khiển hiện, đẩy lên 80dp. Ngược lại chỉ đặt lề dưới là 16dp.
                int bottomMarginDp = isControlVisible ? 80 : 16;
                frameParams.bottomMargin = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        bottomMarginDp,
                        getResources().getDisplayMetrics()
                );
                subtitleView.setLayoutParams(frameParams);
            }
        }
    }

    /**
     * Khởi tạo giao diện, ánh xạ các view và gán sự kiện tương tác theo hướng màn hình.
     * @param orientation Hướng màn hình hiện tại
     */
    private void initViews(int orientation) {
        // Thiết lập chế độ Fullscreen ẩn hoàn toàn thanh hệ thống ở chế độ ngang (Landscape) để chỉ hiển thị ExoPlayer
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindow().setDecorFitsSystemWindows(false);
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindow().setDecorFitsSystemWindows(true);
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                }
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
        }


        playerView = findViewById(R.id.playerView);
        if (playerView != null && exoPlayer != null) {
            playerView.setPlayer(exoPlayer);
        }

        // Tên phim nằm ở màn hình ngang đồng bộ với controller của Exo
        if (playerView != null) {
            topInfo = playerView.findViewById(R.id.topInfo);
        }
        if (topInfo != null) {
            topInfo.setVisibility(
                    playerView.isControllerFullyVisible()
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        if (playerView != null) {
            // Đồng bộ trạng thái phụ đề (bật/tắt và hiển thị) của SubtitleView khi khởi tạo hoặc xoay màn hình
            toggleSubtitle();

            // Gán sự kiện click tường minh cho nút Play (Phát) ở giữa video
            btnPlayCustom = playerView.findViewById(androidx.media3.ui.R.id.exo_play);
            if (btnPlayCustom != null) {
                btnPlayCustom.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        exoPlayer.play();
                        updatePlayPauseButtons(true);
                    }
                });
            }

            // Gán sự kiện click tường minh cho nút Pause (Tạm dừng) ở giữa video
            btnPauseCustom = playerView.findViewById(androidx.media3.ui.R.id.exo_pause);
            if (btnPauseCustom != null) {
                btnPauseCustom.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        exoPlayer.pause();
                        updatePlayPauseButtons(false);
                    }
                });
            }

            // Cập nhật trạng thái play/pause theo ExoPlayer hiện tại
            if (exoPlayer != null) {
                updatePlayPauseButtons(exoPlayer.isPlaying());
            } else {
                // Mặc định: hiện nút Play, ẩn nút Pause
                updatePlayPauseButtons(false);
            }

            // Gán sự kiện click tường minh cho nút lùi video 10s
            View btnRew = playerView.findViewById(androidx.media3.ui.R.id.exo_rew);
            if (btnRew != null) {
                btnRew.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        long currentPos = exoPlayer.getCurrentPosition();
                        long newPos = Math.max(0, currentPos - 10000);
                        exoPlayer.seekTo(newPos);
                    }
                });
            }

            // Gán sự kiện click tường minh cho nút tăng video 10s
            View btnFfwd = playerView.findViewById(androidx.media3.ui.R.id.exo_ffwd);
            if (btnFfwd != null) {
                btnFfwd.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        long currentPos = exoPlayer.getCurrentPosition();
                        long duration = exoPlayer.getDuration();
                        long newPos = currentPos + 10000;
                        if (duration > 0) {
                            newPos = Math.min(duration, newPos);
                        }
                        exoPlayer.seekTo(newPos);
                    }
                });
            }

            // Nút Fullscreen
            btnFullscreen = playerView.findViewById(R.id.btnFullscreen);
            if (btnFullscreen != null) {
                updateFullscreenIcon();
                btnFullscreen.setOnClickListener(v -> {
                    boolean isLandscape = getResources()
                            .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
                    setRequestedOrientation(
                            isLandscape
                                    ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                });
            }

            // Nút phụ đề
            btnSubtitle = playerView.findViewById(R.id.btnSubtitle);
            if (btnSubtitle != null) {
                updateSubtitleButton();
                btnSubtitle.setOnClickListener(v -> {
                    subtitleEnabled = !subtitleEnabled;
                    toggleSubtitle();
                    updateSubtitleButton();
                });
            }

            // Lắng nghe sự thay đổi hiển thị của bộ điều khiển để căn chỉnh phụ đề
            playerView.setControllerVisibilityListener(
                    new PlayerView.ControllerVisibilityListener() {
                        @Override
                        public void onVisibilityChanged(int visibility) {

                            boolean visible = visibility == View.VISIBLE;

                            adjustSubtitleViewPosition(visible);

                            if (topInfo != null) {
                                topInfo.setVisibility(
                                        visible ? View.VISIBLE : View.GONE
                                );
                            }
                        }
                    }
            );
        }

        // Cập nhật thông tin chi tiết phim
        populateMovieDetails();

        // Ánh xạ nút yêu thích
        btnFavorite = findViewById(R.id.btnFavorite);
        // Nếu đã có mediaDetail (ví dụ sau khi xoay màn hình), thiết lập ngay
        if (mediaDetail != null) {
            setupFavoriteButton();
        }

        // Khởi tạo RecyclerView danh sách phim đề xuất (chỉ ở Portrait)
        rvListMovies = findViewById(R.id.rvListMovies);
        if (rvListMovies != null) {
            if (recommendedAdapter == null) {
                recommendedAdapter = new RecommendedMoviesAdapter(new java.util.ArrayList<>());
            }
            rvListMovies.setLayoutManager(new LinearLayoutManager(this));
            rvListMovies.setAdapter(recommendedAdapter);
            // Tắt scroll của RecyclerView để không xung đột với scroll ngoài
            rvListMovies.setNestedScrollingEnabled(false);
        }

        // Xử lý sự kiện click mở fragment bình luận/đánh giá (chỉ áp dụng ở Portrait layout)
        LinearLayout contentLayout = findViewById(R.id.svInfo);
        FrameLayout fragmentContainer = findViewById(R.id.detailFragmentContainer);
        LinearLayout commentFrame = findViewById(R.id.commentFrame);


        if (commentFrame != null && contentLayout != null && fragmentContainer != null) {
            commentFrame.setOnClickListener(v -> {
                contentLayout.setVisibility(View.GONE);
                fragmentContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_up, 0)
                        .replace(R.id.detailFragmentContainer, new CommentFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

//        if (rateFrame != null && contentLayout != null && fragmentContainer != null) {
//            rateFrame.setOnClickListener(v -> {
//                contentLayout.setVisibility(View.GONE);
//                fragmentContainer.setVisibility(View.VISIBLE);
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .setCustomAnimations(R.anim.slide_up, 0)
//                        .replace(R.id.detailFragmentContainer, new RateFragment())
//                        .addToBackStack(null)
//                        .commit();
//            });
//        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Hủy liên kết exoPlayer với PlayerView cũ
        if (playerView != null) {
            playerView.setPlayer(null);
        }

        // Tải lại layout để tự động lựa chọn đúng layout cho hướng xoay hiện tại
        setContentView(R.layout.activity_play);

        // Khởi tạo lại các View và liên kết lại player
        initViews(newConfig.orientation);
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
