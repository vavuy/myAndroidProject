package com.example.inus.Activity.addEvent;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inus.Activity.Home_screen;
import com.example.inus.Activity.Setting.BaseActivity;
import com.example.inus.R;
import com.example.inus.adapter.Event.ResultAdapter;
import com.example.inus.databinding.ActivityFinishEventBinding;
import com.example.inus.databinding.DialogAddFormatBinding;
import com.example.inus.databinding.DialogPostAddBinding;
import com.example.inus.databinding.DialogPostAddSureBinding;
import com.example.inus.firebase.FCMMessages;
import com.example.inus.listeners.Callback;
import com.example.inus.model.addformat;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class _finishEvent extends BaseActivity implements Callback {

    private ActivityFinishEventBinding binding;
    private PreferenceManager preferenceManager;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    private Dialog adddialog, suredialog, dialog;
    private DialogPostAddBinding postAddBinding;
    private DialogAddFormatBinding addFormatBinding;
    private DialogPostAddSureBinding addSureBinding;

    private ArrayList<String> storename= new ArrayList<>();
    private ArrayList<String> storeprice= new ArrayList<>();
    private HashMap<String,String>firebase_Store = new HashMap<>();
    private HashMap<String,String>firebase_sell = new HashMap<>();
    private ArrayList<Uri> uris = new ArrayList<>();

    private String[] hobby ={"美妝保養","教育學習","居家婦幼","醫療保健","視聽娛樂","流行服飾","旅遊休閒","３Ｃ娛樂","人氣食品"};
    private boolean isDialogDone = false;
    private int fwhich= 0;
    int width , heigth ;
    private String[] FUID = null;
    private ArrayList<String> test = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFinishEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        preferenceManager = new PreferenceManager(getApplicationContext());

        postAddBinding = DialogPostAddBinding.inflate(getLayoutInflater());
        addFormatBinding = DialogAddFormatBinding.inflate(getLayoutInflater());
        addSureBinding = DialogPostAddSureBinding.inflate(getLayoutInflater());

        adddialog = new Dialog(this);
        suredialog = new Dialog(this);
        dialog = new Dialog(this);

        width = (int)(getResources().getDisplayMetrics().widthPixels*0.85);
        heigth = (int)(getResources().getDisplayMetrics().heightPixels*0.85);

        // 用Bundle 將時間計算用的時段傳過來
        final Bundle stringArraylist = getIntent().getExtras();
        ArrayList<String> SResults = stringArraylist.getStringArrayList(Constants.KEY_COLLECTION_START_EVENT);
        ArrayList<String> EResults = stringArraylist.getStringArrayList(Constants.KEY_COLLECTION_END_EVENT);
        FUID = stringArraylist.getStringArray("FUID");

        RecyclerView recyclerView = findViewById(R.id.recyclerView_Result);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ResultAdapter resultAdapter = new ResultAdapter(SResults, EResults);
        recyclerView.setAdapter(resultAdapter);
        resultAdapter.notifyDataSetChanged();

        String title = preferenceManager.getString(Constants.KEY_EVENT_TITLE);
        String location = preferenceManager.getString(Constants.KEY_EVENT_LOCATION);
        String description = preferenceManager.getString(Constants.KEY_EVENT_DESCRIPTION);

        binding.textViewTitle.setText(title);
        binding.textViewLocation.setText(location);

        binding.button.setOnClickListener(view ->  finish() );
//        binding.button.setOnClickListener(view -> sendNotifications("test title","test des"));
        binding.btnEventSubmit.setOnClickListener(view ->  {
            try{
                if(preferenceManager.getBoolean(Constants.KEY_ISGROUPGBUY) && !isDialogDone  ){   // true

                    adddialog.setContentView(postAddBinding.getRoot());//開啟post_add畫面
                    adddialog.getWindow().setLayout(width,heigth);
                    adddialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//背景為透明

                    // picker image
                    postAddBinding.addImageView.setOnClickListener(view1 -> {
                        uris.clear();//將uris清空
                        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);//開啟手機相簿
                        picker.setType("image/*");
                        picker.addCategory(Intent.CATEGORY_OPENABLE);
                        picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        startActivityForResult(picker,101);//將值傳出到onActivityResult
                    });

                    // close the dialog
                    postAddBinding.exit.setOnClickListener(view1 -> adddialog.cancel());
                    // 選擇該商品嗜好類別
                    postAddBinding.addHobby.setOnClickListener(view1 -> {
                        new AlertDialog.Builder(_finishEvent.this)
                                .setSingleChoiceItems(hobby,fwhich,((dialogInterface, which) -> fwhich = which))
                                .setPositiveButton("送出", ((dialogInterface, i) -> postAddBinding.addHobby.setText(hobby[fwhich])))
                                .setNegativeButton("取消", null)
                                .show();
                    });

                    // 跳出新增品項
                    postAddBinding.addFormat.setOnClickListener(view1 -> {

                        int dwidth = (int)( width*1.1);
                        int dheigth = (int)( heigth*1.1);
                        dialog.setContentView(addFormatBinding.getRoot());
                        dialog.getWindow().setLayout(dwidth , dheigth);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        ArrayList<addformat> addformatsArrayList = new ArrayList<>();

                        LinearLayout linearLayout = dialog.findViewById(R.id.linear_list);
                        storename.clear();// HashMap清空
                        storeprice.clear();// HashMap清空

                        addFormatBinding.addClose.setOnClickListener(view2 -> dialog.cancel());
                        addFormatBinding.addList.setOnClickListener(view2 ->{
                            View addf_View = getLayoutInflater().inflate(R.layout.add_format_click,null,false);//新增品項欄位
                            ImageView close = (ImageView) addf_View.findViewById(R.id.close);
                            close.setOnClickListener(view3 -> linearLayout.removeView(addf_View));
                            linearLayout.addView(addf_View);
                        } );
                        dialog.show();

                        addFormatBinding.formatSure.setOnClickListener(view2 -> {
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
                            postAddBinding.addFormat.setText(stringBuilder.toString());//顯示所有品項名稱
                            //將品項價錢存到firebase_Store、firebase_sell
                            for (int i= 0;i<storename.size();i++){
                                firebase_Store.put("item_name"+i,storename.get(i));
                                firebase_Store.put("item_price"+i,storeprice.get(i));
                                firebase_sell.put("item_name"+i,storename.get(i));
                                firebase_sell.put("item_price"+i,storeprice.get(i));
                            }
                            dialog.cancel();
                        });

                    });
                    // 設定結束時間
                    postAddBinding.addEndtime.setOnClickListener(view1 -> {
                        setDate();
                    });
                    // 進入確認頁面，並且將資料寫入資料庫
                    postAddBinding.next.setOnClickListener(view1 -> showSureDialog());

                    adddialog.show();

                }else{ isDialogDone = true; }

            }catch (Exception e){
                Log.d("demo", e.getMessage());
            }finally {
                if(isDialogDone){
                    HashMap<String,Object> data = new HashMap<>();
                    data.put(Constants.KEY_EVENT_TITLE,title);
                    data.put(Constants.KEY_EVENT_START_TIME,getStringToDate(preferenceManager.getString(Constants.KEY_COLLECTION_START_TINE)));
                    data.put(Constants.KEY_EVENT_END_TIME,getStringToDate(preferenceManager.getString(Constants.KEY_COLLECTION_END_TIME)));
                    data.put(Constants.KEY_EVENT_LOCATION,location);
                    data.put(Constants.KEY_EVENT_DESCRIPTION, description);

                    // 寫入自己的DB
                    data.put(Constants.KEY_EVENT_STATE , "main");
                    db.collection(Constants.KEY_COLLECTION_USERS + "/" + Constants.UID + "/group" )
                            .document().set(data);
                    //寫入別人的DB
                    data.put(Constants.KEY_EVENT_STATE , "0");
                    for(String id : FUID){
                        db.collection(Constants.KEY_COLLECTION_USERS + "/" + id + "/group")
                                .document().set(data);
                    }

                    sendNotifications("已建立" + title + "揪團事件！" ,"地點："+location + "\n備註：" + description);
                    startActivity(new Intent(this, Home_screen.class));
                }
            }
            });
    }

    private void setDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);      //取得現在的日期年月日
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                int month1 =month+1;
                String datetime = String.valueOf(year) + "/" + String.valueOf(month1) + "/" + String.valueOf(day);
                postAddBinding.addEndtime.setText(datetime);   //取得選定的日期指定給日期編輯框
            }
        }, year, month, day)
                .show();
    }

    private  void showSureDialog(){
        suredialog.setContentView(addSureBinding.getRoot());
        suredialog.getWindow().setLayout(width,heigth);
        suredialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(Constants.UID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot =task.getResult();

                            String name = documentSnapshot.getString("name");
                            String addtitle=postAddBinding.addTitle.getText().toString();
                            String addhobby=postAddBinding.addHobby.getText().toString();
                            String addformat= postAddBinding.addFormat.getText().toString();
                            String addendtime = postAddBinding.addEndtime.getText().toString();
                            String addart=  postAddBinding.addArt.getText().toString();

                            addSureBinding.sureTitle.setText(addtitle);
                            addSureBinding.sureHobby.setText(addhobby);
                            addSureBinding.sureFormat.setText(addformat);
                            addSureBinding.sureEndtime.setText(addendtime);
                            addSureBinding.sureArt.setText(addart);
                            addSureBinding.sureImageView.setImageDrawable( postAddBinding.addImageView.getDrawable());

                            addSureBinding.sure.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String size = String.valueOf(storename.size());

                                    firebase_Store.put("name",name);
                                    firebase_Store.put("title",addtitle);
                                    firebase_Store.put("endtime",addendtime);
                                    firebase_Store.put("hobby",addhobby);
                                    firebase_Store.put("article",addart);
                                    firebase_Store.put("size",size);

                                    firebase_Store.put(Constants.KEY_EVENT_STATE , "main");

                                    try {
                                        db.collection(Constants.KEY_COLLECTION_USERS + "/" + Constants.UID + "/groupBuy")
                                                .add(firebase_Store)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {

                                                        firebase_Store.put(Constants.KEY_EVENT_STATE , "0" );
                                                        for(String uid : FUID){
                                                            db.collection(Constants.KEY_COLLECTION_USERS + "/" + uid + "/groupBuy")
                                                                    .document().set(firebase_Store);
                                                        }

                                                        String id = documentReference.getId();

                                                        firebase_sell.put("name",name);
                                                        firebase_sell.put("title",addtitle);
                                                        firebase_sell.put("endtime",addendtime);
                                                        firebase_sell.put("hobby",addhobby);
                                                        firebase_sell.put("article",addart);
                                                        firebase_sell.put("size",size);

                                                        for(int i=0;i<uris.size();i++){
                                                            storageRef.child("post").child(id).child("title"+i)
                                                                    .putFile(uris.get(i));
                                                        }

                                                        db.collection(Constants.KEY_COLLECTION_USERS + "/"+ Constants.UID+"/sell")//儲存資料到sell
                                                                .document(id)
                                                                .set(firebase_sell)
                                                                .addOnCompleteListener(task1 -> {
                                                                    isDialogDone = true;
                                                                    sendNotifications("JoinUs","您已建立團購");

                                                                    suredialog.dismiss();
                                                                    adddialog.dismiss();
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(e -> Log.d("Demo", e.getMessage()));
                                    }catch (Exception e){}
                                }
                            });
                            addSureBinding.notSure.setOnClickListener(view -> suredialog.cancel());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.d("Demo", e.getMessage()));

        suredialog.show();

    }

    private void sendNotifications(String title , String context){

        for (String s : FUID) {
            db.collection(Constants.KEY_COLLECTION_USERS).document(s).get()
            .addOnCompleteListener(task -> {
                if(task.isSuccessful() && task.getResult().get(Constants.KEY_FCM_TOKEN) != null ){
                    try{
//                        Toast.makeText(getApplicationContext(), "" + task.getResult().get(Constants.KEY_FCM_TOKEN) , Toast.LENGTH_SHORT).show();
                        responseCallback("" + task.getResult().get(Constants.KEY_FCM_TOKEN) );
                    }catch (Exception e){ e.printStackTrace();}
                }
            })
            .addOnFailureListener(e -> {
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
        new FCMMessages().sendMessageMulti(this, new JSONArray(test), title, context, null);

        final NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String channel = "channel 2";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(channel, "Test", importance);
        manager.createNotificationChannel(mChannel);
        Notification notification = new Notification.Builder(getApplicationContext(),channel)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.login_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.login_logo))
                .setContentText(context)
                .setAutoCancel(true)
                .build();
        manager.notify((int) System.currentTimeMillis(),notification);

        try{
            Thread.sleep(1500);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 101) {
            if (data.getData() != null) {      // 選一張照片
                Uri selectedImage = data.getData();//取得uri
                postAddBinding.addImageView.setImageURI(selectedImage);  //顯示在imageview
            } else if (data.getClipData() != null) { //選多張圖片
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri selectedImages = data.getClipData().getItemAt(i).getUri();//取得uri
                    uris.add(selectedImages);//加入到uris陣列裡
                    postAddBinding.addImageView.setImageURI(selectedImages);  //顯示在imageview，但只會顯示最後一張
                }
            }
        }
    }

    private Date getStringToDate(String s)  {
        Date date = new Date();
        try {
            date = Constants.SDFDateTime.parse(s);
        }catch (Exception e){
            e.printStackTrace();
        }
        return date;
    };

    @Override
    public void responseCallback(String data) {
        if(!data.equals(null)){
            test.add(data);
        }

    }
}