package com.example.recipesbook.ui_screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipesbook.databinding.ActivityProfiloBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProfiloActivity extends AppCompatActivity {

    ActivityProfiloBinding binding;
    FirebaseAuth auth;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfiloBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String country = documentSnapshot.getString("country");
                        String imageUrl = documentSnapshot.getString("image");

                        binding.userNameTV.setText(name);
                        binding.emailTV.setText(email);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get()
                                    .load(imageUrl)
                                    .into(binding.imgProfile3);
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Data loading failed", Toast.LENGTH_SHORT).show();
                });

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfiloActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(ProfiloActivity.this, LoginScreen.class));
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();

            }
        });


        binding.bntSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.settingsDrawer.openDrawer(Gravity.LEFT);
            }
        });
    }
}