package com.example.recipesbook.ui_screens;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipesbook.databinding.ActivityForgetPasswordScreenBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordScreen extends AppCompatActivity {
    private ActivityForgetPasswordScreenBinding binding;
    private ProgressDialog progressDialog;
    private final Context context = ForgetPasswordScreen.this;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPasswordScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.tvChangeEmail.setOnClickListener(v -> {
            finish();
        });
        showLoadingDialog();
        forgotPassword();

    }

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private boolean checkData(String email) {


        boolean isValid = true;
        binding.edEmail.setError(null);

        if (email.isEmpty()) {
            binding.edEmail.setError("Please enter your email");
            isValid = false;
        } else if (!isValidEmail(email)) {
            binding.edEmail.setError("Invalid email format");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private void forgotPassword() {
        binding.resetPasswordButton.setOnClickListener(v -> {

            String email = binding.edEmail.getEditText()
                    .getText().toString().trim();

            if (checkData(email)) {

                progressDialog.show();

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            progressDialog.dismiss();

                            if (task.isSuccessful()) {
                                Toast.makeText(
                                        ForgetPasswordScreen.this,
                                        "تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني",
                                        Toast.LENGTH_LONG
                                ).show();
                                finish();
                            } else {
                                Toast.makeText(
                                        ForgetPasswordScreen.this,
                                        task.getException() != null
                                                ? task.getException().getMessage()
                                                : "حدث خطأ، حاول مرة أخرى",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
            }
        });
    }


}