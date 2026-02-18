package com.example.recipesbook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthManager {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private Context context;

    public AuthManager(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // login in
    public void Login(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // sign up
    public void register(String email, String password,
                         OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }


    // upload image to supabase
    public void uploadImage(Bitmap bitmap, String fileName, String path, OnImageUploadCallback callback) throws IOException {

        File file = bitmapToFile(bitmap, fileName);
        String baseUrl = "https://qzetvtuhlgofxvhksfet.supabase.co";
        String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF6ZXR2dHVobGdvZnh2aGtzZmV0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE0MTAwNDAsImV4cCI6MjA4Njk4NjA0MH0.5Q3fQ09p5IQkVaLKO7hYQfY55mLhRw3gCgNUgdo9SJY";

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiServes api = retrofit.create(ApiServes.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);
        api.uploadImage(
                apiKey,
                "Bearer " + apiKey,
                "images",
                path + fileName,
                body
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String url = baseUrl + "storage/v1/object/public/images/" + path + fileName;
                    callback.onSuccess(url);
                } else {
                    callback.onFailure("Upload failed");
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });

    }

    // save user data
    public void saveUserData(String userId, String name, String email, String imageUrl) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("imageUrl", FieldValue.arrayUnion(imageUrl));
        data.put("backgroundImages", new ArrayList<>()); //

        firestore.collection("users")
                .document(userId)
                .set(data)
                .addOnSuccessListener(a -> Log.d("AUTH", "User Saved"))
                .addOnFailureListener(e -> Log.e("AUTH", e.getMessage()));
    }

    // get data the user
    public void fetchUserData(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(listener);
    }


    // change image from bitmap to file
    private File bitmapToFile(Bitmap bitmap, String fileName) throws IOException {
        File file = new File(context.getCacheDir(), fileName);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] data = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();

        return file;
    }

    public interface OnImageUploadCallback {
        void onSuccess(String imageUrl);

        void onFailure(String error);
    }

}
