package com.example.spotifywrapped.ui.wrapped;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.spotifywrapped.Home;
import com.example.spotifywrapped.R;
import com.example.spotifywrapped.ui.account.AccountFragment;
import com.example.spotifywrapped.ui.recommendations.RecsFragment;

public class SongsFragment extends Fragment {

    private ImageButton homeButton;

    private ImageButton nextButton;
    private ImageButton prevButton;

    private String accessToken;
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

        nextButton = view.findViewById(R.id.songs_next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(1);
            }
        });

        return view;

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