package com.example.spotifywrapped.ui.recommendations;

import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.Home;
import com.example.spotifywrapped.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recs_artists, container, false);

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

                    recsJSON = jsonObject;
                    //parseTopArtists(artistJSON);

                    //parseTopTracks(trackJSON);
                    parseArtistRecs(recsJSON);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data**: " + e);
                }
            }
        });
    }

    public void getArtistRecs() throws IOException {
        sendGetRequest("recommendations?limit=5&seed_artists=4NHQUGzhtTLFvgF5SZesLK");
    }

    private void parseArtistRecs(JSONObject jsonObject) throws JSONException {
        JSONArray items = jsonObject.getJSONArray("tracks");
        String[] recs = new String[items.length()];
        //String[] urls = new String[items.length()];
        for (int i = 0; i < items.length(); i++) {
            //tracks[i] = items.getJSONObject(i).getString("name");
            recs[i] = items.getJSONObject(i).getJSONArray("artists").getJSONObject(0).getString("name");
            //urls[i] = items.getJSONObject(i).getString("preview_url");
            System.out.println(recs[i]);
        }
        //populateTracksGrid(tracks, urls);
        populateArtistRecsGrid(recs);
    }

    private void populateArtistRecsGrid(String[] recs) {
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

}