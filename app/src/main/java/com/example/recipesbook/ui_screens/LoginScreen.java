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

public class LoginScreen extends AppCompatActivity {
    private ActivityLoginScreenBinding binding;
    private final Context context = LoginScreen.this;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvDonTHaveAnAccount.setOnClickListener(view -> startActivity(new Intent(context, RegisterScreen.class)));
        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(context, ForgetPasswordScreen.class));
        });

        firebaseAuth = FirebaseAuth.getInstance();
        login();
        showLoadingDialog();


    }

    private void login() {
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.edEmail.getEditText().getText().toString().trim();
            String password = binding.edPassword.getEditText().getText().toString().trim();

            if (checkData(email, password)) {
                progressDialog.show();
                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                String userId = firebaseAuth.getCurrentUser().getUid();
                                saveLoginSession(email, userId);
                                 startActivity(new Intent(context, MainHomePage.class));
                                Toast.makeText(context, "Logged is Successfully", Toast.LENGTH_SHORT).show();
                               finish();
                            } else {
                                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }


    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }


    private boolean checkData(String email, String password) {


        boolean isValid = true;
        binding.edEmail.setError(null);
        binding.edPassword.setError(null);

        if (email.isEmpty()) {
            binding.edEmail.setError("Please enter your email");
            isValid = false;
        } else if (!isValidEmail(email)) {
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


    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void saveLoginSession(String email, String userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userEmail", email);
        editor.putString("userId", userId);
        editor.apply();
    }
}