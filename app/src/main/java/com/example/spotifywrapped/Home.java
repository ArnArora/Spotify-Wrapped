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

import com.example.spotifywrapped.ui.account.AccountFragment;
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
    Button goToPastWrapped;
    private String accessToken;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.home_page, container, false);

        goToRecs = view.findViewById(R.id.recButton);
        goToWrapped = view.findViewById(R.id.wrappedButton);
        goToHoliday = view.findViewById(R.id.holidayButton);
        goToPastWrapped = view.findViewById(R.id.past_wrapped);

        accessToken = getArguments().getString("access-token");

        goToRecs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(1);
            }
        });

        goToWrapped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(0);
            }
        });

        goToHoliday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(2);
            }
        });

        goToPastWrapped.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(3);
            }
        });

        return view;
    }

    public void startFragment(int num) {
        Fragment newFragment = null;
        if (num == 0) {
            newFragment = new WrappedFragment();
        } else if (num == 1) {
            newFragment = new RecsFragment();
        } else if (num == 2){
            newFragment = new WrappedFragment();
        } else if (num == 3) {
            newFragment = new AccountFragment();
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