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
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityRegisterScreenBinding;
import com.example.recipesbook.models.UserModel;
import com.example.recipesbook.utils.AuthManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;

public class RegisterScreen extends AppCompatActivity {

    private ActivityRegisterScreenBinding binding;
    private Context context;
    private Bitmap bitmap;
    private Uri selectedImageUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog progressDialog;
    private AuthManager authManager;

    // لاختيار الصورة
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // استخدم URI فقط لتجنب الكراش
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            binding.imageProfile.setImageURI(selectedImageUri);
                        } else {
                            Toast.makeText(context, "Failed to get image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        authManager = new AuthManager(this);

        setupCountrySpinner();
        setupClickListeners();
        setupProgressDialog();
    }

    private void setupCountrySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.countries_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCountry.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.tvHaveAAccount.setOnClickListener(v -> finish());

        binding.btnSignUp.setOnClickListener(v -> checkData());

        binding.imageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            launcher.launch(intent);
        });
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void checkData() {
        String username = binding.edUsername.getEditText().getText().toString().trim();
        String email = binding.edEmail.getEditText().getText().toString().trim();
        String password = binding.edPassword.getEditText().getText().toString().trim();
        String confirmPassword = binding.edConfirmPassword.getEditText().getText().toString().trim();
        String country = binding.spinnerCountry.getSelectedItem().toString();

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

        // تحويل URI إلى bitmap إذا موجود
        if (selectedImageUri != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Failed to process selected image", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        } else {
            Toast.makeText(context, "Please select a profile image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (isValid) {
            registerUser(username, email, password, country);
        }
    }

    private void registerUser(String username, String email, String password, String country) {
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        String fileName = userId + "_" + System.currentTimeMillis() + ".jpg";

                        try {
                            authManager.uploadImage(bitmap, fileName, "profileImages/", new AuthManager.OnImageUploadCallback() {
                                @Override
                                public void onSuccess(String imageUrl) {
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
                                }

                                @Override
                                public void onFailure(String error) {
                                    progressDialog.dismiss();
                                    Toast.makeText(context, "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                            Toast.makeText(context, "Image processing error", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
