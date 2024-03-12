package com.example.spotify_wrapped.login;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.example.spotify_wrapped.R;
import com.example.spotify_wrapped.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "3b801cbc275249a6be39b9ac60b47962";
    private static final String CLIENT_SECRET = "d92f1c9bd7b34f6183ba7a298f5f9066";
    private static final String REDIRECT_URI = "https://com.example.spotify_wrapped/callback";
    private static final String AUTH_BASE_URL = "https://accounts.spotify.com/";
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

        tokenView = binding.accessToken;
    }

    private String buildAuthorizationUrl() {
        String scope = "user-read-private user-read-email";

        return String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&response_type=code",
                AUTH_BASE_URL, CLIENT_ID, REDIRECT_URI, scope);
    }

    private void initiateAuthorization() {
        String authorizationUrl = buildAuthorizationUrl();
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Check if the URL matches your Redirect URI
                if (url.startsWith("spotify-wrapped://callback")) {
                    handleRedirect(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        webView.loadUrl(authorizationUrl);
        setContentView(webView);
    }

    private void handleRedirect(String url) {
        Uri uri = Uri.parse(url);
        String authorizationCode = uri.getQueryParameter("code");

        if (authorizationCode != null) {
            // Authorization code obtained, exchange it for an access token
            exchangeAuthorizationCode(authorizationCode);
        } else {
            // Handle error or deny from the user
        }
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
            }
        });
    }
}