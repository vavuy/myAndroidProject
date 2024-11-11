package com.example.inus.adapter.Event;

import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inus.databinding.ItemContainerTimepickerBinding;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Timeadapter extends  RecyclerView.Adapter<TimeVH>{

    private PreferenceManager preferenceManager;
    private List<String> items;
    private Calendar calendar = Calendar.getInstance();
    private String st,et;
    private SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");

    public Timeadapter(List<String> items) {
        this.items = items;
    }
    @NonNull
    @Override
    public TimeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemContainerTimepickerBinding timepickerBinding = ItemContainerTimepickerBinding.inflate(
                LayoutInflater.from(parent.getContext())
                ,parent,false
        );
        preferenceManager = new PreferenceManager(parent.getContext());

        return new TimeVH(timepickerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeVH holder, int position) {
        holder.binding.textViewtime1.setText(items.get(position));

        holder.binding.textViewtime1.setOnClickListener(view -> {  // 點擊之後
            // 設定時間資料
            String[] day = preferenceManager.getString(Constants.KEY_EVENT_START_DAY).split("-");
            int year = Integer.parseInt(day[0]);
            int month = Integer.parseInt(day[1]);
            int sday = Integer.parseInt(day[2]);
            int hourOfDay = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);

            //先執行的視窗後顯示
            new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int ihourOfDay, int iMinute) {
                    calendar = new GregorianCalendar(year,month,sday,ihourOfDay,iMinute);
                    et = SDF.format(calendar.getTime());
                    preferenceManager.putString(Constants.KEY_EVENT_END_TIME , et);  // 本次事件結束時間
                    if( st.isEmpty() || st.compareTo(et) > 0){
                        Toast.makeText(timePicker.getContext(), "請重新輸入", Toast.LENGTH_SHORT).show();
                        holder.binding.textViewtime1.setText("請輸入時間");
                    }else {
                        holder.binding.textViewtime1.setText(st + " ~ " + et);
                    }
                }
            }, hourOfDay, minute,true).show();

            new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int ihourOfDay, int iMinute) {
                    calendar = new GregorianCalendar(year,month,sday,ihourOfDay,iMinute);
                    st = SDF.format(calendar.getTime());
                    preferenceManager.putString(Constants.KEY_EVENT_START_TIME , st);  // 本次事件開始時間
                }
            }, hourOfDay, minute,true).show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}

class TimeVH extends RecyclerView.ViewHolder{

    ItemContainerTimepickerBinding binding;

    TimeVH(ItemContainerTimepickerBinding itemContainerTimepickerBinding){
        super(itemContainerTimepickerBinding.getRoot());
        binding = itemContainerTimepickerBinding;
    }

}