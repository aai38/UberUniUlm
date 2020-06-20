package de.uni_ulm.uberuniulm.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.uni_ulm.uberuniulm.MapActivity;
import de.uni_ulm.uberuniulm.ui.OfferListAdapter;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.ui.ClickListener;

public class MyBookedRidesFragment extends Fragment {
    public View fragmentView;
    private RecyclerView mybookingsRecyclerView;
    private static OfferListAdapter adapter;
    private DatabaseReference myRef;
    ArrayList<Pair<ArrayList, OfferedRide>> bookedRides;
    ArrayList<BookedRide> bookedRidesData = new ArrayList<>();
    private FirebaseAuth mAuth;
    private long zIndexBooking;
    private String userIdBooking;
    FirebaseDatabase database;
    SharedPreferences pref;
    String userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_bookings, container, false);

        mybookingsRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewMyBookings);
        mybookingsRecyclerView.setHasFixedSize(true);
        mybookingsRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        database = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();


        pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");


        bookedRides = new ArrayList();

        myRef = database.getReference();


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i("onDataChange2", dataSnapshot.getKey() + " " + String.valueOf(dataSnapshot.child(userId).child("obookedRides").getChildrenCount()));
                for (DataSnapshot ride : dataSnapshot.child(userId).child("obookedRides").getChildren()) {
                    ArrayList<Object> values = new ArrayList();
                    for (DataSnapshot rideValue : ride.getChildren()) {
                        values.add(rideValue.getValue());
                        Log.e("values", values.toString());
                    }

                    if (values.size() == 0) {
                        Log.i("onDataChange", "no values");
                    } else if (values.size() == 1) {
                        Log.i("anything wrong", "only one value");

                    } else {
                                                             //Route route = (Route) values.get(0);
                        userIdBooking = values.get(0).toString();
                        zIndexBooking = (long) values.get(1);
                        BookedRide bookedRide = new BookedRide(userIdBooking, (int) zIndexBooking);
                        bookedRidesData.add(bookedRide);
                        Log.e("bookedRidesData", bookedRidesData.toString());
                    }
                }

                if (bookedRidesData.size() == 0) {
                    Log.i("bookedRides", "nobookedRides");
                } else {
                    String username = (String) dataSnapshot.child(userId).child("username").getValue();
                    for (int i = 0; i < bookedRidesData.size(); i++) {
                        String index = ""+bookedRidesData.get(i).getzIndex();
                        DataSnapshot ride = dataSnapshot.child(bookedRidesData.get(i).getUserKey()).child("offeredRides").child(index);
                            ArrayList<Object> values = new ArrayList();
                            values.add(dataSnapshot.getValue());

                            for (DataSnapshot rideValue : ride.getChildren()) {
                                values.add(rideValue.getValue());
                                Log.e("values", values.toString());
                            }

                            if (values.size() == 0) {
                                Log.i("onDataChange", "no values");
                            } else {
                                                                     //Route route = (Route) values.get(0);
                                List<LatLng> coordinates=new ArrayList<>();
                                try {
                                    List<HashMap> coordinatesHash = (List<HashMap>) values.get(7);
                                    for(HashMap coordinate : coordinatesHash){
                                        LatLng coord=new LatLng((Double)coordinate.get("latitude"), (Double) coordinate.get("longitude"));
                                        coordinates.add(coord);
                                    }
                                }catch(ClassCastException e){
                                    Log.d("YEAH", "OBVIOUSLY");
                                    coordinates= new ArrayList<>();
                                }
                                Log.e("TAG", "values" + values.get(3).toString());
                                long price = (long) (values.get(6));
                                String date = values.get(1).toString();
                                String time;
                                String userkey;
                                long zIndex;
                                if (values.size() == 11) {
                                    time = values.get(8).toString();
                                    userkey = values.get(9).toString();
                                    zIndex = (long) values.get(10);
                                } else {
                                    time = values.get(7).toString();
                                    userkey = values.get(8).toString();
                                    zIndex = (long) values.get(9);
                                }
                                long places = (long) values.get(4);
                                long places_open = (long) values.get(5);
                                String departure = values.get(2).toString();
                                String destination = values.get(3).toString();

                                OfferedRide offeredRide = new OfferedRide(coordinates, (int) price, date, time, (int) places, (int) places_open, departure, destination, userkey, (int) zIndex);
                                ArrayList<Object> userData = new ArrayList();
                                userData.add(userId);
                                userData.add(username);
                                Float rating = -2.0f;
                                userData.add(rating);
                                bookedRides.add(new Pair(userData, offeredRide));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        adapter = new OfferListAdapter(fragmentView.getContext(), bookedRides, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {

            }

            @Override
            public void onOfferClicked(int position) {
                Intent intent = new Intent(fragmentView.getContext(), MapActivity.class);
                Pair clickedRidePair = bookedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                intent.putExtra("USER", (ArrayList) clickedRidePair.first);
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
                startActivity(intent);
            }

            @Override
            public void onEditClicked(int position){
                Intent intent = new Intent(fragmentView.getContext(), MapActivity.class);
                Pair clickedRidePair = bookedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                intent.putExtra("USER", (ArrayList) clickedRidePair.first);
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "EDITOFFER");
                startActivity(intent);
            }
        });
        mybookingsRecyclerView.setAdapter(adapter);

        return fragmentView;
    }
}
