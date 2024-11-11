package com.example.inus.adapter;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inus.R;
import com.example.inus.model.addformat;
import com.example.inus.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class cartAdapter extends RecyclerView.Adapter<cartAdapter.MyViewHolder>{
    Context context;
    ArrayList<String> buy,buyid;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    Dialog buydialog,selldialog,dialog;
    buyAdapter buyAdapter;
    private String[] chobby ={"美妝保養","教育學習","居家婦幼","醫療保健","視聽娛樂","流行服飾","旅遊休閒","3C娛樂","人氣食品"};
    private int Tpye;
    int no= 0;
    ArrayList<Uri> uris = new ArrayList<>();
    public cartAdapter(Context context,ArrayList<String>buy,ArrayList<String>buyid,int tpye) {
        this.context = context;
        this.buy=buy;
        this.buyid =buyid;
        this.Tpye = tpye;
    }
    @NonNull
    @Override
    public cartAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.noneadapter_view,parent,false);
        return new cartAdapter.MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull cartAdapter.MyViewHolder holder, int position) {
        buydialog = new Dialog(context);
        selldialog = new Dialog(context);
        dialog =new Dialog(context);
        if (Tpye ==0){
            holder.name.setText("已參加"+buy.get(position)+"團購活動");
            holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> namelist = new ArrayList<>();
                    ArrayList<String> numlist = new ArrayList<>();
                    ArrayList<String> priceslist =new ArrayList<>();
                    buydialog.setContentView(R.layout.buy_view);
                    buydialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    RecyclerView recyclerView = buydialog.findViewById(R.id.recyclerView);
                    TextView total = buydialog.findViewById(R.id.buy_total);
                    Button sure = buydialog.findViewById(R.id.buy_sure);
                    ImageView close = buydialog.findViewById(R.id.buy_close);
                    db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/buy")
                            .document(buyid.get(position))
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot= task.getResult();
                                        String size = documentSnapshot.getString("size");
                                        int sizeint = Integer.parseInt(size);
                                        String totals = documentSnapshot.getString("total");
                                        total.setText(totals);
                                        for(int i=0; i<sizeint;i++){
                                            String itemname = documentSnapshot.getString("itemname"+i);
                                            String itemnum = documentSnapshot.getString("itemnum"+i);
                                            String itemprices = documentSnapshot.getString("itemprices"+i);
                                            namelist.add(itemname);
                                            numlist.add(itemnum);
                                            priceslist.add(itemprices);
                                        }
                                        buyAdapter = new buyAdapter(context,namelist,numlist,priceslist);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                        recyclerView.setAdapter(buyAdapter);
                                    }
                                }
                            });
                    close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            buydialog.cancel();
                        }
                    });
                    sure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            buydialog.cancel();
                        }
                    });
                    buydialog.show();
                }
            });
        }else{
            holder.name.setText(buy.get(position)+"團購活動");
            holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String,String> firebase_Store = new HashMap<>();
                    HashMap<String,String>firebase_sell = new HashMap<>();
                    ArrayList<String> storename= new ArrayList<>();
                    ArrayList<String> storeprice= new ArrayList<>();
                    selldialog.setContentView(R.layout.seller_post);
                    selldialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    Button sure =selldialog.findViewById(R.id.sure);
                    ImageView exit = selldialog.findViewById(R.id.exit);
                    EditText title = selldialog.findViewById(R.id.sell_title);
                    Button hobby = selldialog.findViewById(R.id.sell_hobby);
                    Button format = selldialog.findViewById(R.id.sell_format);
                    Button endtime = selldialog.findViewById(R.id.sell_endtime);
                    Button del = selldialog.findViewById(R.id.del);
                    ImageView  imageView =selldialog.findViewById(R.id.sell_imageView);
                    EditText art = selldialog.findViewById(R.id.sell_art);
                    exit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            selldialog.cancel();
                        }
                    });
                    db.collection( Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/sell")
                            .document(buyid.get(position))
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        title.setText(documentSnapshot.getString("title"));
                                        hobby.setText(documentSnapshot.getString("hobby"));
                                        endtime.setText(documentSnapshot.getString("endtime"));
                                        art.setText(documentSnapshot.getString("article"));
                                    }
                                }
                            });

                    storageRef.child("post").child(buyid.get(position)).child("title0")
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(context)
                                            .load(uri)
                                            .into(imageView);
                                }
                            });

                    hobby.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(context)
                                    .setSingleChoiceItems(chobby, 0, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            no = which;
                                        }
                                    })
                                    .setPositiveButton("送出", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            hobby.setText(chobby[no]);
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
                    endtime.setOnClickListener(new View.OnClickListener() {
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
                                    endtime.setText(datetime);   //取得選定的日期指定給日期編輯框
                                }
                            }, year, month, day).show();
                        }
                    });
                    format.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.setContentView(R.layout.dialog_add_format);
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            ArrayList<addformat> addformatsArrayList = new ArrayList<>();
                            LinearLayout linearLayout = dialog.findViewById(R.id.linear_list);
                            Button add_list = dialog.findViewById(R.id.add_list);
                            Button format_sure = dialog.findViewById(R.id.format_sure);
                            ImageView  add_close = dialog.findViewById(R.id.add_close);
                            storename.clear();
                            storeprice.clear();
                            add_close.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.cancel();
                                }
                            });
                            format_sure.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
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
                                    format.setText(stringBuilder.toString());
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
                                    LayoutInflater li  = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    View addf_View = li.inflate(R.layout.add_format_click,null,false);
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
                    sure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            db.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(mAuth.getCurrentUser().getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()) {
                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                String size = String.valueOf(storename.size());
                                                firebase_Store.put("name",documentSnapshot.getString("name"));
                                                firebase_Store.put("title",title.getText().toString());
                                                firebase_Store.put("hobby",hobby.getText().toString());
                                                firebase_Store.put("endtime",endtime.getText().toString());
                                                firebase_Store.put("article",art.getText().toString());
                                                firebase_Store.put("size",size);
                                                firebase_sell.put("name",documentSnapshot.getString("name"));
                                                firebase_sell.put("title",title.getText().toString());
                                                firebase_sell.put("hobby",hobby.getText().toString());
                                                firebase_sell.put("endtime",endtime.getText().toString());
                                                firebase_sell.put("article",art.getText().toString());
                                                firebase_sell.put("size",size);
                                                for(int i=0;i<uris.size();i++){
                                                    storageRef.child("post").child(buyid.get(position)).child("title"+i)
                                                            .putFile(uris.get(i))
                                                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                                }
                                                            });
                                                }
                                                db.collection("post")
                                                        .document(buyid.get(position))
                                                        .set(firebase_Store)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                            }
                                                        });
                                                db.collection( Constants.KEY_COLLECTION_USERS + "/" +mAuth.getUid()+"/sell")
                                                        .document(buyid.get(position))
                                                        .set(firebase_sell)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                            }
                                                        });
                                            }
                                        }});
                            selldialog.cancel();
                        }
                    });
                    del.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            selldialog.cancel();
                            db.collection("post")
                                    .document(buyid.get(position))
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                        }
                                    });
                            db.collection(Constants.KEY_COLLECTION_USERS +"/"+mAuth.getUid()+"/sell")
                                    .document(buyid.get(position))
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                        }
                                    });
                        }
                    });
                    selldialog.show();
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return buy.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ConstraintLayout constraintLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            constraintLayout = itemView.findViewById(R.id.constraintlayout);
        }
    }
}
