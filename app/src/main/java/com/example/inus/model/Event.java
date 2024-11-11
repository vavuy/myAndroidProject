package com.example.inus.model;

import com.example.inus.util.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class Event {

    public String title;
    public Date startTime;
    public Date endTime;
    public String location;
    public String description;

    public static ArrayList<Event> eventList = new ArrayList<>();

    public static ArrayList<Event> eventForDate(String date){  // 收 date, return event of date
        ArrayList<Event> events = new ArrayList<>();

        for(Event event : eventList){  // EventDay : selectDay
            if(event.getDate().equals(date)){
                events.add(event);
            }
        }
        return events;
    }

    public Event(String title, Date startTime, Date endTime, String location, String description) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.description = description;
    }

    public Event(String title, Date startTime, Date endTime) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public String getTitle() {
        return title;
    }

    public Date getStartTime() { return startTime; }

    public Date getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public Long getLongtime(){
        long i = startTime.getTime();
        return i;
    }

    public String getDate() {
        String date1 = Constants.SDFDay.format(startTime);
        return date1;
    }
}
