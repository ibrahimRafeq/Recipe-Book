package com.example.recipesbook.ui_screens;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipesbook.databinding.ActivityLoginScreenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginScreen extends AppCompatActivity {

    private ActivityLoginScreenBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;
    private final Context context = LoginScreen.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupProgressDialog();
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.tvDonTHaveAnAccount.setOnClickListener(v ->
                startActivity(new Intent(context, RegisterScreen.class))
        );

        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(context, ForgetPasswordScreen.class))
        );

        binding.loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = binding.edEmail.getEditText().getText().toString().trim();
        String password = binding.edPassword.getEditText().getText().toString().trim();

        if (!isValidData(email, password)) return;

        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        firestore.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(document -> {
                                    progressDialog.dismiss();

                                    if (document.exists()) {
                                        String userName = document.getString("username");
                                        saveLoginSession(email, userId, userName);

                                        startActivity(new Intent(context, MainHomePage.class));
                                        finish();
                                        Toast.makeText(context, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidData(String email, String password) {
        boolean isValid = true;

        binding.edEmail.setError(null);
        binding.edPassword.setError(null);

        if (email.isEmpty()) {
            binding.edEmail.setError("Please enter your email");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edEmail.setError("Invalid email format");
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.edPassword.setError("Please enter your password");
            isValid = false;
        } else if (password.length() < 6) {
            binding.edPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void saveLoginSession(String email, String userId, String userName) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userEmail", email);
        editor.putString("userId", userId);
        editor.putString("userName", userName);
        editor.apply();
    }
}