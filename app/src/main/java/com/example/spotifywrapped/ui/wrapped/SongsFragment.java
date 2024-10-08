package com.example.spotifywrapped.ui.wrapped;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    private String accessToken, artistString;

    private Call mCall;

    private JSONObject trackJSON;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.top_songs, container, false);

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

        view.findViewById(R.id.songs_home_button).setBackgroundResource(background);

        accessToken = getArguments().getString("access-token");
        artistString = getArguments().getString("artist-string");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
                    String jsonString = response.body().string();

                    if (mAuth.getCurrentUser() != null) {
                        saveWrapped(jsonString);
                    }

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

    public void saveWrapped(String wrappedString) {
        FirebaseUser user = mAuth.getCurrentUser();
        DocumentReference userRef = db.collection("users").document(user.getUid());
        String date = getDateAsString();
        WrappedEntry curEntry = new WrappedEntry(date, artistString, wrappedString);
        userRef.update("wrappedEntries.entry" + System.currentTimeMillis(), curEntry).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("SUCCESSFULLY ADDED", wrappedString);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("COULD NOT ADD", "Error adding document", e);
                    }
                });
    }

    private void parseTopTracks(JSONObject jsonObject) throws JSONException {
        JSONArray items = jsonObject.getJSONArray("items");
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