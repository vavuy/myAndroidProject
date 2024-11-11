package com.example.inus.Activity.Setting;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.inus.databinding.ActivityAddToFriendsBinding;
import com.example.inus.firebase.FCMMessages;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.inus.listeners.Callback;


public class AddToFriends extends BaseActivity implements Callback {

    private ActivityAddToFriendsBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PreferenceManager preferenceManager;
    String test=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddToFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());


        binding.leftIcon.setOnClickListener(view -> finish());
        binding.button3.setOnClickListener(v-> {
            try{
                DocumentReference doc = db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.UID);
                doc.update("friends", FieldValue.arrayUnion(binding.inputfrineds.getText().toString().replaceAll(" ","")));
                Toast.makeText(getApplicationContext(), "已新增好友", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Log.d("demoErr" , e.getMessage());
            }
        });

//        binding.buttonTest.setOnClickListener(view -> {
//            db.collection(Constants.KEY_COLLECTION_USERS).document("j3xOD18mmfU2INjkCwTBXdql2402").get()
//                    .addOnCompleteListener(task -> {
//                        if(task.isSuccessful()){
//                            String friendIDStr = "" + task.getResult().get(Constants.KEY_FCM_TOKEN);
//                            responseCallback(friendIDStr);  //async
//                        }
//                    });
//            Toast.makeText(this, preferenceManager.getString(Constants.KEY_FCM_TOKEN), Toast.LENGTH_SHORT).show();
//
//            String notification_title = "This is notification title";
//            String notification_des = "This is notification description";

//            new FCMMessages().sendMessageSingle(this, test, notification_title, notification_des, null);
//        });
    }


    @Override
    public void responseCallback(String data) {
//        data = data.replaceAll("\\[" ,"").replaceAll("\\]" ,"").replaceAll(" ","");
//        friendID = new ArrayList<>(Arrays.asList(data.split(",")));
        test = data;
    }
}