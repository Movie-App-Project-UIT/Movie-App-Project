package com.example.pemomovie.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.pemomovie.dto.MediaItemDto;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private static final String PREF_NAME = "favorite_prefs";
    private static final String KEY_FAVORITES = "favorite_movies";

    // Hàm lấy danh sách phim yêu thích
    public static List<MediaItemDto> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_FAVORITES, null);
        
        if (json == null) {
            return new ArrayList<>();
        }
        
        // Gson biến đổi chuỗi JSON lấy từ bộ nhớ thành List Object
        Gson gson = new Gson();
        Type type = new TypeToken<List<MediaItemDto>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Hàm lưu danh sách phim yêu thích
    public static void saveFavorites(Context context, List<MediaItemDto> favorites) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Gson biến đổi List Object thành chuỗi JSON để lưu vào máy
        Gson gson = new Gson();
        String json = gson.toJson(favorites);
        
        editor.putString(KEY_FAVORITES, json);
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

    // Hàm Thêm hoặc Xóa phim (Hàm này dùng cho nút bấm thả Tym)
    public static boolean toggleFavorite(Context context, MediaItemDto movie) {
        List<MediaItemDto> favorites = getFavorites(context);
        boolean isAdded = false;

        // Tìm kiếm phim hiện tại
        MediaItemDto existingMovie = null;
        for (MediaItemDto item : favorites) {
            if (item.getId() != null && item.getId().equals(movie.getId())) {
                existingMovie = item;
                break;
            }
        }

        // Logic toggle (nếu có rồi thì xóa đi, chưa có thì thêm vào)
        if (existingMovie != null) {
            favorites.remove(existingMovie);
        } else {
            favorites.add(movie);
            isAdded = true;
        }

        // Cập nhật lại bộ nhớ
        saveFavorites(context, favorites);
        return isAdded; // Trả về true nếu vừa thả tym, false nếu vừa gỡ tym
    }
}
