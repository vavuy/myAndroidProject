package com.example.inus.Activity.addEvent;

import androidx.appcompat.app.AlertDialog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.inus.Activity.Home_screen;
import com.example.inus.Activity.Setting.BaseActivity;
import com.example.inus.databinding.ActivityAddEventBinding;
import com.example.inus.model.Event;
import com.example.inus.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class _addEvent extends BaseActivity {

    private ActivityAddEventBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Calendar calendar = Calendar.getInstance();
    private Date date, beforeDate;
    private boolean allday =false;
    private int Rmwhich = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityAddEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListener();

    }

    private void setListener(){
        binding.switch1.setOnClickListener(view -> {
            if(!allday){
                binding.textViewEt.setVisibility(View.GONE);
                allday= true;
            }else {
                binding.textViewEt.setVisibility(View.VISIBLE);
                allday =false;
            }
        });
        binding.textViewSt.setOnClickListener(view ->timepicker(view));
        binding.textViewEt.setOnClickListener(view ->timepicker(view));
        binding.button.setOnClickListener(view -> startActivity(new Intent(this, Home_screen.class)));  // cancel
        binding.button2.setOnClickListener(view -> {
            if(isValidInput()){
                addEvent();
                finish();
//                startActivity(new Intent(this,Home_screen.class));
            }
        });  // setEvent
        binding.textViewRm.setOnClickListener(v->{
            String[] strings={"一小時前","三小時前","一天前"};

            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(strings, Rmwhich, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();//結束對話框
                    Rmwhich = which;
                    binding.textViewRm.setText(strings[which]);
                }
            });
            builder.show();
        });  // 提醒
    }

    public void timepicker(View v) {
        int year = calendar.get(Calendar.YEAR);      //取得所選的日期年月日
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);

        new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int iyear, int imonth, int iday) {
                new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int ihourOfDay, int iminute) {

                        calendar = new GregorianCalendar(iyear,imonth,iday,ihourOfDay,iminute);
                        date = calendar.getTime();

                        if(v == binding.textViewSt){
                            binding.textViewSt.setText(Constants.SDFDateTime.format(date));
                            beforeDate = date;
                        }else{
                            if(date.before(beforeDate)){
                                binding.textViewEt.setText("");
                                showToast("結束時間不可早於開始時間");
                            }else {
                                binding.textViewEt.setText(Constants.SDFDateTime.format(date));
                            }
                        }
                    }
                }, hourOfDay, minute, true).show();
            }
        }, year, month, day).show();

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isValidInput() {
        if(binding.edTextTitle.getText().toString().isEmpty()){
            showToast("請輸入標題");
            return false;
        }else if(binding.textViewSt.getText().toString().isEmpty()){
            showToast("請輸入開始時間");
            return false;
        }else if(binding.textViewEt.getText().toString().isEmpty() && allday){
            showToast("請輸入結束時間");
            return false;
        }else if(binding.editText.getText().toString().isEmpty()){
            binding.editText.setText("");
            return true;
        }else if(binding.editText2.getText().toString().isEmpty()){
            binding.editText2.setText("");
            return true;
        }else {
            return true;
        }
    }

    private void addEvent() {
        Event event = new Event(binding.edTextTitle.getText().toString(),
                beforeDate,date,
                binding.editText.getText().toString(),
                binding.editText2.getText().toString()
                );
//        HashMap<String,Object> data = new HashMap<>();
//        data.put(Constants.KEY_EVENT_TITLE,binding.edTextTitle.getText().toString());
//        data.put(Constants.KEY_EVENT_START_TIME,beforeDate);
//        data.put(Constants.KEY_EVENT_END_TIME,date);
//        data.put(Constants.KEY_EVENT_LOCATION,binding.editText.getText().toString());
//        data.put(Constants.KEY_EVENT_DESCRIPTION, binding.editText2.getText().toString());

        db.collection(Constants.KEY_COLLECTION_USERS + "/" + Constants.UID + "/event" ).document().set(event);
    }
}