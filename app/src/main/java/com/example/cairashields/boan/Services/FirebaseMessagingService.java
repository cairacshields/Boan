package com.example.cairashields.boan.Services;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.cairashields.boan.Helper.MathHelper;
import com.example.cairashields.boan.R;
import com.example.cairashields.boan.ui.SwipeBorrowRequests;
import com.google.firebase.messaging.RemoteMessage;


import static android.app.Notification.VISIBILITY_PRIVATE;

/*
    Class for handling FirebaseMessaging and Notifications
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private String TAG = "MessageReceivedService";
    private String CHANNEL_ID = "BOAN_CHANNEL";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Create an explicit intent for an Activity in your app
        //Should change this to direct users to page to view new Terms Agreements
        Intent intent = new Intent(this, SwipeBorrowRequests.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            Builder mBuilder = new Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_borrow_requests_icon_1)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentIntent(pendingIntent)
                    .setPriority(remoteMessage.getPriority())
                    .setVisibility(VISIBILITY_PRIVATE)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            //Can use this in the future to dismiss/cancel or update notifications
            int notificationId = MathHelper.generateRandomInt(100000000);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(notificationId, mBuilder.build());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
