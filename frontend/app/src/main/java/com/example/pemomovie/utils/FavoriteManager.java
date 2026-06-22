package com.example.pemomovie.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.pemomovie.api.ApiClient;
import com.example.pemomovie.api.ApiService;
import com.example.pemomovie.dto.MediaItemDto;
import com.example.pemomovie.dto.WatchlistItemDto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteManager {
    private static final String PREF_NAME = "favorite_prefs";

    // Lấy Key lưu trữ riêng biệt cho từng User (Dựa vào Firebase UID)
    private static String getUserSpecificKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return "favorite_movies_" + user.getUid();
        }
        return "favorite_movies_guest";
    }

    // Hàm lấy danh sách phim yêu thích
    public static List<MediaItemDto> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(getUserSpecificKey(), null);
        
        if (json == null) {
            return new ArrayList<>();
        }
        
        Gson gson = new Gson();
        Type type = new TypeToken<List<MediaItemDto>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Hàm lưu danh sách phim yêu thích
    public static void saveFavorites(Context context, List<MediaItemDto> favorites) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        Gson gson = new Gson();
        String json = gson.toJson(favorites);
        
        editor.putString(getUserSpecificKey(), json);
        editor.apply();
    }

    // Kiểm tra xem 1 phim (dựa theo ID) đã được thêm vào Favorite chưa
    public static boolean isFavorite(Context context, Long movieId) {
        List<MediaItemDto> favorites = getFavorites(context);
        for (MediaItemDto movie : favorites) {
            if (movie.getId() != null && movie.getId().equals(movieId)) {
                return true;
            }
        }
        return false;
    }

    // Hàm Thêm hoặc Xóa phim cục bộ (Hàm này dùng cho nút bấm thả Tym)
    public static boolean toggleFavorite(Context context, MediaItemDto movie) {
        List<MediaItemDto> favorites = getFavorites(context);
        boolean isAdded = false;

        MediaItemDto existingMovie = null;
        for (MediaItemDto item : favorites) {
            if (item.getId() != null && item.getId().equals(movie.getId())) {
                existingMovie = item;
                break;
            }
        }

        if (existingMovie != null) {
            favorites.remove(existingMovie);
        } else {
            favorites.add(movie);
            isAdded = true;
        }

        saveFavorites(context, favorites);
        
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            ApiClient.getApiService().toggleWatchlist(movie.getId()).enqueue(new Callback<okhttp3.ResponseBody>() {
                @Override
                public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                    if (!response.isSuccessful()) {
                        Log.e("FavoriteManager", "Lỗi đồng bộ Favorite lên backend: " + response.code());
                    }
                }
                @Override
                public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                    Log.e("FavoriteManager", "Lỗi kết nối khi đồng bộ Favorite lên backend");
                }
            });
        }

        return isAdded;
    }

    // Hàm đồng bộ danh sách yêu thích từ Backend Database về máy
    public static void syncFavoritesWithBackend(Context context, Runnable onComplete) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        apiService.getMyWatchlist().enqueue(new Callback<List<WatchlistItemDto>>() {
            @Override
            public void onResponse(Call<List<WatchlistItemDto>> call, Response<List<WatchlistItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MediaItemDto> syncedList = new ArrayList<>();
                    for (WatchlistItemDto dto : response.body()) {
                        if (dto.getMedia() != null) {
                            syncedList.add(dto.getMedia());
                        }
                    }
                    saveFavorites(context, syncedList);
                    Log.d("FavoriteManager", "Đồng bộ Watchlist thành công: " + syncedList.size() + " phim");
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(Call<List<WatchlistItemDto>> call, Throwable t) {
                Log.e("FavoriteManager", "Lỗi mạng khi đồng bộ Watchlist: " + t.getMessage());
                if (onComplete != null) onComplete.run();
            }
        });
    }
}
