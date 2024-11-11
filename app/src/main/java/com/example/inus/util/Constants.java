package com.example.inus.util;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PERFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSingnedIn";
    public static final String KEY_USER_ID = "usedId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "femToken";
    public static final String KEY_USER = "user";
    public static final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    //Event
    public static final String KEY_EVENT_TITLE = "title";
    public static final String KEY_EVENT_START_TIME = "startTime";
    public static final String KEY_EVENT_END_TIME = "endTime";
    public static final String KEY_EVENT_LOCATION = "location";
    public static final String KEY_EVENT_DESCRIPTION = "description";
    public static final String KEY_EVENT_START_DAY = "startDay";
    public static final String KEY_EVENT_END_DAY = "endDay";
    public static final String KEY_SELECTED_USERS = "selectedUsers";
    public static final String KEY_COLLECTION_START_EVENT = "SResult";
    public static final String KEY_COLLECTION_END_EVENT = "EResult";
    public static final String KEY_COLLECTION_START_TINE = "SResultTime";
    public static final String KEY_COLLECTION_END_TIME = "EResultTime";
    public static final String KEY_EVENT_STATE = "state";
    public static final String KEY_ISGROUPGBUY = "isGroupBuy";
    public static final SimpleDateFormat SDFDateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm");   //設定日期格式
    public static final SimpleDateFormat SDFDay = new SimpleDateFormat("yyyy-MM-dd");

    //To show the event of selected day
    public static final LocalDate today = LocalDate.now();
    public static String selectDay = "" + today;
    //Chat
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    //Conversation
    public static final String KEY_COLLECTION_CONVERSATIONS ="conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    //online highlight
    public static final String KEY_AVAILABILITY = "availability";
    //Notifications
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String RENOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    //FCM Notifications
    public static HashMap<String, String> remotoMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders(){
        if(remotoMsgHeaders == null){
            remotoMsgHeaders = new HashMap<>();
            remotoMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAA2ure_2o:APA91bErAUBUBHTxhKQZA3X7DsxhcvAw-d1ppCJVApZ-Cs9uFh185ZFfF42VBdvjKvhgpGbSddKjSElD38wFH1CCN8fKSCyOpwd_O4jPJktydl3oBIHXXIDA9fvDDPTNQsZAULnn9jpt"
            );
            remotoMsgHeaders.put(
                    RENOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remotoMsgHeaders;
    }


}
