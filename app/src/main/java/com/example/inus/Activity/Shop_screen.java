package com.example.inus.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.example.inus.Activity.Setting.setting;
import com.example.inus.R;
import com.example.inus.adapter.shopAdapter;
import com.example.inus.adapter.shopcartAdapter;
import com.example.inus.model.addformat;
import com.example.inus.model.docobject;
import com.example.inus.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Shop_screen extends AppCompatActivity {
    private BottomNavigationView navigation;
    private Button shop_cart,shop_post,post_add;
    private ImageView rigthicon;
    private RelativeLayout relativeLayout1;
    private RecyclerView recyclerView;
    private com.example.inus.adapter.shopAdapter shopAdapter;
    private com.example.inus.adapter.shopcartAdapter shopcartAdapter;
    private Dialog adddialog,suredialog,dialog;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String[] hobby ={"美妝保養","教育學習","居家婦幼","醫療保健","視聽娛樂","流行服飾","旅遊休閒","３Ｃ娛樂","人氣食品"};
    int no= 0;
    private StorageReference storageRef;
    ArrayList<Uri> uris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_screen);
        getSupportActionBar().hide();//隱藏上方導覽列
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.black));//狀態列顏色
        navigation= findViewById(R.id.navigation);
        rigthicon = findViewById(R.id.righticon);
        shop_cart = findViewById(R.id.shop_cart);
        shop_post = findViewById(R.id.shop_post);
        post_add =findViewById(R.id.post_add);
        relativeLayout1 = findViewById(R.id.recyclerView1);
        recyclerView = findViewById(R.id.recyclerView);
        adddialog = new Dialog(this);
        suredialog = new Dialog(this);
        dialog = new Dialog(this);
        final NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        shop_post.setBackground(getResources().getDrawable(R.drawable.theme2_fill__button_color));
        shop_cart.setBackground(getResources().getDrawable(R.drawable.select_btn_color));
        navigation.setSelectedItemId(R.id.shop);//選到shop按鈕改變顏色
        setListener();

        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> articleList = new ArrayList<>();
        ArrayList<String> titlelist = new ArrayList<>();
        ArrayList<String> endtimelist= new ArrayList<>();
        ArrayList<String> id = new ArrayList<>();

        db.collection(Constants.KEY_COLLECTION_USERS)//抓取使用者資料加到arraylsit裡，再將arraylsit傳到shopAdapter
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String hobby = documentSnapshot.getString("hobby");
                            String uhobby = String.valueOf(hobby);
                            db.collection("post")
//                                    .whereEqualTo("hobby",uhobby)//智慧篩選，將使用者的嗜好類別與貼文的嗜好類別做篩選
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                                    id.add(doc.getId());
                                                    docobject b = doc.toObject(docobject.class);
                                                    nameList.add(b.name);
                                                    articleList.add(b.article);
                                                    titlelist.add(b.title);
                                                    endtimelist.add(b.endtime);
                                                    shopAdapter = new shopAdapter(Shop_screen.this, nameList,articleList,titlelist,endtimelist,id);
                                                }
                                                recyclerView.setLayoutManager(new LinearLayoutManager(Shop_screen.this));
                                                recyclerView.setAdapter(shopAdapter);
                                            }
                                        }
                                    });
                        }
                    }
                });

        shop_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shop_post.setBackground(getResources().getDrawable(R.drawable.theme2_fill__button_color));//當我按下上方按鈕後將顏色改變
                shop_cart.setBackground(getResources().getDrawable(R.drawable.select_btn_color));//當我按下上方按鈕後將顏色改變
                ArrayList<String> nameList = new ArrayList<>();
                ArrayList<String> articleList = new ArrayList<>();
                ArrayList<String> titlelist = new ArrayList<>();
                ArrayList<String> endtimelist= new ArrayList<>();
                ArrayList<String> id = new ArrayList<>();

                post_add.setVisibility(View.VISIBLE);//顯示新增貼文按鈕
                db.collection(Constants.KEY_COLLECTION_USERS)
                        .document(mAuth.getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    String hobby = documentSnapshot.getString("hobby");
                                    String uhobby = String.valueOf(hobby);
                                    db.collection("post")
//                                            .whereEqualTo("hobby",uhobby)//智慧篩選，將使用者的嗜好類別與貼文的嗜好類別做篩選
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                                            id.add(doc.getId());
                                                            docobject b = doc.toObject(docobject.class);
                                                            nameList.add(b.name);
                                                            articleList.add(b.article);
                                                            titlelist.add(b.title);
                                                            endtimelist.add(b.endtime);
                                                            shopAdapter = new shopAdapter(Shop_screen.this, nameList,articleList,titlelist,endtimelist,id);
                                                        }
                                                        recyclerView.setLayoutManager(new LinearLayoutManager(Shop_screen.this));
                                                        recyclerView.setAdapter(shopAdapter);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });

        shop_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shop_cart.setBackground(getResources().getDrawable(R.drawable.theme2_fill__button_color));//當我按下上方按鈕後將顏色改變
                shop_post.setBackground(getResources().getDrawable(R.drawable.select_btn_color));//當我按下上方按鈕後將顏色改變
                post_add.setVisibility(View.GONE);
                ArrayList<String> cartbuy = new ArrayList<>();
                ArrayList<String> cartid = new ArrayList<>();
                db.collection( Constants.KEY_COLLECTION_USERS + "/"+mAuth.getUid()+"/cart")//抓取使用者資料加到arraylsit裡，再將arraylsit傳到shopcartAdapter
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot doc : task.getResult()) {
                                        docobject b = doc.toObject(docobject.class);
                                        cartbuy.add(b.title);
                                        cartid.add(doc.getId());
                                        shopcartAdapter = new shopcartAdapter(Shop_screen.this, cartbuy,cartid);
                                    }
                                    recyclerView.setLayoutManager(new LinearLayoutManager(Shop_screen.this));
                                    recyclerView.setAdapter(shopcartAdapter);
                                }
                            }
                        });
            }
        });
    }

    private void setListener(){
        relativeLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(0);//recyclerView回到頂端
            }
        });//recyclerView回到頂端
        post_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openaddpostDialog();//呼叫openaddpostDialog
            }
        });//呼叫openaddpostDialog
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
                        startActivity(new Intent(getApplicationContext(), Notification_screen.class));
                        overridePendingTransition(0,0);
                        return true;

                }
                return false;
            }
        });

    }

    private void openaddpostDialog(){
        adddialog.setContentView(R.layout.dialog_post_add);//開啟post_add畫面
        adddialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//背景為透明
        Button add_hobby =adddialog.findViewById(R.id.add_hobby);
        Button add_endtime =adddialog.findViewById(R.id.add_endtime);
        Button next = adddialog.findViewById(R.id.next);
        ImageView exit= adddialog.findViewById(R.id.exit);
        EditText add_title =adddialog.findViewById(R.id.add_title);
        EditText add_art = adddialog.findViewById(R.id.add_art);
        Button add_format = adddialog.findViewById(R.id.add_format);
        ImageView add_image = adddialog.findViewById(R.id.add_imageView);
        ArrayList<String> storename= new ArrayList<>();
        ArrayList<String> storeprice= new ArrayList<>();
        HashMap<String,String>firebase_Store = new HashMap<>();
        HashMap<String,String>firebase_sell = new HashMap<>();
        add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uris.clear();//將uris清空
                Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);//開啟手機相簿
                picker.setType("image/*");
                picker.addCategory(Intent.CATEGORY_OPENABLE);
                picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(picker,101);//將值傳出到onActivityResult
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adddialog.cancel();//關閉post_add畫面;
            }
        });
        add_hobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Shop_screen.this)//建立一個alertDialog
                        .setSingleChoiceItems(hobby, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                no = which;
                            }
                        })
                        .setPositiveButton("送出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                add_hobby.setText(hobby[no]);//所選的到的嗜好類別顯示在add_hobby上
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });

        add_format.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setContentView(R.layout.dialog_add_format);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                ArrayList<addformat> addformatsArrayList = new ArrayList<>();
                LinearLayout linearLayout = dialog.findViewById(R.id.linear_list);
                Button add_list = dialog.findViewById(R.id.add_list);
                Button format_sure = dialog.findViewById(R.id.format_sure);
                ImageView  add_close = dialog.findViewById(R.id.add_close);
                storename.clear();// HashMap清空
                storeprice.clear();// HashMap清空
                add_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();//關閉add_format
                    }
                });
                format_sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //將輸入的品項傳到storename、storeprice
                        for (int i= 0;i<linearLayout.getChildCount();i++){
                            View addf_View2= linearLayout.getChildAt(i);
                            EditText addf_name= (EditText) addf_View2.findViewById(R.id.addf_name);
                            EditText addf_price= (EditText) addf_View2.findViewById(R.id.addf_price);
                            addformat addformat = new addformat();
                            addformat.setName(addf_name.getText().toString());
                            addformat.setPrice(addf_price.getText().toString());
                            addformatsArrayList.add(addformat);
                        }
                        for (int i= 0;i<addformatsArrayList.size();i++){
                            addformat addformat= addformatsArrayList.get(i);
                            storename.add(addformat.getName());
                            storeprice.add(addformat.getPrice());
                        }
                        StringBuilder stringBuilder = new StringBuilder();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        for (int i= 0;i<storename.size();i++){
                            stringBuilder.append(storename.get(i)+",");
                            stringBuilder2.append(storeprice.get(i)+",");
                        }
                        add_format.setText(stringBuilder.toString());//顯示所有品項名稱
                        //將品項價錢存到firebase_Store、firebase_sell
                        for (int i= 0;i<storename.size();i++){
                            firebase_Store.put("item_name"+i,storename.get(i));
                            firebase_Store.put("item_price"+i,storeprice.get(i));
                            firebase_sell.put("item_name"+i,storename.get(i));
                            firebase_sell.put("item_price"+i,storeprice.get(i));
                        }

                        dialog.cancel();
                    }
                });

                add_list.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View addf_View = getLayoutInflater().inflate(R.layout.add_format_click,null,false);//新增品項欄位
                        ImageView close=(ImageView) addf_View.findViewById(R.id.close);
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                linearLayout.removeView(addf_View);
                            }
                        });
                        linearLayout.addView(addf_View);
                    }
                });
                dialog.show();
            }
        });

        add_endtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);      //取得現在的日期年月日
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        int month1 =month+1;
                        String datetime = String.valueOf(year) + "/" + String.valueOf(month1) + "/" + String.valueOf(day);
                        add_endtime.setText(datetime);   //取得選定的日期指定給日期編輯框
                    }
                }, year, month, day).show();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suredialog.setContentView(R.layout.dialog_post_add_sure);
                suredialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button sure = suredialog.findViewById(R.id.sure);
                Button nsure = suredialog.findViewById(R.id.not_sure);
                Button sure_hobby = suredialog.findViewById(R.id.sure_hobby);
                Button sure_endtime = suredialog.findViewById(R.id.sure_endtime);
                TextView sure_title = suredialog.findViewById(R.id.sure_title);
                TextView sure_art = suredialog.findViewById(R.id.sure_art);
                Button sure_format = suredialog.findViewById(R.id.sure_format);
                ImageView sure_image = suredialog.findViewById(R.id.sure_imageView);

                db.collection(Constants.KEY_COLLECTION_USERS)//抓取使用者名稱
                        .document(mAuth.getCurrentUser().getUid())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot documentSnapshot =task.getResult();
                                    String name = documentSnapshot.getString("name");
                                    String addtitle=add_title.getText().toString();
                                    String addhobby=add_hobby.getText().toString();
                                    String addformat=add_format.getText().toString();
                                    String addendtime = add_endtime.getText().toString();
                                    String addart= add_art.getText().toString();
                                    sure_title.setText(addtitle);
                                    sure_hobby.setText(addhobby);
                                    sure_format.setText(addformat);
                                    sure_endtime.setText(addendtime);
                                    sure_art.setText(addart);
                                    sure_image.setImageDrawable(add_image.getDrawable());
                                    sure.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String size = String.valueOf(storename.size());
                                            firebase_Store.put("name",name);
                                            firebase_Store.put("title",addtitle);
                                            firebase_Store.put("endtime",addendtime);
                                            firebase_Store.put("hobby",addhobby);
                                            firebase_Store.put("article",addart);
                                            firebase_Store.put("size",size);
                                            try {
                                                db.collection("post")//儲存資料到post
                                                        .add(firebase_Store)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                String id = documentReference.getId();
                                                                firebase_sell.put("name",name);
                                                                firebase_sell.put("title",addtitle);
                                                                firebase_sell.put("endtime",addendtime);
                                                                firebase_sell.put("hobby",addhobby);
                                                                firebase_sell.put("article",addart);
                                                                firebase_sell.put("size",size);
                                                                for(int i=0;i<uris.size();i++){
                                                                    storageRef.child("post").child(id).child("title"+i)
                                                                            .putFile(uris.get(i))
                                                                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                                                }
                                                                            });
                                                                }
                                                                db.collection(Constants.KEY_COLLECTION_USERS + "/"+mAuth.getUid()+"/sell")//儲存資料到sell
                                                                        .document(id)
                                                                        .set(firebase_sell)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                final NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                                                                String id4 = "channel_1"; //自定义设置通道ID属性
                                                                                String description = "123";//自定义设置通道描述属性
                                                                                int importance = NotificationManager.IMPORTANCE_HIGH;
                                                                                NotificationChannel mChannel = new NotificationChannel(id4, "123", importance);
                                                                                manager.createNotificationChannel(mChannel);
                                                                                Notification notification = new Notification.Builder(Shop_screen.this,id4)
                                                                                        .setContentTitle("揪inus")//主題
                                                                                        .setSmallIcon(R.drawable.login_logo)//设置通知小图标
                                                                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.login_logo))//设置通知大图标
                                                                                        .setContentText("您已建立團購")//设置通知内容
                                                                                        .setAutoCancel(true)//设置自动删除通知
                                                                                        .build();
                                                                                manager.notify((int) System.currentTimeMillis(),notification);
                                                                            }
                                                                        });
                                                                startActivity(new Intent(getApplicationContext(),Shop_screen.class));
                                                                overridePendingTransition(0,0);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d("Demo",e.getMessage());
                                                            }
                                                        });
                                            }catch (Exception e){}
                                        }
                                    });
                                    nsure.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            suredialog.cancel();
                                        }
                                    });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Demo", e.getMessage());
                            }
                        });
                suredialog.show();
            }
        });
        adddialog.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&& requestCode==101) {
            ImageView image = adddialog.findViewById(R.id.add_imageView);
            if (data.getData() != null) {      // 選一張照片
                Uri selectedImage = data.getData();//取得uri
                image.setImageURI(selectedImage);  //顯示在imageview
            } else if (data.getClipData() != null) { //選多張圖片
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri selectedImages = data.getClipData().getItemAt(i).getUri();//取得uri
                    uris.add(selectedImages);//加入到uris陣列裡
                    image.setImageURI(selectedImages);  //顯示在imageview，但只會顯示最後一張
                }
            }
        }
    }
}