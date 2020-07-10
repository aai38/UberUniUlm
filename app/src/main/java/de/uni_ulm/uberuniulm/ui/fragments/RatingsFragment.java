package de.uni_ulm.uberuniulm.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.Rating;
import de.uni_ulm.uberuniulm.model.ride.RideLoader;
import de.uni_ulm.uberuniulm.ui.main.RatingListAdapter;

public class RatingsFragment extends Fragment {

    public View fragmentView;
    private RatingListAdapter ratingListAdapter;
    private RecyclerView recyclerViewContainer;
    FirebaseDatabase database;
    private DatabaseReference myRef;
    private ArrayList totalRatings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_rating, container, false);

        recyclerViewContainer = (RecyclerView) fragmentView.findViewById(R.id.rating_recyclerView);
        recyclerViewContainer.setHasFixedSize(true);
        recyclerViewContainer.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));
        final ArrayList hold = new ArrayList();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        totalRatings = new ArrayList();

        RideLoader rideLoader = new RideLoader(getContext());
        rideLoader.getRatings(this);





        return fragmentView;
    }

    public void updateRatings(ArrayList ratings) {
        totalRatings = ratings;
        if(ratingListAdapter!=null){
            ratingListAdapter.notifyDataSetChanged();
            recyclerViewContainer.setAdapter(ratingListAdapter);
        }else{
            setRatingAdapter();
        }
    }

    private void setRatingAdapter() {
        ratingListAdapter = new RatingListAdapter(getContext(), totalRatings);
        recyclerViewContainer.setAdapter(ratingListAdapter);
    }
}