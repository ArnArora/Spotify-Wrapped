package com.example.spotifywrapped;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.ui.holiday.HolidayFragment;
import com.example.spotifywrapped.ui.recommendations.RecsFragment;
import com.example.spotifywrapped.ui.wrapped.WrappedFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Home extends Fragment {

    Button goToRecs;
    Button goToWrapped;
    Button goToHoliday;
    //Button goToShare;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String accessToken;
    private Call mCall;
    private JSONObject currentJSON;

    public static final String CLIENT_ID = "3b801cbc275249a6be39b9ac60b47962";
    public static final String REDIRECT_URI = "com.example.spotifywrapped://auth";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.home_page, container, false);

        goToRecs = view.findViewById(R.id.recButton);
        goToWrapped = view.findViewById(R.id.wrappedButton);
        goToHoliday = view.findViewById(R.id.holidayButton);
        TextView tokenView = view.findViewById(R.id.tokenView);

        accessToken = getArguments().getString("access-token");
        tokenView.setText(accessToken);

        goToRecs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment recsFragment = new RecsFragment();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, recsFragment).commit();
            }
        });

        goToWrapped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment wrappedFragment = new WrappedFragment();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, wrappedFragment).commit();
            }
        });

        goToHoliday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment holidayFragment = new HolidayFragment();

                FragmentTransaction fm = getActivity().getSupportFragmentManager().beginTransaction();
                fm.replace(R.id.base_container, holidayFragment).commit();
            }
        });

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
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    setJSON(jsonObject);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(getContext(), "Failed to parse data",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setJSON(JSONObject jsonObject) {
        currentJSON = jsonObject;
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