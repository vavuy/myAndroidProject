package com.example.inus.Activity.addEvent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.inus.Activity.Setting.BaseActivity;
import com.example.inus.databinding.ActivityGroupBinding;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;

public class _group extends BaseActivity {

    private ActivityGroupBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        //是否團購
        binding.switchGroupbuy.setOnClickListener(view -> {
            if(binding.switchGroupbuy.isChecked()){
                preferenceManager.putBoolean(Constants.KEY_ISGROUPGBUY,true);
            }
            else{
                preferenceManager.putBoolean(Constants.KEY_ISGROUPGBUY,false);
            }
        });

        binding.button.setOnClickListener(view -> finish());
        binding.button5.setOnClickListener(view -> {
            // 將輸入的資料寫進 sharedpreferences
            preferenceManager.putString(Constants.KEY_EVENT_TITLE,binding.edtext1.getText().toString());
            preferenceManager.putString(Constants.KEY_EVENT_LOCATION,binding.edtext2.getText().toString());
            preferenceManager.putString(Constants.KEY_EVENT_DESCRIPTION,binding.edtext2.getText().toString());
            if(isVaildInput()){
                startActivity(new Intent(this, _PickerFriends.class));
            }
        });
    }

    // 判斷輸入值是否符合規定
    private boolean isVaildInput() {
        if(binding.edtext1.getText().toString().isEmpty()){
            showToast("請輸入標題");
            return false;
        }else if(binding.edtext2.getText().toString().isEmpty()){
            showToast("請輸入地點");
            return false;
        }else { return true; }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}