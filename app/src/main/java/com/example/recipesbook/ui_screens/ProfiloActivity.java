package com.example.recipesbook.ui_screens;

import android.os.Bundle;
import android.widget.Toast;

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

    }
}