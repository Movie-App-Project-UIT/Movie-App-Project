package com.example.pemomovie.api;

import com.example.pemomovie.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(chain -> {
                        okhttp3.Request original = chain.request();
                        okhttp3.Request request = original.newBuilder()
                                .header("ngrok-skip-browser-warning", "true")
                                .build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(logging)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(300, TimeUnit.SECONDS)
                    .writeTimeout(300, TimeUnit.SECONDS)
                    .build();

            com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                    .registerTypeAdapter(com.example.pemomovie.dto.PageResponseDto.class, new com.google.gson.JsonDeserializer<com.example.pemomovie.dto.PageResponseDto<?>>() {
                        @Override
                        public com.example.pemomovie.dto.PageResponseDto<?> deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                            com.example.pemomovie.dto.PageResponseDto<Object> page = new com.example.pemomovie.dto.PageResponseDto<>();
                            if (json.isJsonArray()) {
                                java.lang.reflect.ParameterizedType pType = (java.lang.reflect.ParameterizedType) typeOfT;
                                java.lang.reflect.Type elementType = pType.getActualTypeArguments()[0];
                                java.lang.reflect.Type listType = com.google.gson.reflect.TypeToken.getParameterized(java.util.List.class, elementType).getType();
                                java.util.List<Object> content = context.deserialize(json, listType);
                                page.setContent(content);
                            } else if (json.isJsonObject()) {
                                com.google.gson.Gson defaultGson = new com.google.gson.Gson();
                                return defaultGson.fromJson(json, typeOfT);
                            }
                            return page;
                        }
                    })
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
    
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
