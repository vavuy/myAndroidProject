package com.example.inus.Activity.addEvent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.inus.Activity.Setting.BaseActivity;
import com.example.inus.R;
import com.example.inus.adapter.Event.FriendsAdapter;
import com.example.inus.databinding.ActivityPickerfriendsBinding;
import com.example.inus.listeners.Callback;
import com.example.inus.model.User;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class _PickerFriends extends BaseActivity implements Callback {

    private ActivityPickerfriendsBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PreferenceManager preferenceManager;
    FriendsAdapter friendsAdapter;
    List<String> friendID = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPickerfriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        getFriends();

        binding.button.setOnClickListener(view -> finish());
        binding.BtnAddfpicker.setOnClickListener(view ->{
            try {
                if (!preferenceManager.getString(Constants.KEY_SELECTED_USERS).isEmpty()) {
                    startActivity(new Intent(this, _PickerTime.class));
                }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "請選擇好友", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFriends(){
        // 讀取好友資料
        db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.UID).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        List<String> s = (List<String>) task.getResult().get("friends");
                        String friendIDStr = "" + s;
                        responseCallback(friendIDStr);  //async
                    }
                });

        //取出用戶中好友資料
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
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
                            RecyclerView recyclerView = findViewById(R.id.recycleView_fpicker);
                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                            friendsAdapter = new FriendsAdapter(users);
                            recyclerView.setAdapter(friendsAdapter);
                            recyclerView.setVisibility(View.VISIBLE);
                            friendsAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void responseCallback(String data) {
        data = data.replaceAll("\\[" ,"").replaceAll("\\]" ,"").replaceAll(" ","");
        friendID = new ArrayList<>(Arrays.asList(data.split(",")));
    }
}