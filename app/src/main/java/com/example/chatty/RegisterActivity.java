package com.example.chatty;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.chatty.databinding.ActivityRegisterBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private Preference preference;
    private String encodedImage;
    private int MIN_LEN_PASS = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preference = new Preference(getApplicationContext());
        Listeners();
    }

    //это кнопочки
    private void Listeners() {
        binding.LoginInTxt.setOnClickListener(view -> onBackPressed());
        binding.btnRegister.setOnClickListener(v -> {
            if (validRegister()) {
                register();
            }
        });
        binding.layoutImage.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    //штука для удобного показа тостов
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    //добавление юзера в базу данных
    private void register() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object>  user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASS, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_USER_COLLECTION)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preference.putBoolean(Constants.KEY_IS_LOGIN, true);
                    preference.putString(Constants.USER_ID, documentReference.getId());
                    preference.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preference.putString(Constants.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exeption ->{
                    loading(false);
                    showToast(exeption.getMessage());
                });
    }

    //проверка вводимых данных
    private Boolean validRegister() {
        if (encodedImage == null) {
            showToast("Выберите аватарку");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Введите имя");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Введите почту");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Введите корректную почту");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Введите пароль");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Повторите пароль");
            return false;
        }else if (binding.inputPassword.getText().toString().length() < MIN_LEN_PASS){
            showToast("Пароль должен содержать не менее 8-ми символов");
            return false;
        }else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Введите повторный пароль корректно");
            return false;
        } else {
            return true;
        }
    }

    //прогрес бар
    private void loading(Boolean isLoad) {
        if (isLoad) {
            binding.btnRegister.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setProgress(View.INVISIBLE);
            binding.btnRegister.setVisibility(View.VISIBLE);
        }
    }

    //загрузка аватарки
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte bytes[] = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    //загрузка аватарки
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.profileImage.setImageBitmap(bitmap);
                            binding.addImageTxt.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
}