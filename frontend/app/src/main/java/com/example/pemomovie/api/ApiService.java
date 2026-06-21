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

    // --- ADMIN API ---
    @GET("/api/v1/admin/movies")
    Call<List<MediaItemDto>> getAllMoviesAdmin();

    @GET("/api/v1/admin/movies/{id}")
    Call<com.example.pemomovie.dto.MediaDetailResponse> getMediaDetailAdmin(@retrofit2.http.Path("id") Long id);

    @retrofit2.http.PUT("/api/v1/admin/movies/{id}/soft-delete")
    Call<Void> softDeleteMovie(@retrofit2.http.Path("id") Long id);

    @GET("/api/v1/admin/movies/preview-tmdb")
    Call<com.example.pemomovie.dto.MediaDetailResponse> previewTmdbMovie(@retrofit2.http.Query("tmdbId") Integer tmdbId);

    @POST("/api/v1/admin/movies")
    Call<com.example.pemomovie.dto.MediaDetailResponse> createMovie(@Body com.example.pemomovie.dto.AdminMovieSaveRequest request);

    @retrofit2.http.PUT("/api/v1/admin/movies/{id}")
    Call<com.example.pemomovie.dto.MediaDetailResponse> updateMovie(@retrofit2.http.Path("id") Long id, @Body com.example.pemomovie.dto.AdminMovieSaveRequest request);

    @GET("/api/v1/admin/categories")
    Call<List<com.example.pemomovie.dto.AdminGenreDto>> getAllCategoriesAdmin();

    @POST("/api/v1/admin/categories")
    Call<com.example.pemomovie.dto.AdminGenreDto> createCategory(@Body java.util.Map<String, String> body);

    @retrofit2.http.PUT("/api/v1/admin/categories/{id}/soft-delete")
    Call<Void> softDeleteCategory(@retrofit2.http.Path("id") Long id);

    @GET("/api/v1/admin/categories/{id}/media")
    Call<List<MediaItemDto>> getMediaInGenre(@retrofit2.http.Path("id") Long id);

    @GET("/api/v1/admin/categories/{id}/media/exclude")
    Call<List<MediaItemDto>> getMediaNotInGenre(@retrofit2.http.Path("id") Long id);

    @POST("/api/v1/admin/categories/{id}/media/{mediaId}")
    Call<Void> addMediaToGenre(@retrofit2.http.Path("id") Long id, @retrofit2.http.Path("mediaId") Long mediaId);

    @retrofit2.http.DELETE("/api/v1/admin/categories/{id}/media/{mediaId}")
    Call<Void> removeMediaFromGenre(@retrofit2.http.Path("id") Long id, @retrofit2.http.Path("mediaId") Long mediaId);

    // --- ADMIN SUBSCRIPTION API ---
    @GET("/api/v1/admin/subscriptions")
    Call<List<com.example.pemomovie.dto.AdminSubscriptionDto>> getAllSubscriptions();

    @POST("/api/v1/admin/subscriptions")
    Call<com.example.pemomovie.dto.AdminSubscriptionDto> createSubscription(@Body com.example.pemomovie.dto.AdminSubscriptionDto request);

    @retrofit2.http.PUT("/api/v1/admin/subscriptions/{id}")
    Call<com.example.pemomovie.dto.AdminSubscriptionDto> updateSubscription(@retrofit2.http.Path("id") Long id, @Body com.example.pemomovie.dto.AdminSubscriptionDto request);

    @retrofit2.http.PUT("/api/v1/admin/subscriptions/{id}/toggle-status")
    Call<Void> toggleSubscriptionStatus(@retrofit2.http.Path("id") Long id);

    @GET("/api/v1/admin/users")
    Call<List<com.example.pemomovie.dto.AdminUserDto>> getUsers(@retrofit2.http.Query("isPremium") Boolean isPremium, @retrofit2.http.Query("search") String search);

    @GET("/api/v1/admin/users/{id}/details")
    Call<com.example.pemomovie.dto.AdminUserDetailDto> getUserDetails(@retrofit2.http.Path("id") Long id);

    @retrofit2.http.PUT("/api/v1/admin/users/{id}/toggle-status")
    Call<Void> toggleUserStatus(@retrofit2.http.Path("id") Long id);

    @POST("/api/v1/admin/subscriptions/{id}/gift")
    Call<Void> giftSubscription(
            @retrofit2.http.Path("id") Long id,
            @Body com.example.pemomovie.dto.GiftSubscriptionRequest request);

    @GET("/api/v1/admin/subscriptions/{id}/gifted-users")
    Call<List<com.example.pemomovie.dto.AdminUserDto>> getGiftedUsers(@retrofit2.http.Path("id") Long id);

    @GET("/api/v1/admin/history")
    Call<List<com.example.pemomovie.dto.AdminHistoryDto>> getAdminHistory();

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
    Call<ResponseBody> simulateSuccess(@Query("packageId") Long packageId);
    @GET("/api/v1/media/{id}/play")
    Call<ResponseBody> getPlayableVideoUrl(@retrofit2.http.Path("id") Long id);

    @GET("/api/v1/lookups/genres")
    Call<List<com.example.pemomovie.dto.GenreDto>> getGenres();

    @GET("/api/v1/lookups/countries")
    Call<List<com.example.pemomovie.dto.CountryDto>> getCountries();

    @GET("/api/v1/lookups/age-ratings")
    Call<List<com.example.pemomovie.dto.AgeRatingDto>> getAgeRatings();

    @GET("/api/v1/media/filter")
    Call<com.example.pemomovie.dto.PageResponseDto<com.example.pemomovie.dto.MediaItemDto>> filterMedia(
            @retrofit2.http.Query("keyword") String keyword,
            @retrofit2.http.Query("genreId") Long genreId,
            @retrofit2.http.Query("countryId") Long countryId,
            @retrofit2.http.Query("ageRatingId") Long ageRatingId,
            @retrofit2.http.Query("releaseYear") Integer releaseYear,
            @retrofit2.http.Query("mediaType") String mediaType,
            @retrofit2.http.Query("sortBy") String sortBy,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("size") int size
    );

    @GET("/api/v1/notifications/{userId}")
    Call<List<com.example.pemomovie.dto.NotificationDto>> getUserNotifications(@retrofit2.http.Path("userId") Long userId);

    @retrofit2.http.PUT("/api/v1/notifications/{id}/read")
    Call<Map<String, String>> markNotificationAsRead(@retrofit2.http.Path("id") Long id);

    @POST("/api/v1/subscriptions/claim-gift/{notificationId}")
    Call<Map<String, String>> claimGift(@retrofit2.http.Path("notificationId") Long notificationId);

    @GET("/api/v1/subscriptions/plans")
    Call<List<com.example.pemomovie.dto.AdminSubscriptionDto>> getActivePlans();

    @GET("/api/v1/reviews/media/{mediaId}")
    Call<List<com.example.pemomovie.dto.ReviewResponseDto>> getReviewsByMedia(@retrofit2.http.Path("mediaId") Long mediaId);

    @Multipart
    @POST("/api/v1/admin/upload/video")
    Call<ResponseBody> uploadVideoAdmin(@Part MultipartBody.Part file);

    @Multipart
    @POST("/api/v1/admin/upload/subtitle")
    Call<ResponseBody> uploadSubtitleAdmin(@Part MultipartBody.Part file);

}
