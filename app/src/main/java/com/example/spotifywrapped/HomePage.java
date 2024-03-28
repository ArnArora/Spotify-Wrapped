package com.example.spotifywrapped;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomePage extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        TextView title = findViewById(R.id.homePage);
        title.setText(getIntent().getStringExtra("token"));
    }
}
