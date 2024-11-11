package com.example.inus.Activity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.inus.Activity.Setting.setting;
import com.example.inus.R;
import com.example.inus.adapter.NotificationAdapter;
import com.example.inus.databinding.ActivityNotificationScreenBinding;
import com.example.inus.util.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Notification_screen extends AppCompatActivity {
    private NotificationAdapter notificationAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityNotificationScreenBinding binding;
    private boolean isGroupBuy =false;
    ArrayList<String> events = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
        setBtnBackground(getCurrentFocus());//預設按鈕顏色
        getEvent(getCurrentFocus());//裝資料庫內的通知
    }

    private void init(){
        getSupportActionBar().hide();//隱藏上方導覽列
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.black));//狀態列顏色
    }

    private  void setListener(){
        binding.group.setOnClickListener(v -> {
            setBtnBackground(v);
            getEvent(v);
            isGroupBuy = false;
        } );
        binding.groupbuy.setOnClickListener(v -> {
            setBtnBackground(v);
            getEvent(v);
            isGroupBuy =true;
        } );

        binding.navigation.setSelectedItemId(R.id.none);//選到none按鈕改變顏色
        binding.righticon.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), setting.class));
            overridePendingTransition(0,0);
        });
        binding.navigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.home:
                    startActivity(new Intent(getApplicationContext(),Home_screen.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.shop:
                    startActivity(new Intent(getApplicationContext(),Shop_screen.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.cart:
                    startActivity(new Intent(getApplicationContext(),Cart_screen.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.talk:
                    startActivity(new Intent(getApplicationContext(),Talk_screen.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.none:
                    return true;
            }
            return false;
        });
    }

    private void setBtnBackground(View v){
        binding.group.setBackground(getResources().getDrawable(R.drawable.theme2_fill__button_color));
        binding.groupbuy.setBackground(getResources().getDrawable(R.drawable.select_btn_color));
        if(v == binding.groupbuy){
            binding.groupbuy.setBackground(getResources().getDrawable(R.drawable.theme2_fill__button_color));
            binding.group.setBackground(getResources().getDrawable(R.drawable.select_btn_color));
        }
    }

    private void getEvent(View v){
        String collectionPath = Constants.KEY_COLLECTION_USERS + "/" + Constants.UID +"/group";
        if(v == binding.groupbuy){
            isGroupBuy =true;
            collectionPath = Constants.KEY_COLLECTION_USERS + "/" + Constants.UID +"/groupBuy";
        }

        db.collection(collectionPath)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        events.clear();
                        for(QueryDocumentSnapshot doc : task.getResult()){
                            events.add(doc.getId());
                        }
                        notificationAdapter = new NotificationAdapter(Notification_screen.this,events,isGroupBuy);
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(Notification_screen.this));
                        binding.recyclerView.setAdapter(notificationAdapter);
                    }
                });
    }

}