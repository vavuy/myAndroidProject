package com.example.inus.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inus.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class buyAdapter extends RecyclerView.Adapter<buyAdapter.MyViewHolder>{
    Context context;
    ArrayList<String> namelist,numlist,priceslist;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    public buyAdapter(Context context, ArrayList<String>namelist, ArrayList<String>numlist, ArrayList<String>priceslist) {
        this.context = context;
        this.namelist=namelist;
        this.numlist=numlist;
        this.priceslist=priceslist;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.buy_item,parent,false);
        return new MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(namelist.get(position));
        holder.num.setText(numlist.get(position));
        holder.price.setText(priceslist.get(position));
    }
    @Override
    public int getItemCount() {
        return namelist.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
       TextView name,num,price;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.buy_name);
            num = itemView.findViewById(R.id.buy_num);
            price =itemView.findViewById(R.id.buy_price);
        }
    }
}
