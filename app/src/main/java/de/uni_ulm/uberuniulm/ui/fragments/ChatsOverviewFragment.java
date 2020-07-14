package de.uni_ulm.uberuniulm.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.uni_ulm.uberuniulm.ChatPage;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.User;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.Chat;
import de.uni_ulm.uberuniulm.ui.chat.ChatClickListener;
import de.uni_ulm.uberuniulm.ui.chat.UserAdapter;

public class ChatsOverviewFragment extends Fragment {
    private UserAdapter userAdapter;
    private List<Pair<String, String>> mUsers;
    private List<String> userlist;
    private String userId;

    private RecyclerView recyclerView;
    DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView= view.findViewById(R.id.chatFragmentRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SharedPreferences pref = new ObscuredSharedPreferences(
                getContext(), getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        userlist= new ArrayList<>();

        reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userlist.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chat chat= snapshot.getValue(Chat.class);

                    if(chat.getSender().equals(userId)){
                        userlist.add(chat.getReceiver());
                    }
                    if(chat.getReceiver().equals(userId)){
                        userlist.add(chat.getSender());
                    }
                }

                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    private void readChats(){
        mUsers= new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                ArrayList<String> keys= new ArrayList<>();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Pair user= new Pair(snapshot.getKey(), snapshot.child("username").getValue());

                    for(String id: userlist){
                        if(user.first.equals(id)){
                            if(mUsers.size() !=0){
                                Boolean notInList=true;
                                for(Pair<String, String> user1: mUsers){
                                    if(user.first.equals(user1.first)){
                                        notInList=false;
                                    }
                                }
                                if(notInList) {
                                    mUsers.add(user);
                                }
                            }else{
                                mUsers.add(user);
                            }
                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(), mUsers,new ChatClickListener() {
                    @Override
                    public void onChatClicked(int position) {
                        Intent intent= new Intent(getContext(), ChatPage.class);
                        intent.putExtra("SENDERID", userId);
                        intent.putExtra("RECEIVERNAME", mUsers.get(position).second);
                        intent.putExtra("RECEIVERID", mUsers.get(position).first);
                        startActivity(intent);
                    }
                });

                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
