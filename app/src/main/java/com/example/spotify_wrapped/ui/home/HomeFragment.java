package com.example.spotify_wrapped.ui.home;

import static com.example.spotify_wrapped.MainActivity.REDIRECT_URI;
import static com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.spotify_wrapped.databinding.FragmentHomeBinding;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class HomeFragment extends Fragment {

    private static final String REDIRECT_URI = "wrapped_app://auth";

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        String authToken = "f123b449aaac4b8f814fbaa0395bd403";
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(authToken, AuthorizationResponse.Type.TOKEN, "wrapped_app://auth");
        builder.setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private"});
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(requireActivity(), REQUEST_CODE, request);
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result comes from the Spotify authentication activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);
            if (response.getType() == AuthorizationResponse.Type.TOKEN) {
                String accessToken = response.getAccessToken();
                // Use the access token to make requests to the Spotify Web API
                // You may want to save this access token for future use
            } else if (response.getType() == AuthorizationResponse.Type.ERROR) {
                // Handle authentication error
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}