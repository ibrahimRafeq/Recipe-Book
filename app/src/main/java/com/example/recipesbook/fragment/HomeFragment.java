package com.example.recipesbook.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.recipesbook.R;
import com.example.recipesbook.adapters.RecipesAdapter;
import com.example.recipesbook.databinding.FragmentHomeBinding;
import com.example.recipesbook.models.RecipeModel;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private FirebaseFirestore firestore;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private RecipesAdapter adapter;
    private List<RecipeModel> allRecipes = new ArrayList<>();
    private String currentCategory = "All Recipes";


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", MODE_PRIVATE);

        initLoadingDialog();
        showLoading();

        getUserData();
        getAllCategories();
        setupSearchListener();
        return binding.getRoot();
    }
    private void initLoadingDialog() {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void showLoading() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void getUserData() {
        String userId = sharedPreferences.getString("userId", "-1");
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("username");
                        String imagePath = documentSnapshot.getString("image");
                        binding.tvName.setText(name != null ? name : "No Name");
                        Picasso.get()
                                .load(imagePath)
                                .placeholder(R.drawable.man)
                                .error(R.drawable.man)
                                .into(binding.imageProfile);
                    }
                    hideLoading();
                })
                .addOnFailureListener(e -> hideLoading());
    }

    private void getAllCategories() {
        binding.tabCategories.addTab(binding.tabCategories.newTab().setText("All Recipes"));

        firestore.collection("categories")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String category = document.getString("name");
                            if (category != null && !category.isEmpty()) {
                                binding.tabCategories.addTab(
                                        binding.tabCategories.newTab().setText(category)
                                );
                            }
                        }
                    }

                    getAllRecipes();
                    setupTabListener();
                    hideLoading();
                })
                .addOnFailureListener(e -> hideLoading());
    }

    private void getAllRecipes() {
        firestore.collection("recipes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allRecipes.clear();
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String recipeId = document.getId();
                            String imageUrl = document.getString("image");
                            String title = document.getString("title");
                            String ingredients = document.getString("ingredients");
                            String steps = document.getString("steps");
                            String category = document.getString("category");
                            String userId = document.getString("userId");
                            String videoUrl = document.getString("videoUrl");

                            RecipeModel recipe = new RecipeModel(recipeId, imageUrl, title, ingredients, steps, category, userId, videoUrl);
                            allRecipes.add(recipe);
                        }

                        adapter = new RecipesAdapter(allRecipes, getContext());
                        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        binding.rvRecipes.setAdapter(adapter);
                    }
                    hideLoading();
                })
                .addOnFailureListener(e -> hideLoading());
    }

    private void setupTabListener() {
        binding.tabCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                String searchText = binding.edSearch.getEditText().getText().toString();
                filterRecipes(searchText);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearchListener() {
        binding.edSearch.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterRecipes(String keyword) {
        List<RecipeModel> filteredList = new ArrayList<>();

        for (RecipeModel recipe : allRecipes) {
            boolean matchesCategory = currentCategory.equals("All Recipes") ||
                    recipe.getCategory().equalsIgnoreCase(currentCategory);

            boolean matchesKeyword = recipe.getTitle().toLowerCase().contains(keyword.toLowerCase());

            if (matchesCategory && matchesKeyword) {
                filteredList.add(recipe);
            }
        }

        if (filteredList.isEmpty()) {
            binding.rvRecipes.setVisibility(View.GONE);
            binding.ivEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvRecipes.setVisibility(View.VISIBLE);
            binding.ivEmpty.setVisibility(View.GONE);

            adapter = new RecipesAdapter(filteredList, getContext());
            binding.rvRecipes.setAdapter(adapter);
        }
    }
}