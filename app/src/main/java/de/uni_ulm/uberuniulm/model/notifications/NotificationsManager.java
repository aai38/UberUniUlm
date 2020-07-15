package de.uni_ulm.uberuniulm.model.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.uni_ulm.uberuniulm.ChatPage;
import de.uni_ulm.uberuniulm.MainPage;
import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.ride.RideLoader;
import kotlin.Triple;

public class NotificationsManager extends FirebaseMessagingService {
    private Context context;
    private static final String TAG = "FirebaseMessagingService";
    private String userId, title, body;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private Map data;

    public NotificationsManager(){

    }

    public void setUp(Context context){
        this.context= context;
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("NOTIFICATION SETUP", "getInstanceId failed", task.getException());
                            return;
                        }

                        String token = task.getResult().getToken();

                        String msg = context.getString(R.string.notification_token_msg, token);
                        database = FirebaseDatabase.getInstance();
                        myRef = database.getReference().child("Users");
                        SharedPreferences pref = new ObscuredSharedPreferences(context, context.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
                        userId = pref.getString("UserKey", "");
                        myRef.child(userId).child("token").setValue(token);
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        if(remoteMessage.getData().size()>0){
            JSONObject data= new JSONObject(remoteMessage.getData());
            Log.d(TAG, "Message data payload: " +data );
        }

        if(remoteMessage.getData()!=null&& remoteMessage.getData().size()>0){
            String title= remoteMessage.getData().get("title");
            String message= remoteMessage.getData().get("body");
            String click_action= remoteMessage.getData().get("click_action");
            data= remoteMessage.getData();

            Log.d(TAG, "Message Notification Title: "+ title);
            Log.d(TAG, "Message Notification Body: "+ message);
            Log.d(TAG, "Message Notification click_action: "+ click_action);
            Log.d(TAG, "Message Notification data"+ data);

            sendNotification(title, message, click_action);
        }
    }

    @Override
    public void onNewToken(String token) {
        myRef.child(userId).child("token").setValue(token);
    }

    @Override
    public void onDeletedMessages(){

    }

    private void sendNotification(String title, String messageBody, String click_action) {
        Intent intent;
        if(click_action.equals("OPENCHAT")){
            intent = new Intent(getApplicationContext(), ChatPage.class);
            intent.putExtra("SENDERID", userId);
            String senderName=(String) data.get("senderName");
            intent.putExtra("RECEIVERNAME", senderName);
            String senderId=(String) data.get("senderId");
            intent.putExtra("RECEIVERID", senderId);
            sendNotification(title, messageBody, intent);
        }else if(click_action.equals("OPENRIDEOVERVIEW")){
            RideLoader rideLoader= new RideLoader(getApplicationContext());
            rideLoader.getSpecificRide(Objects.requireNonNull(data.get("userId")).toString(), Objects.requireNonNull(data.get("rideId")).toString(), this);
            this.title=title;
            this.body= messageBody;
        }else{
            intent = new Intent(getApplicationContext(), MainPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            sendNotification(title, messageBody, intent);
        }
    }

    public void setRideNotification(Triple<ArrayList, OfferedRide, Float> ride){
        Intent intent = new Intent(getApplicationContext(), MapPage.class);
        OfferedRide clickedRide = (OfferedRide) ride.getSecond();
        intent.putExtra("RATING", (float) ride.getThird());
        intent.putExtra("USER", (ArrayList) ride.getFirst());
        intent.putExtra("RIDE", clickedRide);
        intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        sendNotification(title, body, intent);
    }

    private void sendNotification(String title, String messageBody, Intent intent){
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_app_logo_market)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "NotificationsUberUniUlm",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public void subscribeToTopic(String topicname){
        FirebaseMessaging.getInstance().subscribeToTopic(topicname)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed successfully";
                        if (!task.isSuccessful()) {
                            msg = "Could not subscribe to topic";
                        }
                        Log.d(TAG, msg);
                    }
                });
    }

    public void unsubscribeTopic(String topicname) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicname)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed successfully";
                        if (!task.isSuccessful()) {
                            msg = "Could not subscribe to topic";
                        }
                        Log.d(TAG, msg);
                    }
                });
    }
}
