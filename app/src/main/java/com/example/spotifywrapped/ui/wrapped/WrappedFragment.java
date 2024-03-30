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

    public String fetchProfile(String token) throws Exception {
        // TODO: Implement fetching profile data from Spotify API
        return null;
    }

    public void populateUI(String profile) {
        // TODO: Update UI with profile data
    }
}
