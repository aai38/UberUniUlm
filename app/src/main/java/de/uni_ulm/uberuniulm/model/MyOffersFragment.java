package de.uni_ulm.uberuniulm.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uni_ulm.uberuniulm.MapActivity;
import de.uni_ulm.uberuniulm.ui.OfferListAdapter;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.ui.ClickListener;

public class MyOffersFragment extends Fragment {
    public View fragmentView;
    private RecyclerView myoffersRecyclerView;
    private static OfferListAdapter adapter;
    private DatabaseReference myRef;
    private ArrayList<Pair<ArrayList, OfferedRide>> offeredRides;
    private FirebaseAuth mAuth;
    private AutoCompleteTextView atvWaypointLocation;


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
                Log.i("onDataChange2", dataSnapshot.getKey() + " " + String.valueOf(dataSnapshot.child("offeredRides").getChildrenCount()));
                String username = (String) dataSnapshot.child(userId).child("username").getValue();
                for (DataSnapshot ride : dataSnapshot.child("offeredRides").getChildren()) {
                    HashMap<String, Object> values = new HashMap<>();
                    values.put("dataKey", dataSnapshot.getKey());
                    for (DataSnapshot rideValue : ride.getChildren()) {
                        values.put(rideValue.getKey(), rideValue.getValue());
                    }

                    if (values.size() == 0) {
                        Log.i("onDataChange", "no values");
                    } else {
                        List<LatLng> coordinates = new ArrayList<>();

                        if (values.size() == 0) {
                            Log.i("onDataChange", "no values");
                        } else {
                            try {
                                List<HashMap> coordinatesHash = (List<HashMap>) values.get("route");
                                for (HashMap coordinate : coordinatesHash) {
                                    LatLng coord = new LatLng((Double) coordinate.get("latitude"), (Double) coordinate.get("longitude"));
                                    coordinates.add(coord);
                                }
                            } catch (ClassCastException e) {
                                coordinates = new ArrayList<>();
                            }

                            List<LatLng> waypoints = new ArrayList<>();
                            try {
                                List<HashMap> waypointsHash = (List<HashMap>) values.get("waypoints");
                                if (waypointsHash != null) {
                                    for (HashMap waypoint : waypointsHash) {
                                        LatLng coord = new LatLng((Double) waypoint.get("latitude"), (Double) waypoint.get("longitude"));
                                        waypoints.add(coord);
                                    }
                                }
                            } catch (ClassCastException e) {
                                waypoints = new ArrayList<>();
                            }

                            String date = values.get("date").toString();
                            String time = values.get("time").toString();
                            String userkey = values.get("userId").toString();
                            long zIndex = (long) values.get("zIndex");
                            long price = (long) (values.get("price"));
                            long places = (long) values.get("places");
                            long places_open = (long) values.get("places_open");

                            List<String> observers = new ArrayList<>();
                            try {
                                List<String> observersHash = (List<String>) values.get("observers");
                                if (observersHash != null) {
                                    for (String observer : observersHash) {
                                        observers.add(observer);
                                    }
                                }
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                                observers = new ArrayList<>();
                            }


                            String departure = values.get("departure").toString();
                            String destination = values.get("destination").toString();

                            OfferedRide offeredRide = new OfferedRide(coordinates, (int) price, date, time, (int) places, (int) places_open, departure, destination, userkey, (int) zIndex, waypoints, observers);
                            ArrayList<Object> userData = new ArrayList();
                            userData.add(userId);
                            userData.add(username);
                            Float rating = -2.0f;
                            userData.add(rating);
                            offeredRides.add(new Pair(userData, offeredRide));
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        adapter = new OfferListAdapter(fragmentView.getContext(), offeredRides, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {

            }

            @Override
            public void onOfferClicked(int position){
                Intent intent = new Intent(fragmentView.getContext(), MapActivity.class);
                Pair clickedRidePair = offeredRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                intent.putExtra("USER", (ArrayList) clickedRidePair.first);
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
                startActivity(intent);
            }

            @Override
            public void onMarkClicked(View view, int position){
                Boolean notMarkedYet=offeredRides.get(position).second.getObservers().contains(userId);
                OfferedRide ride=offeredRides.get(position).second;
                if (!userId.equals(ride.getUserId())) {
                    TextView markBttn = (TextView) view;
                    if (notMarkedYet) {
                        ride.markRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_mark_offer);
                    } else {
                        ride.unmarkRide(userId);
                        markBttn.setBackgroundResource(R.drawable.ic_mark_offer_deselected);
                    }

                    myRef.child(offeredRides.get(position).first.get(0).toString()).child("offeredRides").child(String.valueOf(ride.getzIndex())).setValue(ride);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onEditClicked(int position){
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Edit your offer");
                dialog.setItems(getResources().getStringArray(R.array.editoptions),new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int pos) {
                        if(pos==0){
                            Intent intent = new Intent(fragmentView.getContext(), MapActivity.class);
                            Pair clickedRidePair = offeredRides.get(position);
                            OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                            intent.putExtra("USER", (ArrayList) clickedRidePair.first);
                            intent.putExtra("RIDE", clickedRide);
                            intent.putExtra("VIEWTYPE", "EDITOFFER");
                            startActivity(intent);
                        }else{
                            AlertDialog.Builder dialogWarning = new AlertDialog.Builder(getActivity());
                            dialogWarning.setTitle("Warning!");
                            Pair clickedRidePair = offeredRides.get(pos);
                            OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                            if(clickedRide.getPlaces()>clickedRide.getPlaces_open()) {
                                dialogWarning.setMessage("Do you really want to delete this ride? The people who booked your trip will be notified.");
                            }else{
                                dialogWarning.setMessage("Do you really want to delete this ride?");
                            }
                            dialogWarning.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogWarning, int which) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference();
                                    myRef.child(userId).child("offeredRides").child(String.valueOf(offeredRides.get(position).second.getzIndex())).removeValue();
                                    //TODO remove from booked rides and notify person
                                    offeredRides.remove(position);
                                    adapter.notifyDataSetChanged();

                                    dialogWarning.dismiss();
                                }
                            });

                            dialogWarning.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogWarning, int which) {
                                    dialogWarning.dismiss();
                                }
                            });

                            dialogWarning.show();
                        }
                    }

                });
                dialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = dialog.create();
                alert.show();

            }
        });
        myoffersRecyclerView.setAdapter(adapter);

        return fragmentView;
    }
}
