package com.example.inus.adapter;

import static com.example.inus.R.*;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inus.R;
import com.example.inus.databinding.DialogAddFormatBinding;
import com.example.inus.databinding.DialogSellerPostBinding;
import com.example.inus.listeners.Callback;
import com.example.inus.model.addformat;
import com.example.inus.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.MyViewHolder> implements Callback {
    Context context;
    private ArrayList<String> events;
    private boolean isGroupBuy;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private String collectionPath;
    private Dialog selldialog, dialog;
    private DialogSellerPostBinding sellerPostBinding;
    private DialogAddFormatBinding addFormatBinding;
    int width, heigth ;

    private String[] chobby ={"美妝保養","教育學習","居家婦幼","醫療保健","視聽娛樂","流行服飾","旅遊休閒","3C娛樂","人氣食品"};
    int no= 0;
    ArrayList<Uri> uris = new ArrayList<>();

    public NotificationAdapter(Context context, ArrayList<String> events, boolean isGroupBuy) {
        this.context = context;
        this.events=events;
        this.isGroupBuy = isGroupBuy;
    }

    @NonNull
    @Override
    public NotificationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(layout.noneadapter_view,parent,false);

        width = (int)( parent.getWidth() *0.85);
        heigth = (int)( parent.getHeight() *1.15);

        return new NotificationAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.MyViewHolder holder, int position) {

        selldialog = new Dialog(context);
        dialog =new Dialog(context);

        sellerPostBinding = DialogSellerPostBinding.inflate(LayoutInflater.from(context));
        addFormatBinding = DialogAddFormatBinding.inflate(LayoutInflater.from(context));

        collectionPath ="group";
        if (isGroupBuy == true)  collectionPath = "groupBuy";

        //get data
        DocumentReference doc = db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.UID)
                .collection(collectionPath).document(events.get(position));
        //  分類
        doc.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.get(Constants.KEY_EVENT_STATE) != null){
                if(documentSnapshot.get(Constants.KEY_EVENT_STATE).equals("main")){
                    holder.row_cibstraubtLayout.setBackground(context.getResources().getDrawable(drawable.notice_border_main));
                }else if(documentSnapshot.get(Constants.KEY_EVENT_STATE).equals("1")){
                    holder.row_cibstraubtLayout.setBackground(context.getResources().getDrawable(drawable.notice_border_done));
                }else if(documentSnapshot.get(Constants.KEY_EVENT_STATE).equals("0")){
                    holder.row_cibstraubtLayout.setBackground(context.getResources().getDrawable(drawable.notice_border_yet));
                }else if(documentSnapshot.get(Constants.KEY_EVENT_STATE).equals("-1")){
                    holder.row_cibstraubtLayout.setBackground(context.getResources().getDrawable(drawable.notice_border_die));
                    holder.itemView.setOnTouchListener((view, motionEvent) -> true);
                }
            }
        });

        // setTitle
        doc.get().addOnSuccessListener(documentSnapshot ->{
                    holder.name.setText(setTitleText(""+ documentSnapshot.get(Constants.KEY_EVENT_TITLE),
                            "" + documentSnapshot.get(Constants.KEY_EVENT_STATE)));
                });

            if(!isGroupBuy){  // 揪團
                holder.itemView.setOnClickListener(view -> {
                    doc.get().addOnSuccessListener(documentSnapshot -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(dialog.getContext());
                        builder.setTitle(""+documentSnapshot.get(Constants.KEY_EVENT_TITLE))
                                .setMessage(setTitleText(""+ documentSnapshot.get(Constants.KEY_EVENT_TITLE),
                                        "" + documentSnapshot.get(Constants.KEY_EVENT_STATE)))
                                .setPositiveButton("確認",(dialogInterface, i) -> {
                                    if(documentSnapshot.get(Constants.KEY_EVENT_STATE).equals("0"))
                                        doc.update(Constants.KEY_EVENT_STATE,"1");
                                })
                                .setNegativeButton("取消",(dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                })
                                .show();
                    });
                });
            }
            else {  //團購
                holder.itemView.setOnClickListener(view -> {

                    HashMap<String,String> firebase_Store = new HashMap<>();
                    HashMap<String,String>firebase_sell = new HashMap<>();
                    ArrayList<String> storename= new ArrayList<>();
                    ArrayList<String> storeprice= new ArrayList<>();

                    selldialog.setContentView(sellerPostBinding.getRoot());
                    selldialog.getWindow().setLayout(width,heigth);
                    selldialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    Button sure =selldialog.findViewById(id.sure);
                    ImageView exit = selldialog.findViewById(id.exit);
                    EditText title = selldialog.findViewById(id.sell_title);
                    Button hobby = selldialog.findViewById(id.sell_hobby);
                    Button format = selldialog.findViewById(id.sell_format);
                    Button endtime = selldialog.findViewById(id.sell_endtime);
                    Button del = selldialog.findViewById(id.del);
                    ImageView  imageView =selldialog.findViewById(id.sell_imageView);
                    EditText art = selldialog.findViewById(id.sell_art);
                    exit.setOnClickListener(view1 -> selldialog.cancel());

                db.collection( Constants.KEY_COLLECTION_USERS+"/"+Constants.UID+"/groupBuy")
                        .document(events.get(position))
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

                    storageRef.child("post").child(events.get(position)).child("title0")
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
                            new androidx.appcompat.app.AlertDialog.Builder(context)
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
                              dialog.setContentView(addFormatBinding.getRoot());
                              dialog.getWindow().setLayout(width,heigth);
                              dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                              ArrayList<addformat> addformatsArrayList = new ArrayList<>();
                              LinearLayout linearLayout = dialog.findViewById(id.linear_list);
                              Button add_list = dialog.findViewById(id.add_list);
                              Button format_sure = dialog.findViewById(id.format_sure);
                              ImageView add_close = dialog.findViewById(id.add_close);
                              storename.clear();
                              storeprice.clear();
                              add_close.setOnClickListener(view1 -> dialog.cancel());
                              format_sure.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                      for (int i= 0;i<linearLayout.getChildCount();i++){
                                          View addf_View2= linearLayout.getChildAt(i);
                                          EditText addf_name= (EditText) addf_View2.findViewById(id.addf_name);
                                          EditText addf_price= (EditText) addf_View2.findViewById(id.addf_price);
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
                                      View addf_View = li.inflate(layout.add_format_click,null,false);
                                      ImageView close=(ImageView) addf_View.findViewById(id.close);
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
                          }}
                          );


                    sure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            db.collection("user")
                                    .document(Constants.UID)
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
                                                    storageRef.child("post").child(events.get(position)).child("title"+i)
                                                            .putFile(uris.get(i))
                                                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                                }
                                                            });
                                                }
                                                db.collection("post")
                                                        .document(events.get(position))
                                                        .set(firebase_Store)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                            }
                                                        });
                                                db.collection( Constants.KEY_COLLECTION_USERS + "/" +Constants.UID+"/sell")
                                                        .document(events.get(position))
                                                        .set(firebase_sell);
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
                                    .document(events.get(position))
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                        }
                                    });
                            db.collection("users/"+Constants.UID+"/sell")
                                    .document(events.get(position))
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                        }
                                    });
                        }
                    });
                    selldialog.show();
                });
            }


        // 長按刪除
        holder.itemView.setOnLongClickListener(v -> {
            AlertDialog.Builder alertDialog =
                    new AlertDialog.Builder(v.getContext());
            alertDialog.setTitle("刪除").setMessage("確定要刪除嗎")
                    .setPositiveButton("OK",(dialogInterface, i) -> {
                        db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.UID).collection(collectionPath)
                                .document(events.get(position)).delete();
                    }).setNegativeButton("取消",null).show();
            notifyItemRemoved(position);
            notifyDataSetChanged();
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public void responseCallback(String data) {

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout row_cibstraubtLayout;
        TextView name;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(id.name);
            row_cibstraubtLayout = (ConstraintLayout) itemView.findViewById(id.constraintlayout);
        }
    }

    private String setTitleText(String title, String state){
        String message ="test" + title + state;

        if(!isGroupBuy){
            if(state.equals("main")){
                message = title + "揪團活動";
            }else if(state.equals("0")){
                message = "您已被邀請參加 " + "" + title + " 活動，請確認是否參加??";
            }else if(state.equals("1")){
                message = title + "活動即將開始!!!";
            }
        }else {
            if(state.equals("main")){
                message = title + "團購活動";
            }else if(state.equals("0")){
                message = "已結束"+ title +"團購活動";
            }else if(state.equals("1")){
                message = title + "活動倒數中!!!";
            }
        }
        return message;
    }
}
