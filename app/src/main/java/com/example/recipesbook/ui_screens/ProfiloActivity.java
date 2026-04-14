package com.example.recipesbook.ui_screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityProfiloBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProfiloActivity extends AppCompatActivity {

    private ActivityProfiloBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfiloBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null) {
            loadUserData();
        }
    }

    private void loadUserData() {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String imageUrl = documentSnapshot.getString("image");

                        binding.userNameTV.setText(name != null ? name : "User Name");
                        binding.emailTV.setText(email != null ? email : "No Email");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.user_image)
                                    .error(R.drawable.user_image)
                                    .into(binding.imgProfile3);
                        } else {
                            binding.imgProfile3.setImageResource(R.drawable.user_image);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        binding.btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(this, FavoritesActivity.class));
        });

        binding.btnMyRecipes.setOnClickListener(v -> {
            startActivity(new Intent(this, MyRecipesActivity.class));
        });

        binding.bntSetting.setOnClickListener(v -> {
            binding.settingsDrawer.openDrawer(Gravity.START);
        });

        binding.btnHelpCenter.setOnClickListener(v -> {
            Toast.makeText(this, "Help Center coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnResetPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        auth.signOut();
                        Intent intent = new Intent(this, LoginScreen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
}