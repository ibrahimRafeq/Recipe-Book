package com.example.recipesbook.ui_screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipesbook.adapters.RecipesAdapter;
import com.example.recipesbook.databinding.ActivityMyRecipesBinding;
import com.example.recipesbook.models.RecipeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyRecipesActivity extends AppCompatActivity {

    private ActivityMyRecipesBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private RecipesAdapter adapter;
    private List<RecipeModel> myRecipesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadMyRecipes();

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new RecipesAdapter(myRecipesList, this);
        binding.rvMyRecipes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMyRecipes.setAdapter(adapter);
    }

    private void loadMyRecipes() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        firestore.collection("recipes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myRecipesList.clear();
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            RecipeModel recipe = new RecipeModel(
                                    document.getId(),
                                    document.getString("publisherName"),
                                    document.getString("title"),
                                    document.getString("ingredients"),
                                    document.getString("steps"),
                                    document.getString("category"),
                                    document.getString("userId"),
                                    document.getString("videoUrl")
                            );
                            myRecipesList.add(recipe);
                        }
                        adapter.notifyDataSetChanged();
                        binding.llEmptyState.setVisibility(View.GONE);
                        binding.rvMyRecipes.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading your recipes", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        binding.rvMyRecipes.setVisibility(View.GONE);
        binding.llEmptyState.setVisibility(View.VISIBLE);
    }
}