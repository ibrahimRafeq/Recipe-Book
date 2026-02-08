package com.example.recipesbook.models;

public class RecipeModel {
    private String id;
    private String image;
    private String title;
    private String ingredients;
    private String steps;
    private String category;
    private String userId;
    private String videoUrl;


    public RecipeModel(String id, String image, String title, String ingredients, String steps, String category, String userId, String videoUrl) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
        this.category = category;
        this.userId = userId;
        this.videoUrl = videoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
