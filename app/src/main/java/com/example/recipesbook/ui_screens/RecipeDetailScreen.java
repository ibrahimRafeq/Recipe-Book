package com.example.recipesbook.ui_screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityRecipeDetailScreenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class RecipeDetailScreen extends AppCompatActivity {

    private ActivityRecipeDetailScreenBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String recipeId;
    private boolean isFavorite = false;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recipeId = getIntent().getStringExtra("id");

        if (recipeId != null) {
            loadRecipeDetails();
            checkIfFavorite();
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnFavorite.setOnClickListener(v -> toggleFavorite());

        binding.btnWatchVideo.setOnClickListener(v -> {
            if (videoUrl != null && !videoUrl.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(intent);
            } else {
                Toast.makeText(this, "No video available for this recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecipeDetails() {
        firestore.collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        binding.tvDetailTitle.setText(documentSnapshot.getString("title"));
                        binding.tvDetailCategory.setText(documentSnapshot.getString("category"));
                        binding.tvDetailPublisherName.setText(documentSnapshot.getString("publisherName"));
                        binding.tvDetailIngredients.setText(documentSnapshot.getString("ingredients"));
                        binding.tvDetailSteps.setText(documentSnapshot.getString("steps"));
                        videoUrl = documentSnapshot.getString("videoUrl");

                        // Since we don't have a dynamic image URL in the model yet, using a placeholder or a default
                        binding.imgRecipeDetail.setImageResource(R.drawable.img);
                        
                        // If there was an image URL in Firestore, we would use Picasso:
                        // String imageUrl = documentSnapshot.getString("imageUrl");
                        // Picasso.get().load(imageUrl).placeholder(R.drawable.img).into(binding.imgRecipeDetail);
                    }
                });
    }

    private void checkIfFavorite() {
        if (auth.getCurrentUser() == null) return;

        firestore.collection("favorites")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> favorites = documentSnapshot.getData();
                        if (favorites != null && favorites.containsKey(recipeId)) {
                            isFavorite = true;
                            binding.btnFavorite.setImageResource(R.drawable.heart);
                            binding.btnFavorite.setColorFilter(getResources().getColor(R.color.red_500));
                        }
                    }
                });
    }

    private void toggleFavorite() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to add to favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        if (isFavorite) {
            // Remove from favorites
            Map<String, Object> updates = new HashMap<>();
            updates.put(recipeId, FieldValue.delete());
            
            firestore.collection("favorites").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        binding.btnFavorite.setColorFilter(getResources().getColor(R.color.gray_300));
                        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add to favorites
            Map<String, Object> data = new HashMap<>();
            data.put(recipeId, true);
            
            firestore.collection("favorites").document(userId)
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        binding.btnFavorite.setColorFilter(getResources().getColor(R.color.red_500));
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}