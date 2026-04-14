package com.example.recipesbook.ui_screens;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipesbook.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating password...");
        progressDialog.setCancelable(false);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.saveChange.setOnClickListener(v -> {
            String oldPass = binding.oldPassword.getText().toString().trim();
            String newPass = binding.newPassword.getText().toString().trim();
            String confirmPass = binding.newPassword2.getText().toString().trim();

            if (validate(oldPass, newPass, confirmPass)) {
                updatePassword(oldPass, newPass);
            }
        });
    }

    private boolean validate(String oldPass, String newPass, String confirmPass) {
        if (oldPass.isEmpty()) {
            binding.oldPassword.setError("Current password is required");
            return false;
        }
        if (newPass.length() < 6) {
            binding.newPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!newPass.equals(confirmPass)) {
            binding.newPassword2.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void updatePassword(String oldPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        progressDialog.show();

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                    progressDialog.dismiss();
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update password. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressDialog.dismiss();
                binding.oldPassword.setError("Incorrect current password");
                Toast.makeText(this, "Authentication failed. Check your current password.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}