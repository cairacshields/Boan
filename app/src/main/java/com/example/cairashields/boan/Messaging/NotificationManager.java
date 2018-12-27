package com.example.cairashields.boan.Messaging;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NotificationManager {

    private static String SERVER_KEY = "key=AAAA7az5Gh4:APA91bHz15lZOPDK6B5kBMuBvphhf8XeMfahpJsViM5KAOg73o9-IOb6_zRYf_muH3iQKfjzhW6bBqvvZs-AiH_GXMNZ0_M6jyoIMpTcGhqDUbx93LLAq18a6ZvUJT1qRLu7cz2-64R9";

    public static Observable<Response> sendNotificationToUser(String title, String notificationBody, String toUserId) {

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final OkHttpClient client = new OkHttpClient();

        try {
            JSONObject json = new JSONObject();
            JSONObject dataJson = new JSONObject();
            dataJson.put("text", notificationBody);
            dataJson.put("title", title);
            dataJson.put("priority", "high");
            json.put("notification", dataJson);
            json.put("to", toUserId);

            RequestBody body = RequestBody.create(JSON, json.toString());
            final Request request = new Request.Builder()
                    .header("Authorization", SERVER_KEY)
                    .url("https://fcm.googleapis.com/fcm/send")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

           // Response response = client.newCall(request).execute();
           // String finalResponse = response.body().string();

            return io.reactivex.Observable.create(
                    new ObservableOnSubscribe<Response>() {
                        @Override
                        public void subscribe(ObservableEmitter<Response> emitter) {
                            try {
                                Response response = client.newCall(request).execute();
                                emitter.onNext(response);
                                emitter.onComplete();
                            } catch(Exception e) {
                                e.printStackTrace();
                                emitter.onError(e); // In case there are network errors
                            }
                        }
                    });

        } catch (JSONException e) {
            Log.d("NotificationManager ", "ERROR");
            e.printStackTrace();
            return null;
        }
    }
}
