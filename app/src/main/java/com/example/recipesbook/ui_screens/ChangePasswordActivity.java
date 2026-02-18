package com.example.recipesbook.ui_screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipesbook.R;
import com.example.recipesbook.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.saveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = binding.oldPassword.getText().toString();
                String newPassword = binding.newPassword.getText().toString();
                String newPassword2 = binding.newPassword2.getText().toString();

                if (newPassword.equals(newPassword2)){
                    changePassword(oldPassword, newPassword);
                }
            }
        });

    }

    private void changePassword(String oldPassword, String newPassword) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "لا يوجد مستخدم مسجل دخول", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();

        AuthCredential credential =
                EmailAuthProvider.getCredential(email, oldPassword);

        // إعادة التوثيق
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // تغيير كلمة السر
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                Toast.makeText(this,
                                        "تم تغيير كلمة السر بنجاح ✅",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this,
                                        "فشل تغيير كلمة السر ❌",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

            } else {
                Toast.makeText(this,
                        "كلمة السر القديمة غير صحيحة ❌",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}