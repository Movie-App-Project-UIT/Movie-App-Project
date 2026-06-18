package com.example.pemomovie.api;

import com.example.pemomovie.dto.SyncUserRequest;
import com.example.pemomovie.dto.UserProfileDto;
import com.example.pemomovie.dto.EmailRequest;
import com.example.pemomovie.dto.ResetPasswordRequest;
import com.example.pemomovie.dto.VerifyCodeRequest;
import com.example.pemomovie.dto.MessageResponse;
import com.example.pemomovie.dto.MediaItemDto;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/v1/users/sync")
    Call<UserProfileDto> syncUser(@Body SyncUserRequest request);

    @GET("/api/v1/users/me")
    Call<UserProfileDto> getMyProfile();

    @POST("/api/v1/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body EmailRequest request);

    @POST("/api/v1/auth/verify-code")
    Call<MessageResponse> verifyCode(@Body VerifyCodeRequest request);

    @POST("/api/v1/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);

    @GET("/api/v1/media/home")
    Call<Map<String, List<MediaItemDto>>> getHomepageData();

    @GET("/api/v1/media/{id}")
    Call<com.example.pemomovie.dto.MediaDetailResponse> getMediaDetail(@retrofit2.http.Path("id") Long id);

    @Multipart
    @POST("/api/v1/users/avatar")
    Call<ResponseBody> uploadAvatar(@Part MultipartBody.Part file);
}
