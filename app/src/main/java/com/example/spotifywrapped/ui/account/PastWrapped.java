package com.example.spotifywrapped.ui.account;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.BaseActivity;
import com.example.spotifywrapped.Home;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.ui.account.AccountFragment;
import com.example.spotifywrapped.ui.recommendations.RecsFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PastWrapped extends Fragment implements MediaPlayer.OnPreparedListener {
    private ImageButton homeButton;
    private View view;

    private ImageButton nextButton;
    private MediaPlayer mediaPlayer;
    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.top_artists, container, false);

        int background = R.drawable.green;

        RelativeLayout layout = view.findViewById(R.id.content_container);
        layout.setBackgroundResource(background);

        view.findViewById(R.id.vector_ek4).setBackgroundResource(background);

        String artistString = getArguments().getString("artist-string");
        String trackString = getArguments().getString("track-string");
        homeButton = view.findViewById(R.id.vector_ek4);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        nextButton = view.findViewById(R.id.artists_next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new PastSongs();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, newFragment);
                fm.addToBackStack(null);

                Bundle bundle = new Bundle();
                bundle.putString("track-string", trackString);
                newFragment.setArguments(bundle);

                fm.commit();
            }
        });

        try {
            parseTopArtists(artistString);
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Cannot parse data", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void parseTopArtists(String jsonString) throws JSONException {
        JSONObject artistJSON = null;
        try {
            final JSONObject jsonObject = new JSONObject(jsonString.substring(jsonString.indexOf("{"), jsonString.lastIndexOf("}") + 1));
            artistJSON = jsonObject;
        } catch (JSONException e) {
            Log.d("JSON", "Failed to parse data**: " + e);
        }
        JSONArray items = artistJSON.getJSONArray("items");
        String[] artists = new String[items.length()];
        String[] imageUrls = new String[items.length()];

        for (int i = 0; i < items.length(); i++) {
            artists[i] = items.getJSONObject(i).getString("name");
            imageUrls[i] = items.getJSONObject(i).getJSONArray("images").getJSONObject(0).getString("url");
        }
        populateArtistsGrid(artists, imageUrls);
    }

    private void populateArtistsGrid(String[] artists, String[] urls) {
        new FetchImage(urls).start();

        TextView artistOne = view.findViewById(R.id.artistOne);
        artistOne.setText(artists[0]);

        TextView artistTwo = view.findViewById(R.id.artistTwo);
        artistTwo.setText(artists[1]);

        TextView artistThree = view.findViewById(R.id.artistThree);
        artistThree.setText(artists[2]);

        TextView artistFour = view.findViewById(R.id.artistFour);
        artistFour.setText(artists[3]);

        TextView artistFive = view.findViewById(R.id.artistFive);
        artistFive.setText(artists[4]);
    }

    public String getDateAsString() {
        String pattern = "MM/dd/yyyy HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);
        Log.d("DATE", todayAsString);
        return todayAsString;
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

                    ImageView imageOne = getView().findViewById(R.id.artistImageOne);
                    imageOne.setImageBitmap(bitmapOne);

                    ImageView imageTwo = getView().findViewById(R.id.artistImageTwo);
                    imageTwo.setImageBitmap(bitmapTwo);

                    ImageView imageThree = getView().findViewById(R.id.artistImageThree);
                    imageThree.setImageBitmap(bitmapThree);

                    ImageView imageFour = getView().findViewById(R.id.artistImageFour);
                    imageFour.setImageBitmap(bitmapFour);

                    ImageView imageFive = getView().findViewById(R.id.artistImageFive);
                    imageFive.setImageBitmap(bitmapFive);
                }
            });

        }

    }
}