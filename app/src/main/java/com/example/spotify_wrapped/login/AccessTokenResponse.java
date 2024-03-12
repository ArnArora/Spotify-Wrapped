package com.example.spotify_wrapped.login;

import com.google.gson.annotations.SerializedName;

public class AccessTokenResponse {
    @SerializedName("access_token")
    private String accessToken;

    // Add other necessary fields

    public String getAccessToken() {
        return accessToken;
    }
}
