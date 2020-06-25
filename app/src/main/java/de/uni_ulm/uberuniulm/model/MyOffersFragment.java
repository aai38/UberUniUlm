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
                Log.i("onDataChange2", dataSnapshot.getKey()+" "+String.valueOf(dataSnapshot.child("offeredRides").getChildrenCount()));
                String username = (String) dataSnapshot.child(userId).child("username").getValue();
                    for (DataSnapshot ride : dataSnapshot.child("offeredRides").getChildren()) {
                        ArrayList<Object> values = new ArrayList();
                        values.add(dataSnapshot.getKey());
                        for (DataSnapshot rideValue : ride.getChildren()) {
                            values.add(rideValue.getValue());
                        }

                        if(values.size()==0) {
                            Log.i("onDataChange", "no values");
                        } else {
                            List<LatLng> coordinates=new ArrayList<>();
                            try {
                                List<HashMap> coordinatesHash = (List<HashMap>) values.get(7);
                                for(HashMap coordinate : coordinatesHash){
                                    LatLng coord=new LatLng((Double)coordinate.get("latitude"), (Double) coordinate.get("longitude"));
                                    coordinates.add(coord);
                                }
                            }catch(ClassCastException e){
                                coordinates= new ArrayList<>();
                            }
                            Log.i("TAG", "values" + values.get(3).toString());
                            long price = (long) (values.get(6));
                            String date =  values.get(1).toString();
                            String time;
                            String userkey;
                            long zIndex;
                            if(values.size()==11){
                                time= values.get(8).toString();
                                userkey = values.get(9).toString();
                                zIndex = (long) values.get(10);
                            }else{
                                time= values.get(7).toString();
                                userkey = values.get(8).toString();
                                zIndex = (long) values.get(9);
                            }
                            long places = (long) values.get(4);
                            long places_open = (long) values.get(5);
                            String departure = values.get(2).toString();
                            String destination = values.get(3).toString();
                            List<LatLng> waypoints=null;
                            ArrayList<String> observers= new ArrayList<>();

                            OfferedRide offeredRide = new OfferedRide(coordinates, (int) price, date, time, (int)places, (int)places_open, departure, destination, userkey, (int)zIndex, waypoints, observers);
                            ArrayList<Object> userData = new ArrayList();
                            userData.add(userId);
                            userData.add(username);
                            Float rating= -2.0f;
                            userData.add(rating);
                            offeredRides.add(new Pair(userData,offeredRide));
                            adapter.notifyDataSetChanged();
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
