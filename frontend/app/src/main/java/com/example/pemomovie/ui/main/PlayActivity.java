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
import android.media.AudioManager;
import android.widget.SeekBar;

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

@OptIn(markerClass = UnstableApi.class)
public class PlayActivity extends AppCompatActivity {

    private ImageButton btnFullscreen;
    private ImageButton btnSubtitle;
    private boolean subtitleEnabled = false;

    private ExoPlayer exoPlayer;
    private PlayerView playerView;
    private ApiService apiService;
    private Long movieId;
    private Long episodeId = -1L;
    private int startPosition = 0;
    private MediaDetailResponse mediaDetail; // Lưu trữ thông tin phim để khôi phục dữ liệu khi xoay màn hình
    private LinearLayout topInfo;
    private boolean viewCountIncremented = false;

    // Play/Pause button references (custom controller)
    private View btnPlayCustom;
    private View btnPauseCustom;

    // Recommended movies
    private RecyclerView rvListMovies;
    private com.example.pemomovie.adapter.PosterAdapter posterAdapter;
    private java.util.List<com.example.pemomovie.dto.MediaItemDto> similarMoviesList = new java.util.ArrayList<>();

    // Favorite button
    private ImageButton btnFavorite;

    // Comment fields
    private com.example.pemomovie.adapter.CommentAdapter commentAdapter;
    private java.util.List<com.example.pemomovie.dto.ReviewResponseDto> reviewList = new java.util.ArrayList<>();
    private Long replyingToReviewId = null;

    // Episode fields
    private com.example.pemomovie.adapter.EpisodeAdapter episodeAdapter;
    private java.util.List<com.example.pemomovie.dto.EpisodeDto> episodesList = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Khởi tạo các thành phần giao diện theo hướng màn hình hiện tại
        initViews(getResources().getConfiguration().orientation);

        movieId = getIntent().getLongExtra("MOVIE_ID", -1L);
        episodeId = getIntent().getLongExtra("EPISODE_ID", -1L);
        startPosition = getIntent().getIntExtra("START_POSITION", 0);
        if (movieId != -1L) {
            fetchMediaDetailAndPlay();
        } else {
            Toast.makeText(this, "Không tìm thấy ID phim!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchMediaDetailAndPlay() {
        apiService.getMediaDetail(movieId).enqueue(new Callback<MediaDetailResponse>() {
            @Override
            public void onResponse(Call<MediaDetailResponse> call, Response<MediaDetailResponse> response) {
                if (isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    mediaDetail = response.body();
                    populateMovieDetails();
                    setupFavoriteButton();
                    fetchSimilarMovies();
                    loadComments();

                    if ("TV_SHOW".equalsIgnoreCase(mediaDetail.getMediaType()) || "TV_SERIES".equalsIgnoreCase(mediaDetail.getMediaType())) {
                        playEpisode();
                    } else {
                        playMovie();
                    }
                } else {
                    Toast.makeText(PlayActivity.this, "Không thể lấy chi tiết phim", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<MediaDetailResponse> call, Throwable t) {
                Toast.makeText(PlayActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playMovie() {
        apiService.getPlayableVideoUrl(movieId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String videoUrl = response.body().string().trim();
                        if (videoUrl.startsWith("/")) {
                            String baseUrl = com.example.pemomovie.BuildConfig.BASE_URL;
                            if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                            videoUrl = baseUrl + videoUrl;
                        }
                        setupExoPlayer(videoUrl, mediaDetail.getSubtitles());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(PlayActivity.this, "Không thể lấy link video (có thể do chưa có VIP)", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    private void playEpisode() {
        if (episodeId == -1L) {
            if (mediaDetail.getSeasons() != null && !mediaDetail.getSeasons().isEmpty()) {
                com.example.pemomovie.dto.SeasonDto firstSeason = mediaDetail.getSeasons().get(0);
                if (firstSeason.getEpisodes() != null) {
                    for (com.example.pemomovie.dto.EpisodeDto ep : firstSeason.getEpisodes()) {
                        boolean hasData = (ep.getVideoUrl() != null && !ep.getVideoUrl().trim().isEmpty()) || 
                                          (ep.getSubtitles() != null && !ep.getSubtitles().isEmpty());
                        if (!ep.isDeleted() && hasData) {
                            episodeId = ep.getId();
                            break;
                        }
                    }
                }
            }
        }
        
        if (episodeId == -1L) {
            Toast.makeText(this, "Không tìm thấy tập phim nào", Toast.LENGTH_SHORT).show();
            return;
        }

        // Focus the current episode
        if (episodeAdapter != null && episodesList != null) {
            for (int i = 0; i < episodesList.size(); i++) {
                if (episodesList.get(i).getId().equals(episodeId)) {
                    episodeAdapter.setSelectedPosition(i);
                    // Also scroll the RecyclerView to show the selected episode
                    RecyclerView rvEpisodes = findViewById(R.id.rvEpisodes);
                    if (rvEpisodes != null) {
                        rvEpisodes.scrollToPosition(i);
                    }
                    break;
                }
            }
        }
        
        com.example.pemomovie.dto.EpisodeDto currentEpisode = null;
        if (mediaDetail.getSeasons() != null && !mediaDetail.getSeasons().isEmpty()) {
            com.example.pemomovie.dto.SeasonDto firstSeason = mediaDetail.getSeasons().get(0);
            if (firstSeason.getEpisodes() != null) {
                for (com.example.pemomovie.dto.EpisodeDto ep : firstSeason.getEpisodes()) {
                    if (ep.getId().equals(episodeId)) {
                        currentEpisode = ep;
                        break;
                    }
                }
            }
        }
        
        List<SubtitleDto> subtitles = currentEpisode != null ? currentEpisode.getSubtitles() : null;

        apiService.getPlayableEpisodeUrl(movieId, episodeId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String videoUrl = response.body().string().trim();
                        if (videoUrl.startsWith("/")) {
                            String baseUrl = com.example.pemomovie.BuildConfig.BASE_URL;
                            if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                            videoUrl = baseUrl + videoUrl;
                        }
                        setupExoPlayer(videoUrl, subtitles);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(PlayActivity.this, "Không thể lấy link video (có thể do chưa có VIP)", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    private void fetchSimilarMovies() {
        if (mediaDetail == null || mediaDetail.getGenres() == null || mediaDetail.getGenres().isEmpty()) {
            return;
        }
        String firstGenre = mediaDetail.getGenres().get(0);
        String genreName = firstGenre;
        String mediaType = "MOVIE".equalsIgnoreCase(mediaDetail.getMediaType()) ? "MOVIE" : "TV_SHOW";
        
        apiService.getGenres().enqueue(new Callback<java.util.List<com.example.pemomovie.dto.GenreDto>>() {
            @Override
            public void onResponse(Call<java.util.List<com.example.pemomovie.dto.GenreDto>> call, Response<java.util.List<com.example.pemomovie.dto.GenreDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long genreId = null;
                    for (com.example.pemomovie.dto.GenreDto g : response.body()) {
                        if (g.getName().equalsIgnoreCase(genreName)) {
                            genreId = g.getId();
                            break;
                        }
                    }
                    if (genreId != null) {
                        apiService.filterMedia(null, java.util.Collections.singletonList(genreId), null, null, null, null, null, null, null, 0, 10).enqueue(new Callback<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>>() {
                            @Override
                            public void onResponse(Call<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>> call, Response<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    similarMoviesList.clear();
                                    for (MediaItemDto m : response.body().getContent()) {
                                        if (!m.getId().equals(movieId)) {
                                            similarMoviesList.add(m);
                                        }
                                    }
                                    runOnUiThread(() -> {
                                        if (posterAdapter != null) {
                                            posterAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onFailure(Call<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>> call, Throwable t) {}
                        });
                    }
                }
            }
            @Override
            public void onFailure(Call<java.util.List<com.example.pemomovie.dto.GenreDto>> call, Throwable t) {}
        });
    }

    private void setupExoPlayer(String videoUrl, List<SubtitleDto> subtitles) {
        if (exoPlayer == null) {
            androidx.media3.exoplayer.trackselection.DefaultTrackSelector trackSelector = new androidx.media3.exoplayer.trackselection.DefaultTrackSelector(this);
            // Cấu hình tua lùi 10s và tua tiến 10s mặc định bằng setSeekBackIncrementMs/setSeekForwardIncrementMs
            exoPlayer = new ExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
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

    private void updateVolumeIcon(ImageButton btnVolume, int volume) {
        if (btnVolume != null) {
            if (volume == 0) {
                btnVolume.setImageResource(R.drawable.ic_volume_off);
            } else {
                btnVolume.setImageResource(R.drawable.ic_volume_up);
            }
        }
    }

    private void showSpeedDialog() {
        String[] options = {"0.5x", "0.75x", "1.0x (Chuẩn)", "1.25x", "1.5x", "2.0x"};
        float[] speeds = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};
        
        new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Tốc độ phát")
                .setItems(options, (dialog, which) -> {
                    float speed = speeds[which];
                    exoPlayer.setPlaybackParameters(new androidx.media3.common.PlaybackParameters(speed));
                    Toast.makeText(this, "Tốc độ: " + options[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showSubtitleDialog() {
        androidx.media3.common.Tracks tracks = exoPlayer.getCurrentTracks();
        java.util.List<androidx.media3.common.TrackSelectionOverride> textOverrides = new java.util.ArrayList<>();
        java.util.List<String> optionsList = new java.util.ArrayList<>();
        
        optionsList.add("Tắt phụ đề");
        textOverrides.add(null); // Tương ứng với tắt
        
        for (androidx.media3.common.Tracks.Group trackGroup : tracks.getGroups()) {
            if (trackGroup.getType() == C.TRACK_TYPE_TEXT) {
                androidx.media3.common.TrackGroup group = trackGroup.getMediaTrackGroup();
                for (int i = 0; i < group.length; i++) {
                    androidx.media3.common.Format format = group.getFormat(i);
                    String lang = format.language != null ? format.language : "Không rõ";
                    String label = format.label != null ? format.label : lang;
                    optionsList.add(label);
                    textOverrides.add(new androidx.media3.common.TrackSelectionOverride(group, i));
                }
            }
        }

        String[] options = optionsList.toArray(new String[0]);
        
        new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Chọn phụ đề")
                .setItems(options, (dialog, which) -> {
                    androidx.media3.common.TrackSelectionOverride override = textOverrides.get(which);
                    if (override == null) {
                        // Tắt phụ đề
                        subtitleEnabled = false;
                        exoPlayer.setTrackSelectionParameters(
                                exoPlayer.getTrackSelectionParameters()
                                        .buildUpon()
                                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                                        .build()
                        );
                        if (playerView != null && playerView.getSubtitleView() != null) {
                            playerView.getSubtitleView().setVisibility(View.GONE);
                        }
                        updateSubtitleButton();
                        Toast.makeText(this, "Đã tắt phụ đề", Toast.LENGTH_SHORT).show();
                    } else {
                        // Bật phụ đề đã chọn
                        subtitleEnabled = true;
                        exoPlayer.setTrackSelectionParameters(
                                exoPlayer.getTrackSelectionParameters()
                                        .buildUpon()
                                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                                        .addOverride(override)
                                        .build()
                        );
                        if (playerView != null && playerView.getSubtitleView() != null) {
                            playerView.getSubtitleView().setVisibility(View.VISIBLE);
                        }
                        updateSubtitleButton();
                        Toast.makeText(this, "Phụ đề: " + options[which], Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showQualityDialog() {
        androidx.media3.common.Tracks tracks = exoPlayer.getCurrentTracks();
        java.util.Map<Integer, androidx.media3.common.TrackSelectionOverride> overrideMap = new java.util.HashMap<>();
        
        java.util.List<Integer> heights = new java.util.ArrayList<>();
        
        for (androidx.media3.common.Tracks.Group trackGroup : tracks.getGroups()) {
            if (trackGroup.getType() == C.TRACK_TYPE_VIDEO) {
                androidx.media3.common.TrackGroup group = trackGroup.getMediaTrackGroup();
                for (int i = 0; i < group.length; i++) {
                    androidx.media3.common.Format format = group.getFormat(i);
                    int height = format.height;
                    // Lọc trùng: nếu cloud sinh ra nhiều bitrate cho cùng 1 độ phân giải
                    if (height > 0 && !heights.contains(height)) {
                        heights.add(height);
                        overrideMap.put(height, new androidx.media3.common.TrackSelectionOverride(group, i));
                    }
                }
            }
        }

        // Sắp xếp độ phân giải từ cao xuống thấp
        java.util.Collections.sort(heights, java.util.Collections.reverseOrder());

        String[] options = new String[heights.size() + 1];
        options[0] = "Tự động (Auto)";
        for (int i = 0; i < heights.size(); i++) {
            int h = heights.get(i);
            options[i + 1] = h >= 720 ? h + "p (HD)" : h + "p";
        }

        new android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Chọn chất lượng")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Auto
                        exoPlayer.setTrackSelectionParameters(
                                exoPlayer.getTrackSelectionParameters()
                                        .buildUpon()
                                        .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                        .build()
                        );
                        Toast.makeText(this, "Chất lượng: Tự động", Toast.LENGTH_SHORT).show();
                    } else {
                        int height = heights.get(which - 1);
                        androidx.media3.common.TrackSelectionOverride override = overrideMap.get(height);
                        if (override != null) {
                            exoPlayer.setTrackSelectionParameters(
                                    exoPlayer.getTrackSelectionParameters()
                                            .buildUpon()
                                            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                                            .addOverride(override)
                                            .build()
                            );
                            Toast.makeText(this, "Chất lượng: " + height + "p", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }


    private void populateMovieDetails() {
        if (mediaDetail == null) return;

        // Thiết lập thông tin phim cho Portrait layout
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText(mediaDetail.getTitle());
        }

        // Cập nhật trạng thái lượt yêu thích ẩn danh nếu cần
        int favCount = mediaDetail.getFavoriteCount() != null ? mediaDetail.getFavoriteCount() : 0;
        if ((mediaDetail.getFavoriteCount() == null || mediaDetail.getFavoriteCount() == 0) && FavoriteManager.isFavorite(this, mediaDetail.getId())) {
            favCount = 1;
            mediaDetail.setFavoriteCount(1);
        }

        // Setup Episodes (nếu là phim bộ)
        RecyclerView rvEpisodes = findViewById(R.id.rvEpisodes);
        if (rvEpisodes != null) {
            if ("TV_SHOW".equalsIgnoreCase(mediaDetail.getMediaType()) || "TV_SERIES".equalsIgnoreCase(mediaDetail.getMediaType())) {
                rvEpisodes.setVisibility(View.VISIBLE);
                episodesList.clear();
                if (mediaDetail.getSeasons() != null && !mediaDetail.getSeasons().isEmpty()) {
                    com.example.pemomovie.dto.SeasonDto firstSeason = mediaDetail.getSeasons().get(0);
                    if (firstSeason.getEpisodes() != null) {
                        for (com.example.pemomovie.dto.EpisodeDto ep : firstSeason.getEpisodes()) {
                            boolean hasData = (ep.getVideoUrl() != null && !ep.getVideoUrl().trim().isEmpty()) || 
                                              (ep.getSubtitles() != null && !ep.getSubtitles().isEmpty());
                            if (!ep.isDeleted() && hasData) {
                                episodesList.add(ep);
                            }
                        }
                    }
                }
                
                episodeAdapter = new com.example.pemomovie.adapter.EpisodeAdapter(this, episodesList, (episode, position) -> {
                    episodeId = episode.getId();
                    playEpisode();
                });
                
                // Find selected position
                for (int i = 0; i < episodesList.size(); i++) {
                    if (episodesList.get(i).getId().equals(episodeId)) {
                        episodeAdapter.setSelectedPosition(i);
                        break;
                    }
                }
                
                rvEpisodes.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));
                rvEpisodes.setAdapter(episodeAdapter);
            } else {
                rvEpisodes.setVisibility(View.GONE);
            }
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
        View globalHeaderInclude = findViewById(R.id.globalHeaderInclude);
        
        // Thiết lập chế độ Fullscreen ẩn hoàn toàn thanh hệ thống ở chế độ ngang (Landscape) để chỉ hiển thị ExoPlayer
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (globalHeaderInclude != null) {
                globalHeaderInclude.setPadding(0, 0, 0, 0);
            }
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
            if (globalHeaderInclude != null) {
                final int paddingLeft = globalHeaderInclude.getPaddingLeft();
                final int paddingRight = globalHeaderInclude.getPaddingRight();
                final int paddingBottom = globalHeaderInclude.getPaddingBottom();
                
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(globalHeaderInclude, (v, insets) -> {
                    androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                    v.setPadding(paddingLeft, systemBars.top, paddingRight, paddingBottom);
                    return insets;
                });
            }
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

            // Cài đặt chọn chất lượng video (Settings)
            View btnSettings = playerView.findViewById(androidx.media3.ui.R.id.exo_settings);
            if (btnSettings != null) {
                btnSettings.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        showQualityDialog();
                    }
                });
            }

            // Tốc độ phát
            View btnSpeed = playerView.findViewById(R.id.btnSpeed);
            if (btnSpeed != null) {
                btnSpeed.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        showSpeedDialog();
                    }
                });
            }

            // Thanh trượt âm lượng
            ImageButton btnVolume = playerView.findViewById(R.id.btnVolume);
            SeekBar seekBarVolume = playerView.findViewById(R.id.seekBarVolume);
            if (btnVolume != null && seekBarVolume != null) {
                AudioManager audioManager = (AudioManager) getSystemService(android.content.Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    seekBarVolume.setMax(maxVolume);
                    seekBarVolume.setProgress(currentVolume);

                    updateVolumeIcon(btnVolume, currentVolume);
                    // Ẩn thanh trượt mặc định
                    seekBarVolume.setVisibility(View.GONE);

                    seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                            }
                            updateVolumeIcon(btnVolume, progress);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });

                    btnVolume.setOnClickListener(v -> {
                        // Toggle visibility
                        if (seekBarVolume.getVisibility() == View.VISIBLE) {
                            seekBarVolume.setVisibility(View.GONE);
                        } else {
                            seekBarVolume.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            // Phụ đề (Subtitle)
            btnSubtitle = playerView.findViewById(R.id.btnSubtitle);
            if (btnSubtitle != null) {
                updateSubtitleButton(); // Cập nhật icon ban đầu
                btnSubtitle.setOnClickListener(v -> {
                    if (exoPlayer != null) {
                        showSubtitleDialog();
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
                            
                            // Cập nhật lại thanh âm lượng nếu hệ thống thay đổi bằng phím cứng khi ẩn controller
                            if (visible && playerView != null) {
                                SeekBar seekBarVolume = playerView.findViewById(R.id.seekBarVolume);
                                ImageButton btnVolume = playerView.findViewById(R.id.btnVolume);
                                AudioManager audioManager = (AudioManager) getSystemService(android.content.Context.AUDIO_SERVICE);
                                if (audioManager != null && seekBarVolume != null && btnVolume != null) {
                                    int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                    seekBarVolume.setProgress(currentVol);
                                    updateVolumeIcon(btnVolume, currentVol);
                                }
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

        // Khởi tạo thanh tìm kiếm trên Header
        new com.example.pemomovie.utils.GlobalHeaderHelper(this).setupGlobalHeader(findViewById(R.id.globalHeaderInclude));

        // Khởi tạo RecyclerView danh sách phim đề xuất (chỉ ở Portrait)
        rvListMovies = findViewById(R.id.rvListMovies);
        if (rvListMovies != null) {
            if (posterAdapter == null) {
                posterAdapter = new com.example.pemomovie.adapter.PosterAdapter(this, similarMoviesList);
            }
            rvListMovies.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 3));
            rvListMovies.setAdapter(posterAdapter);
            rvListMovies.setNestedScrollingEnabled(false);
        }

        // Setup Comments
        RecyclerView rvComment = findViewById(R.id.rvComment);
        android.widget.EditText etComment = findViewById(R.id.etComment);
        ImageButton btnCancelReply = findViewById(R.id.btnCancelReply);
        ImageButton btnSendComment = findViewById(R.id.btnSendComment);
        android.widget.ImageView ivCurrentUserAvatar = findViewById(R.id.ivCurrentUserAvatar);

        if (ivCurrentUserAvatar != null) {
            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getPhotoUrl() != null && !currentUser.getPhotoUrl().toString().trim().isEmpty()) {
                String photoUrl = currentUser.getPhotoUrl().toString().trim();
                if (photoUrl.startsWith("\"") && photoUrl.endsWith("\"")) {
                    photoUrl = photoUrl.substring(1, photoUrl.length() - 1);
                }
                com.bumptech.glide.Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_avatar)
                        .circleCrop()
                        .into(ivCurrentUserAvatar);
            }
        }

        if (rvComment != null && etComment != null) {
            commentAdapter = new com.example.pemomovie.adapter.CommentAdapter(this, reviewList, new com.example.pemomovie.adapter.CommentAdapter.OnCommentActionClickListener() {
                @Override
                public void onReplyClick(com.example.pemomovie.dto.ReviewResponseDto comment) {
                    com.example.pemomovie.ui.main.ReplyBottomSheetFragment bottomSheet = new com.example.pemomovie.ui.main.ReplyBottomSheetFragment(comment, movieId);
                    bottomSheet.show(getSupportFragmentManager(), "ReplyBottomSheet");
                }

                @Override
                public void onReportClick(com.example.pemomovie.dto.ReviewResponseDto comment) {
                    showReportDialog(comment.getId());
                }
            });
            rvComment.setLayoutManager(new LinearLayoutManager(this));
            rvComment.setAdapter(commentAdapter);
            rvComment.setNestedScrollingEnabled(false);

            TextView tvCommentTitle = findViewById(R.id.tvCommentTitle);
            if (tvCommentTitle != null) {
                tvCommentTitle.setText("Bình luận (" + reviewList.size() + ")");
            }

            if (btnCancelReply != null) {
                btnCancelReply.setOnClickListener(v -> {
                    replyingToReviewId = null;
                    etComment.setHint("Nêu cảm nhận của bạn");
                    etComment.setText("");
                    btnCancelReply.setVisibility(View.GONE);
                });
            }

            if (btnSendComment != null) {
                btnSendComment.setOnClickListener(v -> {
                    String content = etComment.getText().toString().trim();
                    if (!content.isEmpty() && movieId != -1L) {
                        postComment(content, etComment, btnCancelReply);
                    }
                });
            }
        }
    }

    private void showReportDialog(Long reviewId) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_report_comment);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        
        android.widget.EditText etReportReason = dialog.findViewById(R.id.etReportReason);
        android.widget.Button btnCancel = dialog.findViewById(R.id.btnCancelReport);
        android.widget.Button btnSubmit = dialog.findViewById(R.id.btnSubmitReport);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            String reason = etReportReason.getText().toString().trim();
            java.util.Map<String, String> payload = new java.util.HashMap<>();
            payload.put("reason", reason);
            apiService.reportReview(reviewId, payload).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PlayActivity.this, "Báo cáo thành công", Toast.LENGTH_SHORT).show();
                        java.util.Set<Long> idsToRemove = new java.util.HashSet<>();
                        idsToRemove.add(reviewId);
                        boolean added;
                        do {
                            added = false;
                            for (com.example.pemomovie.dto.ReviewResponseDto r : reviewList) {
                                if (!idsToRemove.contains(r.getId()) && r.getParentId() != null && idsToRemove.contains(r.getParentId())) {
                                    idsToRemove.add(r.getId());
                                    added = true;
                                }
                            }
                        } while (added);

                        java.util.Iterator<com.example.pemomovie.dto.ReviewResponseDto> iterator = reviewList.iterator();
                        while (iterator.hasNext()) {
                            if (idsToRemove.contains(iterator.next().getId())) {
                                iterator.remove();
                            }
                        }
                        if (commentAdapter != null) commentAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(PlayActivity.this, "Bạn đã báo cáo hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    Toast.makeText(PlayActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }

    private java.util.List<com.example.pemomovie.dto.ReviewResponseDto> allRootComments = new java.util.ArrayList<>();
    private int currentCommentPage = 0;
    private final int COMMENTS_PER_PAGE = 5;

    private void loadComments() {
        if (movieId == null || movieId == -1L || apiService == null) return;
        apiService.getReviews(movieId).enqueue(new retrofit2.Callback<java.util.List<com.example.pemomovie.dto.ReviewResponseDto>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.pemomovie.dto.ReviewResponseDto>> call, retrofit2.Response<java.util.List<com.example.pemomovie.dto.ReviewResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRootComments = response.body();
                    currentCommentPage = 0;
                    showCommentPage(0);
                }
            }
            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.pemomovie.dto.ReviewResponseDto>> call, Throwable t) {}
        });
    }

    private void showCommentPage(int page) {
        if (allRootComments == null || allRootComments.isEmpty()) {
            LinearLayout layoutPagination = findViewById(R.id.layoutPagination);
            if (layoutPagination != null) layoutPagination.setVisibility(View.GONE);
            return;
        }

        currentCommentPage = page;
        int totalPages = (int) Math.ceil((double) allRootComments.size() / COMMENTS_PER_PAGE);

        int start = currentCommentPage * COMMENTS_PER_PAGE;
        int end = Math.min(start + COMMENTS_PER_PAGE, allRootComments.size());

        reviewList.clear();
        for (int i = start; i < end; i++) {
            com.example.pemomovie.dto.ReviewResponseDto root = allRootComments.get(i);
            reviewList.add(root);
        }

        if (commentAdapter != null) {
            commentAdapter.notifyDataSetChanged();
        }

        TextView tvCommentTitle = findViewById(R.id.tvCommentTitle);
        if (tvCommentTitle != null) {
            tvCommentTitle.setText("Bình luận (" + allRootComments.size() + ")");
        }

        setupPaginationUI(totalPages);
    }

    private com.example.pemomovie.adapter.PaginationAdapter paginationAdapter;

    private void setupPaginationUI(int totalPages) {
        LinearLayout layoutPagination = findViewById(R.id.layoutPagination);
        if (layoutPagination == null) return;

        if (totalPages <= 1) {
            layoutPagination.setVisibility(View.GONE);
            return;
        }

        layoutPagination.setVisibility(View.VISIBLE);

        androidx.recyclerview.widget.RecyclerView rvPagination = findViewById(R.id.rvPagination);
        ImageButton btnPrevPage = findViewById(R.id.btnPrevPage);
        ImageButton btnNextPage = findViewById(R.id.btnNextPage);

        if (paginationAdapter == null) {
            paginationAdapter = new com.example.pemomovie.adapter.PaginationAdapter(this, totalPages, page -> {
                showCommentPage(page);
            });
            rvPagination.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            rvPagination.setAdapter(paginationAdapter);
        } else {
            paginationAdapter.setTotalPages(totalPages);
        }
        
        paginationAdapter.setCurrentPage(currentCommentPage);
        rvPagination.scrollToPosition(currentCommentPage);

        btnPrevPage.setAlpha(currentCommentPage > 0 ? 1.0f : 0.3f);
        btnPrevPage.setEnabled(currentCommentPage > 0);
        btnPrevPage.setOnClickListener(v -> {
            if (currentCommentPage > 0) showCommentPage(currentCommentPage - 1);
        });

        btnNextPage.setAlpha(currentCommentPage < totalPages - 1 ? 1.0f : 0.3f);
        btnNextPage.setEnabled(currentCommentPage < totalPages - 1);
        btnNextPage.setOnClickListener(v -> {
            if (currentCommentPage < totalPages - 1) showCommentPage(currentCommentPage + 1);
        });
    }

    private void postComment(String content, android.widget.EditText etComment, ImageButton btnCancelReply) {
        com.example.pemomovie.dto.ReviewRequestDto req = new com.example.pemomovie.dto.ReviewRequestDto(movieId, null, replyingToReviewId, content);
        apiService.postReview(req).enqueue(new retrofit2.Callback<com.example.pemomovie.dto.ReviewResponseDto>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.pemomovie.dto.ReviewResponseDto> call, retrofit2.Response<com.example.pemomovie.dto.ReviewResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    etComment.setText("");
                    etComment.setHint("Nêu cảm nhận của bạn");
                    replyingToReviewId = null;
                    if (btnCancelReply != null) {
                        btnCancelReply.setVisibility(View.GONE);
                    }
                    loadComments();
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                } else {
                    Toast.makeText(PlayActivity.this, "Lỗi khi đăng bình luận", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<com.example.pemomovie.dto.ReviewResponseDto> call, Throwable t) {}
        });
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
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.pause();
        }
        stopSavingHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSavingHistory();
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
