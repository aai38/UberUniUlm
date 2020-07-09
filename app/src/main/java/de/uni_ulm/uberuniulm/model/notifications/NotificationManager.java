package de.uni_ulm.uberuniulm.model.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import de.uni_ulm.uberuniulm.MainPage;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.StartPage;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class NotificationManager extends FirebaseMessagingService {
    private Context context;
    private static final String TAG = "FirebaseMessagingService";
    private String userId;
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    public NotificationManager(Context context){
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
                        myRef = database.getReference();
                        SharedPreferences pref = new ObscuredSharedPreferences(context, context.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
                        userId = pref.getString("UserKey", "");
                        myRef.child(userId).child("token").setValue(token);
                        Log.d("NOTIFICATION SETUP", msg);
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        if(remoteMessage.getData().size()>0){
            Log.d(TAG, "Message data payload: " +remoteMessage.getData());
            try{
                JSONObject data= new JSONObject(remoteMessage.getData());
                String jsonMessage= data.getString("extra_information");
                Log.d(TAG, "onMessageReceived: \n"+ "Extra information: "+ jsonMessage);
            }catch (JSONException e ){
                e.printStackTrace();
            }
        }

        if(remoteMessage.getNotification()!=null){
            String title= remoteMessage.getNotification().getTitle();
            String message= remoteMessage.getNotification().getBody();
            String click_action= remoteMessage.getNotification().getClickAction();

            Log.d(TAG, "Message Notification Title: "+ title);
            Log.d(TAG, "Message Notification Body: "+ message);
            Log.d(TAG, "Message Notification click_action: "+ click_action);

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

    private void sendNotification(String title, String messageBody, String click_action){
        Intent intent;

        if(click_action.equals("SOMEACTIVITY")){
            intent= new Intent(context, StartPage.class);
        }else if(click_action.equals("MAINACTIVITY")){
            intent= new Intent( context, MainPage.class);
        }else{
            intent= new Intent(context, MainPage.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0 /*Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_app_logo_market)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        android.app.NotificationManager notificationManager=
                (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /*ID of notification*/, notificationBuilder.build());
    }
}
