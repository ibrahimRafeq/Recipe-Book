package com.example.recipesbook.ui_screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityMainHomePageBinding;
import com.example.recipesbook.fragment.AddFragment;
import com.example.recipesbook.fragment.HomeFragment;

public class MainHomePage extends AppCompatActivity {
    ActivityMainHomePageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainHomePageBinding.inflate(getLayoutInflater());
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
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_add) {
                loadFragment(new AddFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                // الانتقال إلى شاشة ProfiloActivity بدلاً من الفراغمنت
                startActivity(new Intent(MainHomePage.this, ProfiloActivity.class));
                return false; // نرجع false حتى لا يتم تظليل أيقونة البروفايل في الأسفل إذا كنت ستنتقل لنشاط جديد
            }
            return false;
        });
    }
}