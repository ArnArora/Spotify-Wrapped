package com.example.spotifywrapped.ui.wrapped;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WrappedFragment extends Fragment implements MediaPlayer.OnPreparedListener {
    private WrappedViewModel mViewModel;
    public static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String accessToken;
    private Call mCall;
    //private Button homeButton;
    private ImageButton homeButton;

    private ImageButton nextButton;
    private MediaPlayer mediaPlayer;
    private JSONObject artistJSON;

    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;



    public static WrappedFragment newInstance() {
        return new WrappedFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_artists, container, false);

        int background = R.drawable.green;
        String titleText = "Your Spotify Wrapped - Enjoy!";
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        if (month == 1 && dayOfMonth == 14) {
            background = R.drawable.heart_background;
            titleText = "Your Spotify Wrapped - Happy Valentine's Day!";
        }
        if (month == 9 && dayOfMonth == 31) {
            background = R.drawable.ghost;
            titleText = "Your Spotify Wrapped - Happy Halloween!";
        }
        if (month == 11 && dayOfMonth == 25) {
            background = R.drawable.winter;
            titleText = "Your Spotify Wrapped - Merry Christmas!";
        }

        RelativeLayout layout = view.findViewById(R.id.content_container);
        layout.setBackgroundResource(background);

        view.findViewById(R.id.vector_ek4).setBackgroundResource(background);

        //TextView title = view.findViewById(R.id.text_welcome);
        //title.setText(titleText);

        accessToken = getArguments().getString("access-token");

        homeButton = view.findViewById(R.id.vector_ek4);
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

        nextButton = view.findViewById(R.id.artists_next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new SongsFragment();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, newFragment);
                fm.addToBackStack(null);

                Bundle bundle = new Bundle();
                bundle.putString("access-token", accessToken);
                newFragment.setArguments(bundle);

                fm.commit();
            }
        });

        try {
            getTopArtists();
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

                    artistJSON = jsonObject;
                    //getTopTracks();

                    parseTopArtists(artistJSON);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data**: " + e);
                }
            }
        });
    }

    public void getTopArtists() throws IOException {
        sendGetRequest("me/top/artists?time_range=medium_term&limit=5");
    }

    private void parseTopArtists(JSONObject jsonObject) throws JSONException {
        JSONArray items = jsonObject.getJSONArray("items");
        String[] artists = new String[items.length()];
        String[] imageUrls = new String[items.length()];

        for (int i = 0; i < items.length(); i++) {
            artists[i] = items.getJSONObject(i).getString("name");
            imageUrls[i] = items.getJSONObject(i).getJSONArray("images").getJSONObject(0).getString("url");
            System.out.println(artists[i]);
        }
        populateArtistsGrid(artists, imageUrls);
    }

    private void populateArtistsGrid(String[] artists, String[] urls) {
        /*LinearLayout artistsGrid = getView().findViewById(R.id.top_artists);
        for (int i = 0; i < artistsGrid.getChildCount(); i++) {
            TextView curView = (TextView) artistsGrid.getChildAt(i);
            curView.setText(artists[i]);
        }*/
        new FetchImage(urls).start();

        TextView artistOne = getView().findViewById(R.id.artistOne);
        artistOne.setText(artists[0]);

        TextView artistTwo = getView().findViewById(R.id.artistTwo);
        artistTwo.setText(artists[1]);

        TextView artistThree = getView().findViewById(R.id.artistThree);
        artistThree.setText(artists[2]);

        TextView artistFour = getView().findViewById(R.id.artistFour);
        artistFour.setText(artists[3]);

        TextView artistFive = getView().findViewById(R.id.artistFive);
        artistFive.setText(artists[4]);
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