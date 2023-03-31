package com.example.chatty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import com.example.chatty.databinding.ActivityMainBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Preference preference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preference = new Preference(getApplicationContext());
        loadUserDetails();
        getToken();
        setListeners();
    }

    private void setListeners() {
        binding.exitImage.setOnClickListener(v -> exit());
        binding.createNewChat.setOnClickListener(v ->
                    startActivity(new Intent(getApplicationContext(), UserActivity.class)));
    }

    private void loadUserDetails() {    // Подгрузка данных пользователя
        binding.textName.setText(preference.getString(Constants.KEY_NAME));
        byte [] bytes = Base64.decode(preference.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.profileImage.setImageBitmap(bitmap);
    }

    private void showToast(String message){   // Удобные тосты
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_USER_COLLECTION).document(
                        preference.getString(Constants.USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Не удалось обновить токен"));
    }

    private void exit() {       // Обработка кнопки выхода
        showToast("Выход...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_USER_COLLECTION).document(
                        preference.getString(Constants.USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preference.clear();
                    startActivity(new Intent(getApplicationContext(), LoginInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("При выходе произошла ошибка."));
    }
}