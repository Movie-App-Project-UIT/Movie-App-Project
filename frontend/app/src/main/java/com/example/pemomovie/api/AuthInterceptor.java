package com.example.pemomovie.api;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            try {
                // Fetch the ID token synchronously
                Task<GetTokenResult> task = currentUser.getIdToken(false);
                GetTokenResult result = Tasks.await(task);
                String token = result.getToken();
                
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return chain.proceed(requestBuilder.build());
    }
}
