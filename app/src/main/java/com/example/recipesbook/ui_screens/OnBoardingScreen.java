package com.example.recipesbook.ui_screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityOnBoardingScreenBinding;

public class OnBoardingScreen extends AppCompatActivity {
    private ActivityOnBoardingScreenBinding binding;
    private Context context = OnBoardingScreen.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnBoardingScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.startButton.setOnClickListener(v -> {
            startActivity(new Intent(context, LoginScreen.class));
            finish();
        });


    }
}