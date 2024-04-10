package com.example.spotifywrapped.ui.wrapped;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.Home;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.ui.account.AccountFragment;
import com.example.spotifywrapped.ui.recommendations.RecsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SongsFragment extends Fragment implements MediaPlayer.OnPreparedListener {

    private ImageButton homeButton;

    private ImageButton nextButton;
    private ImageButton prevButton;

    private MediaPlayer mediaPlayer;

    private final OkHttpClient mOkHttpClient = WrappedFragment.mOkHttpClient;

    private String accessToken;

    private Call mCall;

    private JSONObject trackJSON;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.top_songs, container, false);

        accessToken = getArguments().getString("access-token");

        homeButton = view.findViewById(R.id.songs_home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new Home();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, newFragment);
                fm.addToBackStack(null);

                Bundle bundle = new Bundle();
                bundle.putString("access-token", accessToken);
                newFragment.setArguments(bundle);

                fm.commit();
            }
        });

        prevButton = view.findViewById(R.id.songs_back_button);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(0);
            }
        });

        /*nextButton = view.findViewById(R.id.songs_next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(1);
            }
        });*/

        try {
            getTopTracks();
        } catch (IOException e) {
            Log.d("JSON", "Failed to parse data--: " + e);
            Toast.makeText(getContext(), "Failed to parse data",
                    Toast.LENGTH_SHORT).show();
        }

        return view;

    }

    public void sendGetRequest(String url) {
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
                    //System.out.println(response.body().string());
                    //final JSONObject jsonObject = new JSONObject(response.body().string());
                    String jsonString = response.body().string();

                    //System.out.println(jsonString);

                    final JSONObject jsonObject = new JSONObject(jsonString.substring(jsonString.indexOf("{"), jsonString.lastIndexOf("}") + 1));

                    trackJSON = jsonObject;
                    //parseTopArtists(artistJSON);

                    parseTopTracks(trackJSON);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data**: " + e);
                }
            }
        });
    }

    public void getTopTracks() throws IOException {
        sendGetRequest("me/top/tracks?time_range=medium_term&limit=10");
    }

    private void parseTopTracks(JSONObject jsonObject) throws JSONException {
        JSONArray items = jsonObject.getJSONArray("items");
        String[] tracks = new String[items.length()];
        String[] urls = new String[items.length()];
        for (int i = 0; i < items.length(); i++) {
            tracks[i] = items.getJSONObject(i).getString("name");
            urls[i] = items.getJSONObject(i).getString("preview_url");
        }
        populateTracksGrid(tracks, urls);
    }

    private void populateTracksGrid(String[] tracks, String[] urls) {
        /*GridLayout tracksGrid = getView().findViewById(R.id.top_tracks);
        for (int i = 0; i < tracksGrid.getChildCount(); i++) {
            TextView curView = (TextView) tracksGrid.getChildAt(i);
            if (urls[i] == null) {
                curView.setClickable(false);
            }
            final String URL = urls[i];
            curView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playTrack(URL);
                }
            });
            curView.setText(tracks[i]);
        }*/

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView songOne = getView().findViewById(R.id.songOne);
                setClickableTrack(songOne, urls[0], tracks[0]);

                TextView songTwo = getView().findViewById(R.id.songTwo);
                setClickableTrack(songTwo, urls[1], tracks[1]);

                TextView songThree = getView().findViewById(R.id.songThree);
                setClickableTrack(songThree, urls[2], tracks[2]);

                TextView songFour = getView().findViewById(R.id.songFour);
                setClickableTrack(songFour, urls[3], tracks[3]);

                TextView songFive = getView().findViewById(R.id.songFive);
                setClickableTrack(songFive, urls[4], tracks[4]);
            }
        });
    }

    private void setClickableTrack(TextView curView, String url, String track) {
        if (url == null) {
            curView.setClickable(false);
        }
        final String URL = url;
        curView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTrack(URL);
            }
        });
        curView.setText(track);

    }

    private void playTrack(String url) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
        );
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Cannot load song", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    public void startFragment(int num) {
        Fragment newFragment = null;
        if (num == 0) {
            newFragment = new WrappedFragment();
        } else if (num == 1) {
            newFragment = new SummaryFragment();
        }

        FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
        fm.replace(R.id.base_container, newFragment);
        fm.addToBackStack(null);

        Bundle bundle = new Bundle();
        bundle.putString("access-token", accessToken);
        newFragment.setArguments(bundle);

        fm.commit();
    }
}