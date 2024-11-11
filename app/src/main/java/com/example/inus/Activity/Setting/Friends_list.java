package com.example.inus.Activity.Setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.inus.adapter.UsersAdapter;
import com.example.inus.databinding.ActivityFriendsListBinding;
import com.example.inus.listeners.Callback;
import com.example.inus.listeners.UserListener;
import com.example.inus.model.User;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Friends_list extends BaseActivity implements UserListener, Callback {

    private FirebaseAuth mAuth;
    private ActivityFriendsListBinding binding;
    private PreferenceManager preferenceManager;
    private String UID;
    List<String> friendID = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getUsers();
        binding.leftIcon.setOnClickListener(view -> finish());
    }

    // 初始化
    private void init(){
        mAuth = FirebaseAuth.getInstance();
        UID =mAuth.getCurrentUser().getUid();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }
    // 取得好友
    private  void getUsers(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // 讀取自己的好友清單
        db.collection(Constants.KEY_COLLECTION_USERS).document(UID).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        List<String> s = (List<String>) task.getResult().get("friends");
                        String friendIDStr = "" + s;
                        responseCallback(friendIDStr);  //async
                    }
                });

        // 把屬於好友的名單取出
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {  // 跳過自己
                                continue;
                            }
                            if (friendID.contains(queryDocumentSnapshot.getId())) {  // 篩選自己的好友
                                User user = new User();
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.id = queryDocumentSnapshot.getId();
                                users.add(user);
                            }
                        }
                        if (users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecycleView.setAdapter(usersAdapter);
                            binding.usersRecycleView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    // 還沒載入完全的動畫
    private  void loading(Boolean isloading){
        if(isloading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
    // 點擊好友後進入聊天室
    public  void  onUserClicked(User user){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }

    @Override
    public void responseCallback(String data) {
        data = data.replaceAll("\\[" ,"").replaceAll("\\]" ,"").replaceAll(" ","");
        friendID = new ArrayList<>(Arrays.asList(data.split(",")));
    }
}