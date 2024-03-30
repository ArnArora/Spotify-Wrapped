package com.example.spotifywrapped.ui.wrapped;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.spotifywrapped.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

public class WrappedFragment extends Fragment {
    public static final String CLIENT_ID = "3b801cbc275249a6be39b9ac60b47962";
    private WrappedViewModel mViewModel;

    public static WrappedFragment newInstance() {
        return new WrappedFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wrapped_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WrappedViewModel.class);

        String code = null; // Set the code you get from the Spotify redirect here
        if (code == null || code.isEmpty()) {
            try {
                redirectToAuthCodeFlow(CLIENT_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                String accessToken = getAccessToken(CLIENT_ID, code);
                String profile = fetchProfile(accessToken);
                populateUI(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void redirectToAuthCodeFlow(String clientId) throws Exception {
        String verifier = generateCodeVerifier(128);
        String challenge = generateCodeChallenge(verifier);

        // Assuming localStorage equivalent is used for storing verifier
        // localStorage.setItem("verifier", verifier);

        StringBuilder params = new StringBuilder();
        params.append("client_id=").append(clientId)
                .append("&response_type=code")
                .append("&redirect_uri=http://localhost:5173/callback")
                .append("&scope=user-top-read")
                .append("&code_challenge_method=S256")
                .append("&code_challenge=").append(challenge);

        String url = "https://accounts.spotify.com/authorize?" + params.toString();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    public String generateCodeVerifier(int length) {
        StringBuilder text = new StringBuilder();
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            text.append(possible.charAt(random.nextInt(possible.length())));
        }
        return text.toString();
    }

    public String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] data = digest.digest(codeVerifier.getBytes());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
        }
    }

    public String getAccessToken(String clientId, String code) throws Exception {
        
        return null;
    }

    public String fetchProfile(String token) throws Exception {
        // TODO: Implement fetching profile data from Spotify API
        return null;
    }

    public void populateUI(String profile) {
        // TODO: Update UI with profile data
    }
}
