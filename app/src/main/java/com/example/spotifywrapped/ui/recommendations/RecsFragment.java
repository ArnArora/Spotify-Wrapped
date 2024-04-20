package com.example.spotifywrapped.ui.recommendations;

import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecsFragment extends Fragment {

    private RecsViewModel mViewModel;

    public static final String CLIENT_ID = "e491319d4b474c5ea52ce46ced1edad1";
    public static final String REDIRECT_URI = "SPOTIFY-SDK://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode;
    private Call mCall;


    private TextView tokenTextView, codeTextView, profileTextView;
    private ImageButton homeButton;

    private String accessToken;

    private JSONObject recsJSON;
    private JSONObject imagesJSON;

    private String[] imageUrls = new String[5];
    private int index = 0;

    Handler mainHandler = new Handler();
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recs_artists, container, false);

        int background = R.drawable.green;

        RelativeLayout layout = view.findViewById(R.id.content_container);

        layout.setBackgroundResource(background);

        view.findViewById(R.id.recs_home_button).setBackgroundResource(background);

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

        try {
            getArtistRecs();
        } catch (IOException e) {
            Log.d("JSON", "Failed to parse data--: " + e);
            Toast.makeText(getContext(), "Failed to parse data",
                    Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    public void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    public void sendGetRequest(String url, int num) {
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


                    if (num == 0) {
                        recsJSON = jsonObject;
                        //parseTopArtists(artistJSON);

                        //parseTopTracks(trackJSON);
                        parseArtistRecs(recsJSON);
                    } else if (num == 1) {
                        imagesJSON = jsonObject;

                        //parseArtistId(imagesJSON);
                    }
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data**: " + e);
                }
            }
        });
    }

    public void getArtistRecs() throws IOException {
        sendGetRequest("recommendations?limit=5&seed_artists=4NHQUGzhtTLFvgF5SZesLK", 0);
    }

    /*public void getArtistImage(String id) throws IOException {
        sendGetRequest("artists/" + id, 1);
    }*/

    private void parseArtistRecs(JSONObject jsonObject) throws JSONException {
        JSONArray items = jsonObject.getJSONArray("tracks");
        String[] recs = new String[items.length()];
        //String[] urls = new String[items.length()];
        String[] imageUrls = new String[items.length()];
        for (int i = 0; i < items.length(); i++) {
            //tracks[i] = items.getJSONObject(i).getString("name");
            recs[i] = items.getJSONObject(i).getJSONArray("artists").getJSONObject(0).getString("name");
            imageUrls[i] = items.getJSONObject(i).getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
            //urls[i] = items.getJSONObject(i).getString("preview_url");
            //System.out.println(ids[i]);

        }
        //populateTracksGrid(tracks, urls);
        /*try {
            getArtistImage(ids[0]);
        } catch (IOException e) {
            Log.d("JSON", "Failed to parse data--: " + e);
            Toast.makeText(getContext(), "Failed to parse data",
                    Toast.LENGTH_SHORT).show();
        }*/


        populateArtistRecsGrid(recs, imageUrls);
    }

    /*private void parseArtistId(JSONObject jsonObject) throws JSONException {
        if (index < 5) {
            imageUrls[index] = jsonObject.getJSONArray("images").getJSONObject(0).getString("url");
        }

        index++;

        if (index >= 5) {
            populateArtistImages(imageUrls);
        }
    }*/

    private void populateArtistRecsGrid(String[] recs, String[] imageUrls) {
        new FetchImage(imageUrls).start();

        TextView artistRecOne = getView().findViewById(R.id.artistRecOne);
        artistRecOne.setText(recs[0]);

        TextView artistRecTwo = getView().findViewById(R.id.artistRecTwo);
        artistRecTwo.setText(recs[1]);

        TextView artistRecThree = getView().findViewById(R.id.artistRecThree);
        artistRecThree.setText(recs[2]);

        TextView artistRecFour = getView().findViewById(R.id.artistRecFour);
        artistRecFour.setText(recs[3]);

        TextView artistRecFive = getView().findViewById(R.id.artistRecFive);
        artistRecFive.setText(recs[4]);
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

                    ImageView imageOne = getView().findViewById(R.id.artistRec1);
                    imageOne.setImageBitmap(bitmapOne);

                    ImageView imageTwo = getView().findViewById(R.id.artistRec2);
                    imageTwo.setImageBitmap(bitmapTwo);

                    ImageView imageThree = getView().findViewById(R.id.artistRec3);
                    imageThree.setImageBitmap(bitmapThree);

                    ImageView imageFour = getView().findViewById(R.id.artistRec4);
                    imageFour.setImageBitmap(bitmapFour);

                    ImageView imageFive = getView().findViewById(R.id.artistRec5);
                    imageFive.setImageBitmap(bitmapFive);
                }
            });

        }

    }

}