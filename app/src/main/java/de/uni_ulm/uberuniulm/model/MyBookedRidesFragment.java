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
import android.widget.TextView;

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
                    HashMap<String, Object> values = new HashMap<>();
                    for (DataSnapshot rideValue : ride.getChildren()) {
                        values.put(rideValue.getKey(),rideValue.getValue());
                    }

                    if (values.size() == 0) {
                        Log.i("onDataChange", "no values");
                    } else {
                        userIdBooking = values.get("userKey").toString();
                        zIndexBooking = (long) values.get("zIndex");
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
                        Log.d("BOOKED RIDES DATA", bookedRidesData.toString());
                        String index = ""+bookedRidesData.get(i).getzIndex();
                        DataSnapshot ride = dataSnapshot.child(bookedRidesData.get(i).getUserKey()).child("offeredRides").child(index);
                            HashMap<String, Object> values = new HashMap<>();
                            //values.put("datakey",dataSnapshot.getValue());

                            for (DataSnapshot rideValue : ride.getChildren()) {
                                values.put(ride.getKey(), rideValue.getValue());
                                Log.e("values", values.toString());
                            }

                            if (values.size() == 0) {
                                Log.i("onDataChange", "no values");
                            } else if (values.size() == 1) {
                                Log.d("BOOKED RIDES", values.toString());
                            }else{
                                List<LatLng> coordinates=new ArrayList<>();
                                try {
                                    List<HashMap> coordinatesHash = (List<HashMap>) values.get("route");
                                    if(coordinatesHash!=null) {
                                        for (HashMap coordinate : coordinatesHash) {
                                            LatLng coord = new LatLng((Double) coordinate.get("latitude"), (Double) coordinate.get("longitude"));
                                            coordinates.add(coord);
                                        }
                                    }
                                }catch(ClassCastException e){
                                    coordinates= new ArrayList<>();
                                }
                                long price = (long) (values.get("price"));
                                String date = values.get("date").toString();
                                String time = values.get("time").toString();
                                String userkey= values.get("userId").toString();
                                long zIndex= (long) values.get("zIndex");
                                long places = (long) values.get("places");
                                long places_open = (long) values.get("places_open");
                                String departure = values.get("departure").toString();
                                String destination = values.get("destination").toString();
                                List<LatLng> waypoints= null;
                                List<String> observers= new ArrayList<>();

                                try {
                                    List<String> observersHash = (List<String>) values.get("observers");
                                    if(observersHash!=null) {
                                        for (String observer : observersHash) {
                                            observers.add(observer);
                                        }
                                    }
                                }catch(ClassCastException e){
                                    e.printStackTrace();
                                    observers= new ArrayList<>();
                                }

                                try {
                                    List<HashMap> waypointsHash = (List<HashMap>) values.get("waypoints");
                                    if(waypointsHash!=null) {
                                        for (HashMap waypoint : waypointsHash) {
                                            LatLng coord = new LatLng((Double) waypoint.get("latitude"), (Double) waypoint.get("longitude"));
                                            waypoints.add(coord);
                                        }
                                    }
                                }catch(ClassCastException e){
                                    waypoints= new ArrayList<>();
                                }

                                OfferedRide offeredRide = new OfferedRide(coordinates, (int) price, date, time, (int) places, (int) places_open, departure, destination, userkey, (int) zIndex, waypoints, observers);
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
            public void onMarkClicked(View view, int position){
                Boolean notMarkedYet=bookedRides.get(position).second.getObservers().contains(userId);
                OfferedRide ride=bookedRides.get(position).second;
                if (!userId.equals(ride.getUserId())) {
                    TextView markBttn = (TextView) view;
                    if (!notMarkedYet) {
                        ride.markRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_mark_offer);
                    } else {
                        ride.unmarkRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_mark_offer_deselected);
                    }

                    myRef.child(bookedRides.get(position).first.get(0).toString()).child("offeredRides").child(String.valueOf(ride.getzIndex())).setValue(ride);
                    adapter.notifyDataSetChanged();
                }
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
