package com.example.spotifywrapped.ui.account;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PastSongs extends Fragment implements MediaPlayer.OnPreparedListener {

    private ImageButton prevButton;
    private ImageButton homeButton;
    private View view;

    private MediaPlayer mediaPlayer;

    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.top_songs, container, false);

        int background = R.drawable.green;

        RelativeLayout layout = view.findViewById(R.id.content_container);
        layout.setBackgroundResource(background);

        view.findViewById(R.id.songs_home_button).setBackgroundResource(background);

        String jsonString = getArguments().getString("track-string");

        prevButton = view.findViewById(R.id.songs_back_button);
        homeButton = view.findViewById(R.id.songs_home_button);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        try {
            parseTopTracks(jsonString);
        } catch (JSONException e) {
            Log.d("JSON", "Failed to parse data--: " + e);
            Toast.makeText(getContext(), "Failed to parse data",
                    Toast.LENGTH_SHORT).show();
        }

        return view;

    }

    private void parseTopTracks(String jsonString) throws JSONException {
        JSONObject trackJSON = null;
        try {
            final JSONObject jsonObject = new JSONObject(jsonString.substring(jsonString.indexOf("{"), jsonString.lastIndexOf("}") + 1));
            trackJSON = jsonObject;
        } catch (JSONException e) {
            Log.d("JSON", "Failed to parse data**: " + e);
        }
        JSONArray items = trackJSON.getJSONArray("items");
        String[] tracks = new String[items.length()];
        String[] urls = new String[items.length()];
        String[] imageUrls = new String[items.length()];

        for (int i = 0; i < items.length(); i++) {
            tracks[i] = items.getJSONObject(i).getString("name");
            urls[i] = items.getJSONObject(i).getString("preview_url");
            imageUrls[i] = items.getJSONObject(i).getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
        }
        populateTracksGrid(tracks, urls, imageUrls);
    }

    private void populateTracksGrid(String[] tracks, String[] urls, String[] imageUrls) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new FetchImage(imageUrls).start();

                TextView songOne = view.findViewById(R.id.songOne);
                setClickableTrack(songOne, urls[0], tracks[0]);

                TextView songTwo = view.findViewById(R.id.songTwo);
                setClickableTrack(songTwo, urls[1], tracks[1]);

                TextView songThree = view.findViewById(R.id.songThree);
                setClickableTrack(songThree, urls[2], tracks[2]);

                TextView songFour = view.findViewById(R.id.songFour);
                setClickableTrack(songFour, urls[3], tracks[3]);

                TextView songFive = view.findViewById(R.id.songFive);
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
        super.onDestroy();
    }

    private class FetchImage extends Thread {
        String[] urls;
        Bitmap bitmapOne;
        Bitmap bitmapTwo;
        Bitmap bitmapThree;
        Bitmap bitmapFour;
        Bitmap bitmapFive;

        FetchImage(String[] urls) {
            this.urls = urls;
        }

        public void run() {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog = new ProgressDialog(getContext());
                    progressDialog.setMessage("Getting images...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                }
            });

            InputStream inputStream = null;
            try {
                inputStream = new java.net.URL(urls[0]).openStream();
                bitmapOne = BitmapFactory.decodeStream(inputStream);

                inputStream = new java.net.URL(urls[1]).openStream();
                bitmapTwo = BitmapFactory.decodeStream(inputStream);

                inputStream = new java.net.URL(urls[2]).openStream();
                bitmapThree = BitmapFactory.decodeStream(inputStream);

                inputStream = new java.net.URL(urls[3]).openStream();
                bitmapFour = BitmapFactory.decodeStream(inputStream);

                inputStream = new java.net.URL(urls[4]).openStream();
                bitmapFive = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    ImageView imageOne = getView().findViewById(R.id.songsImageOne);
                    imageOne.setImageBitmap(bitmapOne);

                    ImageView imageTwo = getView().findViewById(R.id.songsImageTwo);
                    imageTwo.setImageBitmap(bitmapTwo);

                    ImageView imageThree = getView().findViewById(R.id.songsImageThree);
                    imageThree.setImageBitmap(bitmapThree);

                    ImageView imageFour = getView().findViewById(R.id.songsImageFour);
                    imageFour.setImageBitmap(bitmapFour);

                    ImageView imageFive = getView().findViewById(R.id.songsImageFive);
                    imageFive.setImageBitmap(bitmapFive);
                }
            });

        }

    }
}