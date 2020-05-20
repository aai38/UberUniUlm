package de.uni_ulm.uberuniulm.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.map.Route;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.OfferListAdapter;
import de.uni_ulm.uberuniulm.R;

public class MyOffersFragment extends Fragment {
    public View fragmentView;
    private RecyclerView myoffersRecyclerView;
    private static OfferListAdapter adapter;
    private DatabaseReference myRef;
    private ArrayList<Pair<String, OfferedRide>> offeredRides;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_offers, container, false);
        fragmentView.findViewById(R.id.startActivityRegisterProfileImage);

        myoffersRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewMyOffers);
        myoffersRecyclerView.setHasFixedSize(true);
        myoffersRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        offeredRides = new ArrayList();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser= mAuth.getCurrentUser();


        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        String userId = pref.getString("UserKey", "");
        myRef = database.getReference().child(userId);
        Log.i("onDataChange1", "no values");


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("onDataChange2", dataSnapshot.getKey()+" "+String.valueOf(dataSnapshot.child("offeredRides").getChildrenCount()));
                    for (DataSnapshot ride : dataSnapshot.child("offeredRides").getChildren()) {
                        ArrayList<Object> values = new ArrayList();
                        values.add(dataSnapshot.getKey());
                        for (DataSnapshot rideValue : ride.getChildren()) {
                            values.add(rideValue.getValue());
                        }

                        if(values.size()==0) {
                            Log.i("onDataChange", "no values");
                        } else {
                            //Route route = (Route) values.get(0);
                            Route route = null;
                            Log.i("TAG", "values" + values.get(3).toString());
                            long price = (long) (values.get(6));
                            String date =  values.get(1).toString();
                            String time = values.get(7).toString();
                            long places = (long) values.get(4);
                            long places_open = (long) values.get(5);
                            String departure = values.get(2).toString();
                            String destination = values.get(3).toString();

                            OfferedRide offeredRide = new OfferedRide(route, (int) price, date, time, (int)places, (int)places_open, departure, destination);
                            offeredRides.add(new Pair<>(currentUser.getUid(),offeredRide));
                            adapter.notifyDataSetChanged();
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        adapter = new OfferListAdapter(fragmentView.getContext(), offeredRides);
        myoffersRecyclerView.setAdapter(adapter);

        return fragmentView;
    }
}
