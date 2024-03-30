package com.example.spotifywrapped;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class BaseActivity extends AppCompatActivity {
    //private HomePageBinding binding;

    //Button recsButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_activity_blank);

        //binding = HomePageBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());
        String accessToken = getIntent().getStringExtra("access-token");
        //binding.tokenView.setText(accessToken);

        Fragment homeFragment = new Home();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.base_container, homeFragment).commit();

        /*recsButton = findViewById(R.id.recButton);

        recsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment recsFragment = new RecsFragment();

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.homeActivity, recsFragment).commit();
            }
        });*/
    }
}