package com.example.recipesbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.CardRecipesRcBinding;
import com.example.recipesbook.models.RecipeModel;
import com.example.recipesbook.ui_screens.RecipeDetailScreen;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipesAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<RecipeModel> models;
    private Context context;
    private CardRecipesRcBinding binding;
    public RecipesAdapter(List<RecipeModel> models, Context context) {
        this.models = models;
        this.context = context;
    }


    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=CardRecipesRcBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new RecipeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecipeViewHolder viewHolder = (RecipeViewHolder) holder;
        try {
            Picasso.get()
                    .load(models.get(position).getImage())
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .into(viewHolder.binding.imageRecipe);
            viewHolder.binding.tvRecipeName.setText(models.get(position).getTitle());
            viewHolder.binding.tvDescription.setText(models.get(position).getIngredients());
            viewHolder.binding.tvCategory.setText(models.get(position).getCategory());

            viewHolder.binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(context, RecipeDetailScreen.class);
                intent.putExtra("id", models.get(position).getId());
                context.startActivity(intent);
            });

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }



    @Override
    public int getItemCount() {
        return  models.size();
    }


    private class RecipeViewHolder extends RecyclerView.ViewHolder {
        private CardRecipesRcBinding binding;

        public RecipeViewHolder(CardRecipesRcBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
