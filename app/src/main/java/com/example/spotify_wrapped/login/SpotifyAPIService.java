package com.example.spotify_wrapped.login;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SpotifyAPIService {
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("api/token")
    Call<AccessTokenResponse> getAccessToken(
            @Field("grant_type") String grantType,
            @Field("code") String authorizationCode,
            @Field("redirect_uri") String redirectUri,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret
    );
}
