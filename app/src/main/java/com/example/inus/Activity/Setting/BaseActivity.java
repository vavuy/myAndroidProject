package com.example.inus.Activity.Setting;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inus.R;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.black));//狀態列顏色

        mAuth = FirebaseAuth.getInstance();
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_AVAILABILITY,0);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        documentReference.update(Constants.KEY_AVAILABILITY,1);
    }
}
