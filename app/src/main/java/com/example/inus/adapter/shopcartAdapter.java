package com.example.inus.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inus.R;
import com.example.inus.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class shopcartAdapter extends RecyclerView.Adapter<shopcartAdapter.MyViewHolder>{
    Context context;
    ArrayList<String> cartbuy,cartid;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private Dialog cartdialog,joindialog;
    private shopcartitemAdapter shopcartitemAdapter;
    private postjoinAdapter postjoinAdapter;
    public shopcartAdapter(Context context,ArrayList<String>cartbuy,ArrayList<String>cartid) {
        this.context = context;
        this.cartbuy=cartbuy;
        this.cartid=cartid;
    }
    @NonNull
    @Override
    public shopcartAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.shop_post_cart,parent,false);
        return new shopcartAdapter.MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull shopcartAdapter.MyViewHolder holder, int position) {
        cartdialog = new Dialog(context);
        String posid = cartid.get(position);
        holder.name.setText("已參加"+cartbuy.get(position)+"團購活動");
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,String>firebase_Store = new HashMap<>();
                ArrayList<String> nameList = new ArrayList<>();
                ArrayList<String> pricesList = new ArrayList<>();
                ArrayList<String> numlist = new ArrayList<>();
                ArrayList<String> totallist = new ArrayList<>();
                cartdialog.setContentView(R.layout.shop_cart_view);
                cartdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                RecyclerView recyclerView =cartdialog.findViewById(R.id.recyclerView);
                ImageView cart_close = cartdialog.findViewById(R.id.cart_close);
                Button cart_sure = cartdialog.findViewById(R.id.cart_sure);
                Button cart_revise = cartdialog.findViewById(R.id.cart_revise);
                TextView cart_total = cartdialog.findViewById(R.id.cart_total);
                db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/cart/"+posid+"/prices")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot doc : task.getResult()) {
                                        String itemname = doc.getId();
                                        String item_prices = doc.getString("item");
                                        String item_num = doc.getString("num");
                                        String item_total = doc.getString("total");
                                        nameList.add(itemname);
                                        pricesList.add(item_prices);
                                        numlist.add(item_num);
                                        totallist.add(item_total);
                                        shopcartitemAdapter = new shopcartitemAdapter(context,nameList,pricesList,numlist);
                                    }
                                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                    recyclerView.setAdapter(shopcartitemAdapter);
                                    int t =0;
                                    for(int i= 0;i<totallist.size();i++)
                                    {
                                        int o = Integer.parseInt(totallist.get(i));
                                        t+=o;
                                    }
                                    String t1 = String.valueOf(t);
                                    firebase_Store.put("total",t1);
                                    cart_total.setText(t1);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Demo",e.getMessage());
                            }
                        });

                cart_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cartdialog.cancel();
                    }
                });
                cart_sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String size = String.valueOf(nameList.size());
                        firebase_Store.put("size",size);
                        firebase_Store.put("title",cartbuy.get(position));
                        for (int i = 0; i<nameList.size();i++){
                            firebase_Store.put("itemname"+i,nameList.get(i));
                            firebase_Store.put("itemprices"+i,pricesList.get(i));
                            firebase_Store.put("itemnum"+i,numlist.get(i));
                        }

                        db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/buy")
                                .document(posid)
                                .set(firebase_Store)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });
                        cartdialog.cancel();
                        db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/cart/")
                                .document(posid)
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                    }
                                });
                    }
                });
                cart_revise.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for(int i=0 ; i< cartbuy.size();i++){
                            db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/cart/"+posid+"/"+cartbuy.get(i))
                                    .document("prices")
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                            db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/cart")
                                    .document(posid)
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                        }
                        cartdialog.cancel();
                        ArrayList<String> itemname = new ArrayList<>();
                        ArrayList<String> itemprice = new ArrayList<>();
                        joindialog = new Dialog(context);
                        joindialog.setContentView(R.layout.post_join);
                        joindialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        RecyclerView recyclerView = joindialog.findViewById(R.id.recyclerView);
                        TextView join_title = joindialog.findViewById(R.id.join_title);
                        ImageView join_exit = joindialog.findViewById(R.id.join_exit);
                        Button join_sumbit = joindialog.findViewById(R.id.join_sumbit);
                        db.collection("post")
                                .document(posid)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            String size = documentSnapshot.getString("size");
                                            join_title.setText(documentSnapshot.getString("title"));
                                            int sizeint = Integer.parseInt(size);
                                            for (int i= 0;i<sizeint;i++){
                                                String name = documentSnapshot.getString("item_name"+i);
                                                String price = documentSnapshot.getString("item_price"+i);
                                                itemprice.add(price);
                                                itemname.add(name);
                                            }
                                            postjoinAdapter = new postjoinAdapter(context,itemname,itemprice,posid);
                                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                            recyclerView.setAdapter(postjoinAdapter);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Demo",e.getMessage());
                                    }
                                });
                        join_exit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                joindialog.cancel();

                            }
                        });
                        join_sumbit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                joindialog.cancel();
                                HashMap<String,String> sure = new HashMap<>();
                                sure.put("title",cartbuy.get(position));
                                for(int i=0 ; i< cartbuy.size();i++){
                                    db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/cart/"+posid+"/"+cartbuy.get(i))
                                            .document("prices")
                                            .delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                    db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/cart")
                                            .document(posid)
                                            .delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                }
                                db.collection(Constants.KEY_COLLECTION_USERS+"/"+mAuth.getUid()+"/cart")
                                        .document(posid)
                                        .set(sure)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                            }
                                        });

                            }
                        });
                        joindialog.show();
                    }
                });
                cartdialog.show();
            }
        });
    }
    @Override
    public int getItemCount() { return cartbuy.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ConstraintLayout constraintLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            constraintLayout = itemView.findViewById(R.id.constraint);
        }
    }
}
