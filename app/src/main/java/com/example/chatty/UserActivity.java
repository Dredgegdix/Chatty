package com.example.chatty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatty.databinding.ActivityUserBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserActivity extends AppCompatActivity implements UserListener{

    private static final int MAX_LENGTH_SIZE = 15;
    private  ActivityUserBinding binding;
    private Preference preference;
    private List<User> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preference = new Preference(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.backImage.setOnClickListener(v -> onBackPressed());
        binding.searchProfile.setOnClickListener(v -> {
            if (ruleSearch(binding.search.getText().toString())){
                searchUser();
            }
        });
        binding.searchImg.setOnClickListener(v -> {
            onClickSearchImg();
        });
        binding.searchBack.setOnClickListener(v -> {
            onClickSearchBack();
        });
    }

    private void onClickSearchImg(){
        binding.linerLayoutSearch.setVisibility(View.VISIBLE);
        binding.searchImg.setVisibility(View.GONE);
        binding.searchBack.setVisibility(View.VISIBLE);
        binding.search.setText(null);
    }

    private void onClickSearchBack(){
        binding.linerLayoutSearch.setVisibility(View.GONE);
        binding.searchBack.setVisibility(View.GONE);
        binding.searchImg.setVisibility(View.VISIBLE);
    }

    private void showToast(String toast){
        Toast.makeText(getApplicationContext(), toast , Toast.LENGTH_SHORT).show();
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_USER_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preference.getString(Constants.USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            } else {
                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                users.add(user);
                            }
                        }
                            if (users.size() > 0){
                                Collections.shuffle(users);
                                UserAdapter userAdapter = new UserAdapter(users, this);
                                binding.usersRecyclerView.setAdapter(userAdapter);
                                binding.usersRecyclerView.setVisibility(View.VISIBLE);
                            }else{
                                showErrorMessage();
                            }
                        }else{
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoad){
        if(isLoad){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }

    private void searchUser(){
        String search = binding.search.getText().toString();
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                if (user1.getName().contains(search) && !user2.getName().contains(search)) {
                    return -1;
                } else if (!user1.getName().contains(search) && user2.getName().contains(search)) {
                    return 1;
                } else {
                    return user1.getName().compareTo(user2.getName());
                }
            }
        });
        UserAdapter userAdapter = new UserAdapter(users, this);
        binding.usersRecyclerView.setAdapter(userAdapter);
    }

    private Boolean ruleSearch(String search){
        if (search.isEmpty()){
            showToast("Введите запрос");
            return false;
        }if (search.length() > MAX_LENGTH_SIZE){
            showToast("Слишком длинный запрос");
            return false;
        }else {
            return true;
        }
    }
}