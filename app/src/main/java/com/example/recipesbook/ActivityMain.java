package com.example.recipesbook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipesbook.databinding.ActivityMainBinding;
import com.example.recipesbook.ui_screens.OnBoardingScreen;


public class ActivityMain extends AppCompatActivity {
    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private Context context = ActivityMain.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Intent nextIntent;
            if (isLoggedIn) {
                nextIntent =new Intent(context, OnBoardingScreen.class);                //nextIntent = new Intent(context, MainHomePage.class);
               // Toast.makeText(context, "Logged go to Home Page ", Toast.LENGTH_SHORT).show();
            } else {
                nextIntent =new Intent(context, OnBoardingScreen.class);
            }
            startActivity(nextIntent);
            finish();
        }).start();




    }
}