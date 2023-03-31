package com.example.chatty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.chatty.databinding.ActivityUserBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    private ActivityUserBinding binding;
    private Preference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_user);
        preference = new Preference(getApplicationContext());
        setListener();
        getUsers();
    }

    private void setListener() {
        binding.backImage.setOnClickListener(view -> onBackPressed());
    }

    private void getUsers() {
        load(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_USER_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    load(false);
                    String currentUserID = preference.getString(Constants.USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserID.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.username = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            userList.add(user);
                        }
                        if (userList.size() > 0) {
                            UserAdapter userAdapter = new UserAdapter(userList);
                            binding.usersRecyclerView.setAdapter(userAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("@s", "Пользователь недоступен"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void load(Boolean isLoad) {
        if (isLoad) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}