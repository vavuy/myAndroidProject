package com.example.inus.adapter.Event;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inus.databinding.ItemContainerFriendsBinding;
import com.example.inus.model.User;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

    private boolean isSelect = false;
    private final List<User> users;
    public static ArrayList<String> selectedUsers = new ArrayList<>();
    private PreferenceManager preferenceManager;

    public FriendsAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerFriendsBinding itemContainerFriendsBinding = ItemContainerFriendsBinding.inflate(
                LayoutInflater.from(parent.getContext())
                ,parent,false
        );
        preferenceManager = new PreferenceManager(parent.getContext());
        return new FriendsViewHolder(itemContainerFriendsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        holder.setData(users.get(position));
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        holder.itemView.setOnClickListener(view -> {

            if(!isSelect){
                holder.binding.imageViewSelected.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundColor(Color.parseColor("#f5f5dc"));
                isSelect = true;
                selectedUsers.add(users.get(position).id);  // 選取好友
            }else {
                holder.binding.imageViewSelected.setVisibility(View.GONE);
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                isSelect = false;
                selectedUsers.remove(users.get(position).id); // 取消選取
            }
            preferenceManager.putString(Constants.KEY_SELECTED_USERS,"" + selectedUsers);  // 取出所選的好友ID
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder{

        ItemContainerFriendsBinding binding;

        FriendsViewHolder(ItemContainerFriendsBinding itemContainerFriendsBinding){
            super(itemContainerFriendsBinding.getRoot());
            binding = itemContainerFriendsBinding;
        }

        void setData(User user){
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.textName.setText(user.name);
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.getDecoder().decode(encodedImage);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }


}
