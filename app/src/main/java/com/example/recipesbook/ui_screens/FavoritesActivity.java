package com.example.recipesbook.ui_screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipesbook.adapters.RecipesAdapter;
import com.example.recipesbook.databinding.ActivityFavoritesBinding;
import com.example.recipesbook.models.RecipeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoritesActivity extends AppCompatActivity {

    private ActivityFavoritesBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecipesAdapter adapter;
    private List<RecipeModel> favoriteRecipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadFavorites();

        binding.btnBackFav.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new RecipesAdapter(favoriteRecipes, this);
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        
        firestore.collection("favorites").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> favoritesMap = documentSnapshot.getData();
                        if (favoritesMap != null && !favoritesMap.isEmpty()) {
                            fetchRecipes(new ArrayList<>(favoritesMap.keySet()));
                        } else {
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading favorites", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void fetchRecipes(List<String> recipeIds) {
        favoriteRecipes.clear();
        for (String id : recipeIds) {
            firestore.collection("recipes").document(id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            RecipeModel recipe = new RecipeModel(
                                    documentSnapshot.getId(),
                                    documentSnapshot.getString("publisherName"),
                                    documentSnapshot.getString("title"),
                                    documentSnapshot.getString("ingredients"),
                                    documentSnapshot.getString("steps"),
                                    documentSnapshot.getString("category"),
                                    documentSnapshot.getString("userId"),
                                    documentSnapshot.getString("videoUrl")
                            );
                            favoriteRecipes.add(recipe);
                            adapter.notifyDataSetChanged();
                            
                            binding.llEmptyState.setVisibility(View.GONE);
                            binding.rvFavorites.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void showEmptyState() {
        binding.rvFavorites.setVisibility(View.GONE);
        binding.llEmptyState.setVisibility(View.VISIBLE);
    }
}