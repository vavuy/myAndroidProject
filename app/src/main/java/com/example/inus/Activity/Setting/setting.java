package com.example.inus.Activity.Setting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.inus.R;
import com.example.inus.databinding.ActivitySettingBinding;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class setting extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;
    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
    }

    private void init(){
        getSupportActionBar().hide();//隱藏上方導覽列
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.black));//狀態列顏色

        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setListeners(){

        binding.lefticon.setOnClickListener(view -> finish()); // 回上頁
        binding.personalInformationSettings.setOnClickListener(view -> startActivity(new Intent(this, personal_info.class)));
        binding.addFriends.setOnClickListener(view -> startActivity(new Intent(this, AddToFriends.class)));// 新增好友
        binding.friendsList.setOnClickListener(view -> startActivity(new Intent(this,Friends_list.class)));  // 好友列表
        binding.authentication.setOnClickListener(view -> {
            if (mAuth.getCurrentUser().isEmailVerified() == false){
                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> showToast("認證成功"));
            }
        }); //會員認證

        binding.signOut.setOnClickListener(view -> {
            showToast("Signing out");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentReference =
                    db.collection(Constants.KEY_COLLECTION_USERS).document(
                            preferenceManager.getString(Constants.KEY_USER_ID)
                    );
            HashMap<String,Object> updates = new HashMap<>();
            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());  // delete token
            documentReference.update(updates)
                    .addOnSuccessListener(unused -> {
                        preferenceManager.clear();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> showToast("unable to sign out"));
        });  // 登出
    }

}