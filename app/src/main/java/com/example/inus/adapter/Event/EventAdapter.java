package com.example.inus.adapter.Event;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.inus.R;
import com.example.inus.model.Event;
import java.text.SimpleDateFormat;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(@NonNull Context context, List<Event> events) {
        super(context, 0,events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Event event = getItem(position);
        SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_container_event,parent,false);
        }

        View view1 = convertView.findViewById(R.id.eventView);
        view1.setBackgroundResource(R.drawable.blue_border_rounded_cornwe);
        view1.setBackgroundColor(Color.TRANSPARENT);

        if( position%2 == 0 ){
            view1.setBackgroundColor(Color.parseColor("#30bfbf"));
        }else {
            view1.setBackgroundColor(Color.parseColor("#a7cdce"));
        }

        convertView.getVerticalScrollbarPosition();

        TextView eventTime = convertView.findViewById(R.id.eventCellTime);
        TextView eventTime2 = convertView.findViewById(R.id.eventCellTime2);
        TextView eventTitle = convertView.findViewById(R.id.eventCellTitle);
        TextView eventInfo = convertView.findViewById(R.id.eventCellInfo);

        String Stime = SDF.format(event.getStartTime());
        String Etime = SDF.format(event.getEndTime());
        String Etitle = event.getTitle();
        String EInfo =event.getLocation();

        eventTime.setText(Stime);
        eventTime2.setText(Etime);
        eventTitle.setText(Etitle);
        eventInfo.setText(EInfo);

        return convertView;
    }


}
