package de.uni_ulm.uberuniulm.ui.fragments;

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

import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.ride.RideLoader;
import de.uni_ulm.uberuniulm.ui.main.OfferListAdapter;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.ui.main.ClickListener;

public class MyOffersFragment extends Fragment {
    public View fragmentView;
    private RecyclerView myoffersRecyclerView;
    private static OfferListAdapter adapter;
    private ArrayList<Pair<ArrayList, OfferedRide>> offeredRides;
    private String userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_offers, container, false);

        myoffersRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewMyOffers);
        myoffersRecyclerView.setHasFixedSize(true);
        myoffersRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        offeredRides = new ArrayList();

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        RideLoader rideLoader= new RideLoader(getContext());
        rideLoader.getUsersOfferedRides(this);
        return fragmentView;
    }

    public void updateOffers(ArrayList<Pair<ArrayList, OfferedRide>> rides){
        offeredRides= rides;
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }else{
            setOfferAdapter();
        }
    }

    private void setOfferAdapter(){
        adapter = new OfferListAdapter(fragmentView.getContext(), offeredRides, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {

            }

            @Override
            public void onOfferClicked(int position){
                Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
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
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();
                    myRef.child(userId).child("offeredRides").child(String.valueOf(offeredRides.get(position).second.getzIndex())).removeValue();
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
                            Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
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
    }
}
