package com.example.spotify_wrapped;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.spotify_wrapped.databinding.ActivityLoginBinding;
import com.example.spotify_wrapped.login.AccessTokenResponse;
import com.example.spotify_wrapped.login.SpotifyAPIService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "3b801cbc275249a6be39b9ac60b47962";
    private static final String CLIENT_SECRET = "d92f1c9bd7b34f6183ba7a298f5f9066";
    private static final String REDIRECT_URI = "spotifywrapped://callback";
    private static final String AUTH_BASE_URL = "https://accounts.spotify.com/";
    private static final String RESPONSE_TYPE = "code";
    private TextView tokenView;
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private final SpotifyAPIService spotifyApiService = retrofit.create(SpotifyAPIService.class);
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button loginButton = binding.loginButton;
        loginButton.setOnClickListener((v) -> initiateAuthorization());

        tokenView = binding.tokenView;
        tokenView.setText("Access Token Goes Here");

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            Log.d("DATA", data.toString());
        }

        // Check if the intent has data and if it matches the custom URI scheme
        if (data != null && "spotify-wrapped".equals(data.getScheme())) {
            // Extract the callback URL
            String callbackUrl = data.toString();
            // Handle the callback URL as needed
            Log.d("URL NOW", callbackUrl);
        }
    }

    private String buildAuthorizationUrl() {
        String scope = "user-read-private user-read-email";
        String queryString = "";
        try {
            queryString = String.format("response_type=%s&client_id=%s&scope=%s&redirect_uri=%s",
                    URLEncoder.encode(RESPONSE_TYPE, "UTF-8"),
                    URLEncoder.encode(CLIENT_ID, "UTF-8"),
                    URLEncoder.encode(scope, "UTF-8"),
                    URLEncoder.encode(REDIRECT_URI, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            System.err.println("Error encoding query parameters: " + e.getMessage());
        }
        return AUTH_BASE_URL + "authorize?" + queryString;
    }

    private void initiateAuthorization() {
        String authorizationUrl = buildAuthorizationUrl();
        Log.d("URL", authorizationUrl);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(authorizationUrl));
    }

    private void exchangeAuthorizationCode(String authorizationCode) {
        Call<AccessTokenResponse> call = spotifyApiService.getAccessToken(
                "authorization_code",
                authorizationCode,
                REDIRECT_URI,
                CLIENT_ID,
                CLIENT_SECRET
        );

        call.enqueue(new Callback<AccessTokenResponse>() {
            @Override
            public void onResponse(Call<AccessTokenResponse> call, Response<AccessTokenResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("CHECKING", "SUCCESSFUL");
                    AccessTokenResponse accessTokenResponse = response.body();
                    if (accessTokenResponse != null) {
                        String accessToken = accessTokenResponse.getAccessToken();
                        tokenView.setText(accessToken);
                    }
                } else {
                }
            }

            @Override
            public void onFailure(Call<AccessTokenResponse> call, Throwable t) {
                Log.d("CHECKING", "FAILURE");
            }
        });
    }
}