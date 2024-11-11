package com.example.inus.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.codbking.calendar.CaledarAdapter;
import com.codbking.calendar.CalendarBean;
import com.codbking.calendar.CalendarUtil;
import com.codbking.calendar.CalendarView;
import com.example.inus.Activity.Setting.BaseActivity;
import com.example.inus.Activity.Setting.setting;
import com.example.inus.Activity.addEvent._addEvent;
import com.example.inus.Activity.addEvent._group;
import com.example.inus.R;
import com.example.inus.adapter.Event.EventAdapter;
import com.example.inus.databinding.ActivityHomeScreenBinding;
import com.example.inus.model.Event;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Home_screen extends BaseActivity {

    private ActivityHomeScreenBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PreferenceManager preferenceManager;
    private boolean isOpen =false;  // for float btn
    private Animation fabOpen , fabClose, rotateForward, rotateBackward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        initList(Constants.selectDay);  // 登入時顯示
        initView();  // DB from initList
        setListener();
        getToken();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.selectDay = ""+ LocalDate.now();
        initList(Constants.selectDay);
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());

        /* float button animation */
        binding.fabAddEvent.hide();
        binding.fabGroup.hide();
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);
    }
    // 建立上方日曆
    private void initView(){
        binding.calendarDateView.setAdapter(new CaledarAdapter() {
            @Override
            public View getView(View view, ViewGroup viewGroup, CalendarBean calendarBean) {

                if(view == null){
                    view = LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.item_xiaomi,null);
                }

                TextView text = (TextView) view.findViewById(R.id.text);
                text.setText("" + calendarBean.day);

                if(calendarBean.mothFlag != 0){
                    text.setTextColor(0xff9299a1);
                } else {
                    text.setTextColor(0xff444444);
                }
                //農曆
//                TextView chinaText = (TextView) view.findViewById(R.id.chinaText);
//                chinaText.setText(calendarBean.chinaDay);
                return view;
            }
        });

        binding.calendarDateView.setOnItemClickListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int i, CalendarBean calendarBean) {
                String moth,day;  // 補0
                if(calendarBean.moth < 10){ moth = "0" + calendarBean.moth;
                }else{ moth = "" + calendarBean.moth; }

                if(calendarBean.day < 10){  day = "0" + calendarBean.day;
                }else{ day = "" + calendarBean.day; }
                Constants.selectDay = calendarBean.year + "-" + moth +"-" + day;

                binding.title.setText(Constants.selectDay);
                initList(Constants.selectDay);  // 點擊時顯示
            }
        });

        // 顯示第一次title
        int[] data = CalendarUtil.getYMD(new Date());
        binding.title.setText(data[0] + "-" +data[1] +"-" + data[2]);
    }

    /*calendar event */
    private void initList(String selectDay){

        ArrayList<Event> Gevent = new ArrayList<>();

        // 揪團 事件
        db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.UID).collection("group").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Gevent.clear();
                        for(QueryDocumentSnapshot doc : task.getResult()){
                            Event event = new Event(doc.getString(Constants.KEY_EVENT_TITLE), doc.getDate(Constants.KEY_EVENT_START_TIME),
                                    doc.getDate(Constants.KEY_EVENT_END_TIME), doc.getString(Constants.KEY_EVENT_LOCATION),doc.getString(Constants.KEY_EVENT_DESCRIPTION));
                            Gevent.add(event);
                        }
                    }
                });

        // 個人 事件
        db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.UID).collection("event").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Event.eventList.clear();
                        Event.eventList.addAll(Gevent);
                        for(QueryDocumentSnapshot doc : task.getResult()){
                            Event event = new Event(doc.getString(Constants.KEY_EVENT_TITLE), doc.getDate(Constants.KEY_EVENT_START_TIME),
                                    doc.getDate(Constants.KEY_EVENT_END_TIME), doc.getString(Constants.KEY_EVENT_LOCATION),doc.getString(Constants.KEY_EVENT_DESCRIPTION));

                            Event.eventList.add(event);
                            Collections.sort(Event.eventList,(obj1,obj2) -> obj1.startTime.compareTo(obj2.startTime));

                            ArrayList<Event> dailyEvents = Event.eventForDate(selectDay);  // 選擇的日期 顯示事件
                            binding.list.setAdapter(new EventAdapter(getApplicationContext(),dailyEvents));
                        }
                    }
                });


    }

    private void setListener(){
        binding.fab.setOnClickListener(fabListener);
        binding.fabAddEvent.setOnClickListener(fabListener);
        binding.fabGroup.setOnClickListener(fabListener);
        binding.navigation.setSelectedItemId(R.id.home);//選到home按鈕改變顏色
        binding.navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:return true;

                    case R.id.shop:
                        startActivity(new Intent(getApplicationContext(),Shop_screen.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.cart:
                        startActivity(new Intent(getApplicationContext(),Cart_screen.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.talk:
                        startActivity(new Intent(getApplicationContext(),Talk_screen.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.none:
                        startActivity(new Intent(getApplicationContext(), Notification_screen.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        binding.rightIcon.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), setting.class));
            overridePendingTransition(0,0);
        });

    }
    /*float button event */
    View.OnClickListener fabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            /*btn animation */
            if(isOpen){
                binding.fab.setAnimation(rotateForward);
                binding.fabAddEvent.setAnimation(fabClose);
                binding.fabGroup.setAnimation(fabClose);
                binding.fabAddEvent.hide();
                binding.fabGroup.hide();
                binding.fabAddEvent.setClickable(false);
                binding.fabGroup.setClickable(false);
                isOpen=false;
            }else{
                binding.fab.setAnimation(rotateBackward);
                binding.fabAddEvent.setAnimation(fabOpen);
                binding.fabGroup.setAnimation(fabOpen);
                binding.fabAddEvent.show();
                binding.fabGroup.show();
                binding.fabAddEvent.setClickable(true);
                binding.fabGroup.setClickable(true);
                isOpen=true;
            }

            if(view == binding.fabAddEvent)
                startActivity(new Intent(Home_screen.this, _addEvent.class));
            if(view == binding.fabGroup)
                startActivity(new Intent(Home_screen.this, _group.class));
        }
    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN,token);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}