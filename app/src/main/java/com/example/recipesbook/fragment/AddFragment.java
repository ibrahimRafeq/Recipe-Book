package com.example.recipesbook.fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.recipesbook.databinding.FragmentAddBinding;
import com.example.recipesbook.models.RecipeModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    public AddFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater, container, false);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);

        initLoadingDialog();
        showLoading();
        getAllCategories();

        binding.btnAddRecipe.setOnClickListener(v -> checkData());

        return binding.getRoot();
    }

    private void checkData() {
        String title = binding.etTitle.getText().toString().trim();
        String ingredients = binding.etIngredients.getText().toString().trim();
        String steps = binding.etSteps.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        String videoUrl = binding.etVideoUrl.getText().toString().trim();

        if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equals("Select Category")) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (videoUrl.isEmpty() || !Patterns.WEB_URL.matcher(videoUrl).matches()) {
            Toast.makeText(getContext(), "Please enter a valid video URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = sharedPreferences.getString("userId", "-1");
        performAdd(title, ingredients, steps, category, userId, videoUrl);
    }

    private void getAllCategories() {
        firestore.collection("categories")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> categoryList = new ArrayList<>();
                    categoryList.add("Select Category");

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String category = document.getString("name");
                        if (category != null && !category.isEmpty()) {
                            categoryList.add(category);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            android.R.layout.simple_spinner_item,
                            categoryList
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spinnerCategory.setAdapter(adapter);
                    hideLoading();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                });
    }

    private void initLoadingDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void showLoading() {
        if (!progressDialog.isShowing()) progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog.isShowing()) progressDialog.dismiss();
    }

    private void performAdd(String title, String ingredients, String steps,
                            String category, String userId, String videoUrl) {

        showLoading();

        String publisherName = sharedPreferences.getString("userName", "Unknown");

        RecipeModel recipe = new RecipeModel(
                null,
                publisherName,
                title,
                ingredients,
                steps,
                category,
                userId,
                videoUrl
        );

        firestore.collection("recipes")
                .add(recipe)
                .addOnSuccessListener(docRef -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Recipe added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Failed to add recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearFields() {
        binding.etTitle.setText("");
        binding.etIngredients.setText("");
        binding.etSteps.setText("");
        binding.etVideoUrl.setText("");
        binding.spinnerCategory.setSelection(0);
    }
}