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

public class shopcartitemAdapter extends RecyclerView.Adapter<shopcartitemAdapter.MyViewHolder>{
    Context context;
    ArrayList<String> nameList,pricesList,numlist;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    public shopcartitemAdapter(Context context, ArrayList<String>nameList, ArrayList<String>pricesList, ArrayList<String>numlist) {
        this.context = context;
        this.nameList = nameList;
        this.pricesList= pricesList;
        this.numlist= numlist;

    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.shop_cart_buy,parent,false);
        return new MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(nameList.get(position));
        holder.prices.setText(pricesList.get(position));
        holder.num.setText(numlist.get(position));


    }
    @Override
    public int getItemCount() { return nameList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name,prices,num;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
           name = itemView.findViewById(R.id.cart_name);
           prices = itemView.findViewById(R.id.cart_price);
           num = itemView.findViewById(R.id.cart_num);

        }
    }
}

