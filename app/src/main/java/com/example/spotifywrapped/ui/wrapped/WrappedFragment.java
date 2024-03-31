package com.example.spotifywrapped.ui.wrapped;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.Home;
import com.example.spotifywrapped.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WrappedFragment extends Fragment {
    private WrappedViewModel mViewModel;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String accessToken;
    private Call mCall;
    private GridLayout tracksGrid;
    private LinearLayout artistsGrid;
    private Button homeButton;
    public static WrappedFragment newInstance() {
        return new WrappedFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wrapped_home, container, false);
        accessToken = getArguments().getString("access-token");
        tracksGrid = view.findViewById(R.id.top_tracks);
        artistsGrid = view.findViewById(R.id.top_artists);
        try {
            getTopArtists();
        } catch (IOException e) {
            Log.d("JSON", "Failed to parse data: " + e);
            Toast.makeText(getContext(), "Failed to parse data",
                    Toast.LENGTH_SHORT).show();
        }
        homeButton = view.findViewById(R.id.wrapped_home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Home home = new Home();
                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, home).commit();
            }
        });
        return view;
    }

    public void sendGetRequest(int num, String url) {
        if (accessToken == null) {
            Toast.makeText(getContext(), "Cannot retrieve user data right now", Toast.LENGTH_SHORT).show();
            return;
        }
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/" + url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(getContext(), "Failed to fetch data",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    if (num == 0) {
                        parseTopTracks(jsonObject);
                    }
                    if (num == 1) {
                        parseTopArtists(jsonObject);
                        getTopTracks();
                    }
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                }
            }
        });
    }

    public void getTopTracks() throws IOException {
        sendGetRequest(0, "me/top/tracks?time_range=medium_term&limit=10");
    }

    public void getTopArtists() throws IOException {
        sendGetRequest(1, "me/top/artists?time_range=medium_term&limit=5");
    }

    private void parseTopTracks(JSONObject jsonObject) throws JSONException {
        JSONArray items = jsonObject.getJSONArray("items");
        String[] tracks = new String[items.length()];
        for (int i = 0; i < items.length(); i++) {
            tracks[i] = items.getJSONObject(i).getString("name");
        }
        populateTracksGrid(tracks);
    }

    private void parseTopArtists(JSONObject jsonObject) throws JSONException {
        JSONArray items = jsonObject.getJSONArray("items");
        String[] artists = new String[items.length()];
        for (int i = 0; i < items.length(); i++) {
            artists[i] = items.getJSONObject(i).getString("name");
        }
        populateArtistsGrid(artists);
    }

    private void populateArtistsGrid(String[] artists) {
        for (int i = 0; i < artistsGrid.getChildCount(); i++) {
            TextView curView = (TextView) artistsGrid.getChildAt(i);
            curView.setText(artists[i]);
        }
    }

    private void populateTracksGrid(String[] tracks) {
        for (int i = 0; i < tracksGrid.getChildCount(); i++) {
            TextView curView = (TextView) tracksGrid.getChildAt(i);
            curView.setText(tracks[i]);
        }
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    public void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}
