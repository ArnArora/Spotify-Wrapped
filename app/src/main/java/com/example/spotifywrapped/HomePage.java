package com.example.spotifywrapped;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spotifywrapped.databinding.HomePageBinding;
import com.example.spotifywrapped.databinding.SignInBinding;

public class HomePage extends AppCompatActivity {
    private HomePageBinding binding;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = HomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String accessToken = getIntent().getStringExtra("access-token");
        binding.tokenView.setText(accessToken);
    }
}
