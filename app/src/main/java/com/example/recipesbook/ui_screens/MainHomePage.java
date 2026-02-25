package com.example.recipesbook.ui_screens;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityMainHomePageBinding;
import com.example.recipesbook.fragment.AddFragment;
import com.example.recipesbook.fragment.HomeFragment;
import com.example.recipesbook.fragment.ProfileFragment;

public class MainHomePage extends AppCompatActivity {
    ActivityMainHomePageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpFragment();

    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(binding.container.getId(), fragment)
                .commit();
    }
    private void setUpFragment() {
        loadFragment(new HomeFragment());
        binding.bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                loadFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.nav_add) {
                loadFragment(new AddFragment());
            } else if (item.getItemId() == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
            }
            return true;
        });
    }

}