package com.example.chatty;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatty.databinding.ActivityLoginInBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginInActivity extends AppCompatActivity {

    private ActivityLoginInBinding binding;
    private Preference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preference = new Preference(getApplicationContext());
        if (preference.getBoolean(Constants.KEY_IS_LOGIN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivityLoginInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Listeners();
    }


    //все кнопочки
    private void Listeners(){
        binding.createNewAccountTxt.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));
        binding.btnLoginIn.setOnClickListener(v -> {
            if(isValidLogin()){
                login();
            }
        });
    }


    //тосты
    private void showToast(String messagge){
        Toast.makeText(getApplicationContext(), messagge, Toast.LENGTH_SHORT).show();
    }


    //проверка на существование пользователя и вход
    private void login(){
        load(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_USER_COLLECTION)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASS, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preference.putBoolean(Constants.KEY_IS_LOGIN, true);
                        preference.putString(Constants.USER_ID, documentSnapshot.getId());
                        preference.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preference.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        load(false);
                        showToast("Ошибка входа");
                    }
                });
    }

    //проверка вводимых данных
    private Boolean isValidLogin(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Введите почту");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Введите корректную почту");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Введите пароль");
            return false;
        }else{
            return true;
        }
    }


    //прогресс бар
    private void load(Boolean isLoad){
        if(isLoad){
            binding.btnLoginIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnLoginIn.setVisibility(View.VISIBLE);
        }
    }
}