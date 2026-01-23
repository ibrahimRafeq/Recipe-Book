package com.example.recipesbook.ui_screens;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityRegisterScreenBinding;
import com.example.recipesbook.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RegisterScreen extends AppCompatActivity {
    ActivityRegisterScreenBinding binding;
    private final Context context = RegisterScreen.this;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityRegisterScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.tvHaveAAccount.setOnClickListener(v -> finish());
        binding.btnSignUp.setOnClickListener(v -> checkData());

        showLoadingDialog();
        setupCountrySpinner();
        initImagePicker();
        pickImage();

    }
    private void initImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        binding.imageProfile.setImageURI(selectedImageUri);
                    }
                }
        );
    }

    private void pickImage() {
        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }


    private void setupCountrySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCountry.setAdapter(adapter);
        binding.spinnerCountry.setSelection(0);
    }
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private void checkData() {
        String username = binding.edUsername.getEditText().getText().toString().trim();
        String email = binding.edEmail.getEditText().getText().toString().trim();
        String password = binding.edPassword.getEditText().getText().toString().trim();
        String confirmPassword = binding.edConfirmPassword.getEditText().getText().toString().trim();

        boolean isValid = true;

        binding.edUsername.setError(null);
        binding.edEmail.setError(null);
        binding.edPassword.setError(null);
        binding.edConfirmPassword.setError(null);

        if (username.isEmpty()) {
            binding.edUsername.setError("Please enter your username");
            isValid = false;
        }

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

        if (confirmPassword.isEmpty()) {
            binding.edConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            binding.edConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            createUserInFirebase(username,email,password,selectedImageUri.toString(),binding.spinnerCountry.getSelectedItem().toString());
        }
    }
    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }
    private void createUserInFirebase(String username, String email, String password, String imageUrl, String country) {
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        UserModel userModel = new UserModel(userId, username, email, password, imageUrl, country);

                        firebaseFirestore.collection("users")
                                .document(userId)
                                .set(userModel)
                                .addOnSuccessListener(unused -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Sign up successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(context, LoginScreen.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


   /* private File bitmapToFile(Bitmap bitmap, String name) throws IOException {
        File f = new File(getCacheDir(), name);
        f.createNewFile();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return f;
    }

    */

}