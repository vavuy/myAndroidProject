package com.example.inus.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inus.Activity.Setting.setting;
import com.example.inus.R;
import com.example.inus.adapter.cartAdapter;
import com.example.inus.model.docobject;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Base64;

public class Cart_screen extends AppCompatActivity {
    private BottomNavigationView navigation;
    private Button buyer,seller;
    private ImageView rigthicon;
    private com.makeramen.roundedimageview.RoundedImageView avatar;
    private RecyclerView recyclerView;
    private TextView name;
    private com.example.inus.adapter.cartAdapter cartAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PreferenceManager preferenceManager;
    int Tpye;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_screen);
        getSupportActionBar().hide();//隱藏上方導覽列
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.black));//狀態列顏色
        rigthicon = findViewById(R.id.righticon);
        avatar = findViewById(R.id.avatar);
        navigation= findViewById(R.id.navigation);
        name =findViewById(R.id.name);
        buyer = findViewById(R.id.buyer);
        seller =findViewById(R.id.seller);
        recyclerView =findViewById(R.id.recyclerView);
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListener();

        mAuth = FirebaseAuth.getInstance();
        String Uid =mAuth.getCurrentUser().getUid();
        buyer.setBackground(getResources().getDrawable(R.drawable.theme2_fill__button_color));
        seller.setBackground(getResources().getDrawable(R.drawable.select_btn_color));

        name.setText(preferenceManager.getString(Constants.KEY_NAME));
        try {
            byte[] bytes = Base64.getDecoder().decode(preferenceManager.getString(Constants.KEY_IMAGE));
            if(bytes != null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                avatar.setImageBitmap(bitmap);
            }else {
                avatar.setImageDrawable(getDrawable(R.drawable.person_white));
            }
        }catch (NullPointerException e){
            Log.d("error", e.getMessage());
        }

        ArrayList<String> buy = new ArrayList<>();
        ArrayList<String>buyid = new ArrayList<>();
        Tpye=0;
        db.collection(Constants.KEY_COLLECTION_USERS + "/" +Uid+"/buy")//抓取buy資料裝到buy、buyid陣列並傳到cartAdapter
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                docobject b = doc.toObject(docobject.class);
                                buy.add(b.title);
                                buyid.add(doc.getId());
                                cartAdapter= new cartAdapter(Cart_screen.this,buy,buyid,Tpye);
                            }
                            recyclerView.setLayoutManager(new LinearLayoutManager(Cart_screen.this));
                            recyclerView.setAdapter(cartAdapter);
                        }
                    }
                });

        buyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyer.setBackground(getResources().getDrawable(R.drawable.theme2_fill__button_color));//當我按下上方按鈕後將顏色改變
                seller.setBackground(getResources().getDrawable(R.drawable.select_btn_color));//當我按下上方按鈕後將顏色改變
                Tpye=0;
                ArrayList<String> buy = new ArrayList<>();
                ArrayList<String>buyid = new ArrayList<>();
                db.collection( Constants.KEY_COLLECTION_USERS + "/"+Uid+"/buy")//抓取buy資料裝到buy、buyid陣列並傳到cartAdapter
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot doc : task.getResult()) {
                                        docobject b = doc.toObject(docobject.class);
                                        buyid.add(doc.getId());
                                        buy.add(b.title);
                                        cartAdapter= new cartAdapter(Cart_screen.this,buy,buyid,Tpye);
                                    }
                                    recyclerView.setLayoutManager(new LinearLayoutManager(Cart_screen.this));
                                    recyclerView.setAdapter(cartAdapter);
                                }
                            }
                        });
            }
        });
        seller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seller.setBackground(getResources().getDrawable(R.drawable.theme2_fill__button_color));//當我按下上方按鈕後將顏色改變
                buyer.setBackground(getResources().getDrawable(R.drawable.select_btn_color));//當我按下上方按鈕後將顏色改變
                Tpye=1;
                ArrayList<String> buy = new ArrayList<>();
                ArrayList<String>buyid = new ArrayList<>();
                db.collection( Constants.KEY_COLLECTION_USERS + "/"+Uid+"/sell")//抓取sell資料裝到buy、buyid陣列並傳到cartAdapter
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot doc : task.getResult()) {
                                        docobject b = doc.toObject(docobject.class);
                                        buy.add(b.title);
                                        buyid.add(doc.getId());
                                        cartAdapter= new cartAdapter(Cart_screen.this,buy,buyid,Tpye);
                                    }
                                    recyclerView.setLayoutManager(new LinearLayoutManager(Cart_screen.this));
                                    recyclerView.setAdapter(cartAdapter);
                                }
                            }
                        });
            }
        });
        navigation.setSelectedItemId(R.id.cart);//選到cart按鈕改變顏色

    }

    private void setListener(){
        rigthicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), setting.class));//點選右上角設定轉跳至設定畫面
                overridePendingTransition(0,0);
            }
        });

        navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {//下方導覽列
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
                        return true;
                    case R.id.talk:
                        startActivity(new Intent(getApplicationContext(),Talk_screen.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.none:
                        startActivity(new Intent(getApplicationContext(), Notification_screen.class));
                        overridePendingTransition(0,0);
                        return true;

                }
                return false;
            }
        });
    }




}