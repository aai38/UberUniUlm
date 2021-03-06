package de.uni_ulm.uberuniulm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uni_ulm.uberuniulm.model.User;
import de.uni_ulm.uberuniulm.model.ride.Chat;
import de.uni_ulm.uberuniulm.ui.chat.MessageAdapter;

import static java.security.AccessController.getContext;

public class ChatPage extends AppCompatActivity {
    com.mikhaellopez.circularimageview.CircularImageView profileImage;
    TextView usernameText;
    ImageButton sendButton;
    EditText inputField;

    DatabaseReference dataRef;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    Intent intent;

    ValueEventListener seenListener;

    String senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.content_chat_header);
        Toolbar toolbar=(Toolbar) findViewById(R.id.chatToolbar);

        recyclerView = findViewById(R.id.chatPageRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        ConstraintLayout zoomFragment= findViewById(R.id.chatPageZoomFragment);
        zoomFragment.setVisibility(View.INVISIBLE);

        inputField= (EditText) findViewById(R.id.chatPageTextInputField);
        sendButton= (ImageButton) findViewById(R.id.chatPageSendButton);

        profileImage= toolbar.findViewById(R.id.chatPageUserImage);
        usernameText= toolbar.findViewById(R.id.chatPageUsernameTextField);

        intent= getIntent();
        senderId= getIntent().getStringExtra("SENDERID");
        String userId= getIntent().getStringExtra("RECEIVERID");
        String userName= getIntent().getStringExtra("RECEIVERNAME");
        usernameText.setText(userName);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConstraintLayout zoomFragment= findViewById(R.id.chatPageZoomFragment);
                ImageView profilePhoto= findViewById(R.id.chatPageZoomProfilePhoto);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference profileImageRef = storageRef.child("profile_images/"+userId+".jpg");
                Glide.with(getApplicationContext())
                        .load(profileImageRef)
                        .centerCrop()
                        .skipMemoryCache(true) //2
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.start_register_profile_photo)
                        .transform(new CircleCrop())
                        .into(profilePhoto);
                zoomFragment.setVisibility(View.VISIBLE);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String message= inputField.getText().toString();
                if(!message.equals("")){
                    sendMessage(senderId, userId, message);
                }else{
                    Toast.makeText(ChatPage.this, "You can't send empty message.", Toast.LENGTH_SHORT).show();
                }
                inputField.setText("");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        dataRef= FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username= (String) dataSnapshot.child("username").getValue();
                usernameText.setText(username);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference profileImageRef = storageRef.child("profile_images/"+userId+".jpg");
                Glide.with(getApplicationContext())
                        .load(profileImageRef)
                        .centerCrop()
                        .skipMemoryCache(true) //2
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.start_register_profile_photo)
                        .transform(new CircleCrop())
                        .into(profileImage);

                readMessage(senderId, userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenChat(userId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    private void seenChat(String userid){
        dataRef= FirebaseDatabase.getInstance().getReference("Chats");
        seenListener= dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(senderId)&& chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap= new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Chats");

        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.push().setValue(hashMap);
    }

    public void onChatPageZoomView(View view){
        ConstraintLayout zoomFragment= findViewById(R.id.chatPageZoomFragment);
        zoomFragment.setVisibility(View.INVISIBLE);
    }

    private void readMessage(String myid, String userid){
        mChat= new ArrayList<>();
        dataRef= FirebaseDatabase.getInstance().getReference("Chats");

        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat= snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid)|| chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }

                    messageAdapter= new MessageAdapter(ChatPage.this, mChat);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        dataRef.removeEventListener(seenListener);
    }
}
