package com.example.inus.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.inus.Activity.Setting.BaseActivity;
import com.example.inus.Activity.Setting.ChatActivity;
import com.example.inus.Activity.Setting.setting;
import com.example.inus.R;
import com.example.inus.adapter.Chat.RecentConversationsAdapter;

import com.example.inus.databinding.ActivityTalkScreenBinding;
import com.example.inus.listeners.ConversionListener;
import com.example.inus.model.ChatMessage;
import com.example.inus.model.User;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class Talk_screen extends BaseActivity implements ConversionListener {

    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore db;
    private ActivityTalkScreenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTalkScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversation();
    }
    // 初始化
    private void  init(){
        db =FirebaseFirestore.getInstance();
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations,this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        preferenceManager = new PreferenceManager(getApplicationContext());
    }
    // btn 事件
    private void setListeners() {
        binding.rightIcon.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), setting.class));
            overridePendingTransition(0,0);
        });
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        binding.navigation.setSelectedItemId(R.id.talk);//選到cart按鈕改變顏色
        binding.navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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

                    case R.id.talk:return true;

                    case R.id.none:
                        startActivity(new Intent(getApplicationContext(), Notification_screen.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }
    // 載入對話內容
    private void listenConversation(){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    // 及時更新對話訊息
    private final EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String recevierId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = recevierId;
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObjcet = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i=0;i < conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObjcet = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }//For loop
            Collections.sort(conversations,(obj1, obj2) -> obj2.dateObjcet.compareTo(obj1.dateObjcet));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    });
    // 取得token
    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    // 更新token
    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
//                .addOnSuccessListener(unused -> showToast("Token update successfully"))
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }
    // 載入USER 資料
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadUserDetails(){
            binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
            try {
                byte[] bytes = Base64.getDecoder().decode(preferenceManager.getString(Constants.KEY_IMAGE));
                if(bytes != null){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.imageProfile.setImageBitmap(bitmap);
                }else {
                    binding.imageProfile.setImageDrawable(getDrawable(R.drawable.person_white));
                }
            }catch (NullPointerException e){
                Log.d("ImageError", e.getMessage());
            }
    }
    // showToast
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    // 點擊對話框之後進入聊天室
    @Override
    public void onConversionClicked(User user) {
        Intent it = new Intent(getApplicationContext(), ChatActivity.class);
        it.putExtra(Constants.KEY_USER,user);
        startActivity(it);
    }
}