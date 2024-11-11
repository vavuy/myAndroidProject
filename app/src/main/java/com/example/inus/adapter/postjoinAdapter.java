package com.example.inus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inus.R;
import com.example.inus.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class postjoinAdapter extends RecyclerView.Adapter<postjoinAdapter.MyViewHolder>{
    Context context;
    ArrayList<String> itemname,itemprice;
    String posid;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private StorageReference storageRef= FirebaseStorage.getInstance().getReference();

    public postjoinAdapter(Context context,ArrayList<String>itemname,ArrayList<String>itemprice,String posid) {
        this.context = context;
        this.itemname=itemname;
        this.itemprice=itemprice;
        this.posid=posid;


    }
    @NonNull
    @Override
    public postjoinAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.post_join_view,parent,false);
        return new postjoinAdapter.MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull postjoinAdapter.MyViewHolder holder, int position) {
        holder.post_item_name.setText(itemname.get(position));
        holder.post_item_price.setText(itemprice.get(position));
        HashMap<String,Object> TotalData =new HashMap<>();

        db.collection("post")
                .document(posid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String size = documentSnapshot.getString("size");
                            int sizeint = Integer.parseInt(size);
                            for (int i = 0; i <sizeint;i++) {
                                if (position ==i) {
                                    holder.padd.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String pr = holder.post_item_price.getText().toString();
                                            String co = holder.count.getText().toString();
                                            int num = Integer.valueOf(co).intValue();
                                            int pr1 = Integer.valueOf(pr).intValue();
                                            int add = num + 1;
                                            String total = String.valueOf(add);
                                            holder.count.setText(total);
                                            String co1 = holder.count.getText().toString();
                                            int num1 = Integer.valueOf(co1).intValue();
                                            int ss = num1 * pr1;
                                            String s = String.valueOf(ss);
                                            String name = itemname.get(position);
                                            TotalData.put("total", s);
                                            TotalData.put("item",holder.post_item_price.getText().toString());
                                            TotalData.put("num",holder.count.getText().toString());
                                            db.collection(Constants.KEY_COLLECTION_USERS+"/"+ mAuth.getUid() + "/cart/"+posid+"/prices")
                                                    .document(name)
                                                    .set(TotalData)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                        }
                                                    });
                                        }
                                    });
                                    holder.pv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String pr = holder.post_item_price.getText().toString();
                                            String co = holder.count.getText().toString();
                                            int pr1 = Integer.valueOf(pr).intValue();
                                            int num = Integer.valueOf(co).intValue();
                                            if (num != 0) {
                                                int add = num - 1;
                                                String total = String.valueOf(add);
                                                holder.count.setText(total);
                                                String co1 = holder.count.getText().toString();
                                                int num1 = Integer.valueOf(co1).intValue();
                                                int ss = num1 * pr1;
                                                String s = String.valueOf(ss);
                                                String name = itemname.get(position);
                                                TotalData.put("total" , s);
                                                TotalData.put("item",holder.post_item_price.getText().toString());
                                                TotalData.put("num",holder.count.getText().toString());
                                                db.collection(Constants.KEY_COLLECTION_USERS+"/"+ mAuth.getUid() + "/cart/"+posid+"/prices")
                                                        .document(name)
                                                        .set(TotalData)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                            }
                                                        });
                                            }else{

                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
    }
    @Override
    public int getItemCount() {
        return itemname.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView post_item_name,post_item_price,count;
        Button padd,pv;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            post_item_name = itemView.findViewById(R.id.post_item_name);
            post_item_price = itemView.findViewById(R.id.post_item_price);
            count = itemView.findViewById(R.id.count);
            padd = itemView.findViewById(R.id.padd);
            pv=itemView.findViewById(R.id.pv);


        }
    }
}
