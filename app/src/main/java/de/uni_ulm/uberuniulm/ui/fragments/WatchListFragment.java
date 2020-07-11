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
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.ride.RideLoader;
import de.uni_ulm.uberuniulm.ui.main.ClickListener;
import de.uni_ulm.uberuniulm.ui.main.OfferListAdapter;
import kotlin.Triple;

public class WatchListFragment extends Fragment {
    public View fragmentView;
    private RecyclerView watchListRecyclerView;
    private static OfferListAdapter adapter;
    private ArrayList<Triple<ArrayList, OfferedRide, Float>> observedRides;
    private String userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_bookings, container, false);

        watchListRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewMyBookings);
        watchListRecyclerView.setHasFixedSize(true);
        watchListRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        observedRides = new ArrayList();

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        RideLoader rideLoader= new RideLoader(getContext());
        rideLoader.getWatchedRides(this);
        return fragmentView;
    }

    public void updateOffers(ArrayList<Triple<ArrayList, OfferedRide, Float>> rides){
        observedRides = rides;
        if(adapter!=null){
            adapter.notifyDataSetChanged();
            watchListRecyclerView.setAdapter(adapter);
        }else{
            setOfferAdapter();
        }
    }

    private void setOfferAdapter(){
        adapter = new OfferListAdapter(fragmentView.getContext(), observedRides, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {

            }

            @Override
            public void onOfferClicked(int position){
                Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
                Triple clickedRidePair = observedRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.getSecond();
                intent.putExtra("USER", (ArrayList) clickedRidePair.getFirst());
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("RATING", (float)clickedRidePair.getThird());
                intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
                startActivity(intent);
            }

            @Override
            public void onEditClicked(int position) {

            }

            @Override
            public void onMarkClicked(View view, int position){
                Boolean notMarkedYet= observedRides.get(position).getSecond().getObservers().contains(userId);
                OfferedRide ride= observedRides.get(position).getSecond();
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
                    myRef.child(userId).child("offeredRides").child(String.valueOf(observedRides.get(position).getSecond().getzIndex())).removeValue();
                    myRef.child(observedRides.get(position).getFirst().get(0).toString()).child("offeredRides").child(String.valueOf(ride.getzIndex())).setValue(ride);
                    adapter.notifyDataSetChanged();
                }
            }

        });
        watchListRecyclerView.setAdapter(adapter);
    }
}
