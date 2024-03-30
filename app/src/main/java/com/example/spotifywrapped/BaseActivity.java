package com.example.spotifywrapped;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class BaseActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_activity_blank);

        String accessToken = getIntent().getStringExtra("access-token");

        Fragment homeFragment = new Home();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.base_container, homeFragment);
        Bundle bundle = new Bundle();
        bundle.putString("access-token", accessToken);
        homeFragment.setArguments(bundle);
        fragmentTransaction.commit();
    }
}