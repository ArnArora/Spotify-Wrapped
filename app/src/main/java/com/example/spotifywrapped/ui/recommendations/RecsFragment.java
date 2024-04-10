package com.example.spotifywrapped.ui.recommendations;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifywrapped.Home;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.databinding.FragmentRecsBinding;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

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

    private FragmentRecsBinding binding;

    private ImageButton homeButton;

    private String accessToken;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recs_songs, container, false);

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

        return view;
    }


}