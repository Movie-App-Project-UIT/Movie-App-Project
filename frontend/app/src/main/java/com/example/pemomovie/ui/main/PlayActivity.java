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
import android.widget.ScrollView;

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
    private int startPosition = 0;
    private MediaDetailResponse mediaDetail; // Lưu trữ thông tin phim để khôi phục dữ liệu khi xoay màn hình
    private LinearLayout topInfo;
    private boolean viewCountIncremented = false;

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
        startPosition = getIntent().getIntExtra("START_POSITION", 0);
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
                        if (videoUrl.startsWith("/")) {
                            String baseUrl = com.example.pemomovie.BuildConfig.BASE_URL;
                            if (baseUrl.endsWith("/")) {
                                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                            }
                            videoUrl = baseUrl + videoUrl;
                        }
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
                            .setLabel(sub.getLanguage() != null ? sub.getLanguage() : "Tiếng Việt")
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build();
                    subtitleConfigs.add(config);
                }
            }
            mediaItemBuilder.setSubtitleConfigurations(subtitleConfigs.build());
        }

        exoPlayer.setMediaItem(mediaItemBuilder.build());
        exoPlayer.prepare();
        if (startPosition > 0) {
            exoPlayer.seekTo(startPosition * 1000L);
        }
        exoPlayer.play();
        startSavingHistory(); // Bắt đầu đếm giờ và lưu lịch sử xem

        // Ghi nhận lượt xem thực tế khi ấn Play (hoặc tự động Play) lần đầu tiên
        if (!viewCountIncremented && movieId != null) {
            apiService.incrementViewCount(movieId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        viewCountIncremented = true;
                        Log.d("PlayActivity", "Tăng lượt xem thành công!");
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("PlayActivity", "Lỗi tăng lượt xem: " + t.getMessage());
                }
            });
        }

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

    private void updatePlayPauseButtons(boolean isPlaying) {
        if (btnPlayCustom != null) {
            btnPlayCustom.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
        }
        if (btnPauseCustom != null) {
            btnPauseCustom.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        }
    }


    private void populateMovieDetails() {
        if (mediaDetail == null) return;

        // Thiết lập thông tin phim cho Portrait layout
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText(mediaDetail.getTitle());
        }
        TextView tvViews = findViewById(R.id.tvViews);
        if (tvViews != null) {
            String views = mediaDetail.getViewCount() != null ? String.valueOf(mediaDetail.getViewCount()) : "0";
            tvViews.setText(views + " lượt xem");
        }
        TextView tvTime = findViewById(R.id.tvTime);
        if (tvTime != null) {
            String year = mediaDetail.getReleaseYear() != null ? String.valueOf(mediaDetail.getReleaseYear()) : "N/A";
            
            // Xử lý đồng bộ dữ liệu: Nếu Backend chưa kịp cập nhật hoặc trả về 0/null, nhưng bộ nhớ thiết bị đã lưu là "Yêu thích"
            int favCount = mediaDetail.getFavoriteCount() != null ? mediaDetail.getFavoriteCount() : 0;
            if ((mediaDetail.getFavoriteCount() == null || mediaDetail.getFavoriteCount() == 0) && FavoriteManager.isFavorite(this, mediaDetail.getId())) {
                favCount = 1;
                mediaDetail.setFavoriteCount(1); // Cập nhật lại vào đối tượng để lần ấn tim tiếp theo tính toán đúng
            }
            
            tvTime.setText(year + "  •  " + favCount + " lượt yêu thích");
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
            int currentCount = mediaDetail.getFavoriteCount() != null ? mediaDetail.getFavoriteCount() : 0;
            if (isAdded) {
                btnFavorite.setImageResource(R.drawable.ic_heart);
                btnFavorite.setColorFilter(Color.parseColor("#FF1493"));
                mediaDetail.setFavoriteCount(currentCount + 1);
                Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorites);
                btnFavorite.setColorFilter(null);
                mediaDetail.setFavoriteCount(Math.max(0, currentCount - 1));
                Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            }
            populateMovieDetails();
            
            // Gửi API về Backend để đồng bộ Watchlist vào Database
            apiService.toggleWatchlist(mediaDetail.getId()).enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(retrofit2.Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        android.util.Log.e("PlayActivity", "Lỗi đồng bộ Watchlist với DB: " + response.code());
                    } else {
                        android.util.Log.d("PlayActivity", "Đồng bộ Watchlist thành công!");
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<okhttp3.ResponseBody> call, Throwable t) {
                    android.util.Log.e("PlayActivity", "Lỗi mạng khi đồng bộ Watchlist: " + t.getMessage());
                }
            });
        });
    }


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

            // Play/pause
            if (exoPlayer != null) {
                updatePlayPauseButtons(exoPlayer.isPlaying());
            } else {
                // Mặc định: hiện nút Play, ẩn nút Pause
                updatePlayPauseButtons(false);
            }

            // Nút lùi 10s
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

            // Nút tăng 10s
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
                    if (exoPlayer != null) {
                        androidx.media3.ui.TrackSelectionDialogBuilder builder = 
                            new androidx.media3.ui.TrackSelectionDialogBuilder(
                                PlayActivity.this,
                                "Chọn phụ đề",
                                exoPlayer,
                                androidx.media3.common.C.TRACK_TYPE_TEXT
                            );
                        builder.setTheme(androidx.appcompat.R.style.Theme_AppCompat_Dialog);
                        builder.build().show();
                    }
                });
            }

            // Sự kiện ẩn hiện các controll của video
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
        // Nếu đã có mediaDetail thiết lập ngay
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
        ScrollView contentLayout = findViewById(R.id.svInfo);
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

    private android.os.Handler historyHandler = new android.os.Handler();
    private Runnable historyRunnable;

    private void startSavingHistory() {
        if (historyRunnable == null) {
            historyRunnable = new Runnable() {
                @Override
                public void run() {
                    if (exoPlayer != null && exoPlayer.isPlaying() && movieId != null) {
                        saveWatchHistory();
                    }
                    historyHandler.postDelayed(this, 10000); // Lưu mỗi 10 giây
                }
            };
        }
        historyHandler.postDelayed(historyRunnable, 10000);
    }

    private void stopSavingHistory() {
        if (historyRunnable != null) {
            historyHandler.removeCallbacks(historyRunnable);
        }
    }

    private void saveWatchHistory() {
        if (exoPlayer != null && movieId != null) {
            int progressSeconds = (int) (exoPlayer.getCurrentPosition() / 1000);
            int totalDurationSeconds = (int) (exoPlayer.getDuration() / 1000);
            
            // Nếu duration hợp lệ (>0) thì mới lưu
            if (totalDurationSeconds > 0) {
                // Hiện tại chỉ hỗ trợ phim lẻ, nếu là phim bộ thì truyền thêm episodeId
                apiService.updateHistory(new com.example.pemomovie.dto.UpdateHistoryRequest(movieId, null, progressSeconds, totalDurationSeconds))
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}
                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {}
                        });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSavingHistory();
        saveWatchHistory(); // Lưu lần cuối trước khi thoát
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
