package com.example.inus.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FCMMessages {

    private Context context;

    // 傳給單一用戶
    public void sendMessageSingle(Context context, final String recipient ,final String title,
                                  final String body, final Map<String, String> dataMap)
    {
        this.context = context;

        // 要傳遞的內容
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("body",body);
        notificationMap.put("title",title);

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("notification",notificationMap);
        rootMap.put("to",recipient);
        if(dataMap != null)
            rootMap.put("data",dataMap);

        // 發送
        new SendFCM().setFcm(rootMap).execute();

    }

    // 傳給多個用戶
    public void sendMessageMulti(Context context , final JSONArray recipients,
                                 final String title, final String body, final Map<String, String> dataMap){
        this.context = context;

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("body", body);
        notificationMap.put("title",title);

        Map<String, Object> rootMap = new HashMap<>();
        rootMap.put("notification", notificationMap);
        rootMap.put("registration_ids",recipients);
        if(dataMap != null)
            rootMap.put("data",dataMap);

        new SendFCM().setFcm(rootMap).execute();
    }


    @SuppressLint("StaticFieldLeak")
    class SendFCM extends AsyncTask<String, String, String> {

        private String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
        private Map<String, Object> fcm;

        SendFCM setFcm(Map<String, Object> fcm){
            this.fcm = fcm;
            return this;
        }

        @Override
        protected String doInBackground(String... strings) {

            try{
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, new JSONObject(fcm).toString());

                // 建立Request，設置連線資訊
                Request request = new Request.Builder()
                        .url(FCM_MESSAGE_URL)
                        .post(body)
                        .addHeader("Authorization",
                                "key=AAAA2ure_2o:APA91bErAUBUBHTxhKQZA3X7DsxhcvAw-d1ppCJVApZ-Cs9uFh185ZFfF42VBdvjKvhgpGbSddKjSElD38wFH1CCN8fKSCyOpwd_O4jPJktydl3oBIHXXIDA9fvDDPTNQsZAULnn9jpt" )
                        .build();

                Response response = new OkHttpClient().newCall(request).execute();
                return response.body().string() ;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result){
            try{
                JSONObject resultJson = new JSONObject(result);
                int success, failure;
                success = resultJson.getInt("success");
                failure = resultJson.getInt("failure");
                Toast.makeText(context, "Sent: " + success + "/" +  failure, Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                Log.d("JsonError",e.getMessage());
//                Toast.makeText(context, "fail" , Toast.LENGTH_SHORT).show();
            }
        }

    }
}
