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

import retrofit2.http.Path;
import retrofit2.http.Query;
import com.example.pemomovie.dto.MediaDetailResponse;

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
    Call<MediaDetailResponse> getMediaDetail(@Path("id") Long id);

    @Multipart
    @POST("/api/v1/users/avatar")
    Call<ResponseBody> uploadAvatar(@Part MultipartBody.Part file);

    @POST("/api/v1/payments/create-url")
    Call<ResponseBody> createPaymentUrl(
            @Query("packageId") Long packageId,
            @Query("paymentMethod") String paymentMethod,
            @Query("amount") Long amount
    );

    @POST("/api/v1/payments/test-success")
    Call<ResponseBody> simulateSuccess(
            @Query("packageId") Long packageId
    );
}
