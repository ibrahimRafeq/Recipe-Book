package com.example.recipesbook.models;

public class UserModel {
    private String id;
    private String username;
    private String email;
    private String password;
    private String image;
    private String country;

    public UserModel(String id, String username, String email, String password, String image, String country) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.image = image;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
