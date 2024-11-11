package com.example.inus.Activity.Setting;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.inus.adapter.Chat.ChatAdapter;
import com.example.inus.databinding.ActivityChatBinding;
import com.example.inus.model.ChatMessage;
import com.example.inus.model.User;
import com.example.inus.network.ApiClient;
import com.example.inus.network.ApiService;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    private String conversionId =null;
    private Boolean isReceiverAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();   // 觸發事件
        loadReceiverDetails();  // 載入聊天者資訊
        init();  // 初始化
        listenMessages();  // 從DB中根據ID來讀資料
    }

    // 初始化
    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodeString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
    }
    // 送出聊天內容，上傳DB且通知對方
    private void sendMessage(){
        // 建立聊天訊息紀錄
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        db.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        // 建立最後一次對話紀錄
        if(conversionId != null){
            updateConversion(binding.inputMessage.getText().toString());
        }else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        //若非正在使用，則發送通知
        if(!isReceiverAvailable){
            try{
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID,preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());

            }catch (Exception e){
                showToast(e.getMessage());
            }
        }
        //送出訊息後，文字框清空
        binding.inputMessage.setText(null);
    }
    // showToast
    private  void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    // 發送通知
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).setMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    try{
                        if(response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully" );
                }else {
                    Log.d("demo", ""+ response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }
    //
    private void listenerAvailabilityOfReceiver(){
        db.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, ((value, error) -> {
            if(error != null){
                return;
            }
            if(value != null){
                if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }

                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);

                if(receiverUser.image == null){
                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodeString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
                // 及時顯示對方是否在線上
                if(isReceiverAvailable){
                    binding.textAvailability.setVisibility(View.VISIBLE);
                }else {
                    binding.textAvailability.setVisibility(View.GONE);
                }
            }
        }));
    }
    // 汲取聊天內容
    private void listenMessages(){
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    // DB 即時更新 聊天內容
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            int count = chatMessages.size();

            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadbleDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObjcet = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
                Collections.sort(chatMessages,(obj1, obj2) -> obj1.dateObjcet.compareTo(obj2.dateObjcet));
                if(count ==0){
                    chatAdapter.notifyDataSetChanged();
                }else{
                    chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() -1);
                }
                binding.chatRecyclerView.setVisibility(View.VISIBLE);
            }
            binding.progressBar.setVisibility(View.GONE);
            if(conversionId == null){
                chcekForConversion();
            }
        }
    };
    // image convert bytes, 以顯示頭像
    private Bitmap getBitmapFromEncodeString(String encodedImage) {
        if(encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }else {
            return null;
        }
    }
    // 載入USER資訊
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textname.setText(receiverUser.name);
    }
    // btn 事件
    private void setListeners(){
        binding.leftIcon.setOnClickListener(view -> onBackPressed());
        binding.layoutSend.setOnClickListener(view -> sendMessage());
    }
    // 設定時間格式
    private String getReadbleDateTime(Date date){
        return new SimpleDateFormat("yy/MM/dd HH:mm" , Locale.getDefault()).format(date);
    }
    //把聯絡人寫入 conversion 中
    private  void addConversion(HashMap<String, Object> conversion){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }
    // 更新 Conversion 的資料
    private void updateConversion(String message){
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }
    //  如果新增聊天內容，則寫入對話中
    private void chcekForConversion(){
        if(chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }
    // 寫入對話DB
    private void checkForConversionRemotely(String senderId, String receiverId){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }
    // 取得最新一筆對話紀錄跟聯絡人
    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() !=null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };
    // 返回時顯示對方是否在線上
    @Override
    protected void onPostResume() {
        super.onPostResume();
        listenerAvailabilityOfReceiver();
    }

}