package de.uni_ulm.uberuniulm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLng;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import de.uni_ulm.uberuniulm.model.ParkingSpots;
import de.uni_ulm.uberuniulm.ui.ClickListener;
import de.uni_ulm.uberuniulm.ui.OfferListAdapter;


public class MainPageFragment extends Fragment{

    public View fragmentView;
    ArrayList<Pair<ArrayList,OfferedRide>> offeredRides;
    RecyclerView offerRecyclerView;
    private OfferListAdapter adapter;
    private DatabaseReference myRef;
    private OfferedRide offeredRide;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_main_page, container, false);
        LinearLayout mapFragment = fragmentView.findViewById(R.id.mainPageFragmentContainer);
        mapFragment.setVisibility(View.VISIBLE);
        SearchView departure = fragmentView.findViewById(R.id.searchViewDeparture);
        SearchView destination = fragmentView.findViewById(R.id.searchViewDestination);
        ImageButton addFilterBttn= fragmentView.findViewById(R.id.addFilterBttn);

        addFilterBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout filterDialog=(FrameLayout) fragmentView.findViewById(R.id.addFilterDialog);
                filterDialog.setVisibility(View.VISIBLE);
                setUpFilterDialog();
            }
        });

        ImageButton filterConfirmBttn= fragmentView.findViewById(R.id.addFilterConfirmBttn);
        filterConfirmBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner filterTypeSpinner = fragmentView.findViewById(R.id.addFilterTypeSpinner);
                int filterType = filterTypeSpinner.getSelectedItemPosition();
                String[] filters = getResources().getStringArray(R.array.filters);
                LinearLayout filterContainer = (LinearLayout) fragmentView.findViewById(R.id.filterOverview);
                LinearLayout filterItem = (LinearLayout) inflater.inflate(R.layout.filter_overview_item, null, false);
                TextView filterItemText = (TextView) filterItem.findViewById(R.id.filterItemText);

                if (!filters[filterType].equals("Offeror")) {
                    Spinner filterContentSpinner = fragmentView.findViewById(R.id.addFilterContentSpinner);
                    int filterContent = filterContentSpinner.getSelectedItemPosition();
                    adapter.setFilter(filterType, filterContent);
                    String filterName=filters[filterType];
                    filterItemText.setText(filters[filterType] + ": " + fragmentView.getResources().getStringArray(getResId(filterName, R.array.class))[0]);
                } else {
                    EditText usernameTextField = fragmentView.findViewById(R.id.addFilterTextInput);
                    String offerorName = usernameTextField.getText().toString();
                    adapter.setUsernameFilter(offerorName);
                    filterItemText.setText(filters[filterType] + ": " + offerorName);
                }

                ImageButton filterItemCloseButton= (ImageButton) filterItem.findViewById(R.id.filterItemCloseBttn);

                filterItem.setContentDescription(String.valueOf(filterType));
                filterItemCloseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int filterType= Integer.parseInt(filterItem.getContentDescription().toString());
                        filterContainer.removeView((ViewGroup)view.getParent());
                        adapter.deleteFilter(filterType);
                    }
                });

                FrameLayout filterDialog=(FrameLayout) fragmentView.findViewById(R.id.addFilterDialog);
                filterDialog.setVisibility(View.INVISIBLE);

                filterContainer.addView(filterItem);
            }
        });

        ImageButton filterCancelBttn= fragmentView.findViewById(R.id.addFilterCancelBttn);
        filterCancelBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout filterDialog=(FrameLayout) fragmentView.findViewById(R.id.addFilterDialog);
                filterDialog.setVisibility(View.INVISIBLE);
            }
        });

        departure.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.filterDeparture(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.filterDeparture(s);
                return true;
            }
        });

        destination.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.filterDestination(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.filterDestination(s);
                return true;
            }
        });

        offerRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.mainPageOfferRecyclerView);
        offerRecyclerView.setHasFixedSize(true);
        offerRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        offeredRides = new ArrayList();

        ParkingSpots parkingSpots = new ParkingSpots();

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        int ridesTotal = pref.getInt("RideId", 0);

        myRef = database.getReference();


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user: dataSnapshot.getChildren()) {
                    DataSnapshot usersOfferedRides= user.child("offeredRides");
                    Long rating= (Long) user.child("rating").getValue();
                    ArrayList userData = new ArrayList();
                    userData.add(user.getKey());
                    userData.add(user.child("username").getValue());
                    for (DataSnapshot ride : usersOfferedRides.getChildren()) {
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
                                time= values.get(8).toString();
                                userkey = values.get(9).toString();
                                zIndex = (long) values.get(10);
                            }
                            long places = (long) values.get(4);
                            long places_open = (long) values.get(5);
                            String departure = values.get(2).toString();
                            String destination = values.get(3).toString();


                            offeredRide = new OfferedRide(coordinates, (int) price, date, time, (int)places, (int)places_open, departure, destination, userkey, (int) zIndex);
                            userData.add((float) rating);
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
                AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                alert.setMessage("You want to book this ride?");
                alert.setTitle("Book Ride");


                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Pair clickedRidePair = offeredRides.get(position);
                        OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference();
                        BookedRide bookedRide = new BookedRide(clickedRide.getUserId(), clickedRide.getzIndex());
                        final SharedPreferences pref = new ObscuredSharedPreferences(
                                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("BookedRideId", Context.MODE_PRIVATE));
                        int zIndex = pref.getInt("BookedRideId", 0);

                        //hier eigentlich Benachrichtigung an Fahrer
                        myRef.child(userId).child("obookedRides").child(String.valueOf(zIndex)).setValue(bookedRide);

                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("BookedRideId", zIndex +1);
                        editor.apply();

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

                alert.show();
            }

            @Override
            public void onOfferClicked(int position) {
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
                Intent intent = new Intent(fragmentView.getContext(), MapActivity.class);
                Pair clickedRidePair = offeredRides.get(position);
                OfferedRide clickedRide = (OfferedRide) clickedRidePair.second;
                intent.putExtra("USER", (ArrayList) clickedRidePair.first);
                intent.putExtra("RIDE", clickedRide);
                intent.putExtra("VIEWTYPE", "EDITOFFER");
                startActivity(intent);
            }
        });


        Log.d("OFFEREDRIDES", String.valueOf(offeredRides.size()));
        offerRecyclerView.setAdapter(adapter);

        return fragmentView;
    }

    private void setUpFilterDialog() {
        Spinner filterTypeSpinner= (Spinner) fragmentView.findViewById(R.id.addFilterTypeSpinner);
        ArrayAdapter adapterType= new ArrayAdapter<String>(getActivity(),
               R.layout.filter_spinner_item, getResources().getStringArray(R.array.filters));
        filterTypeSpinner.setAdapter(adapterType);

        Spinner filterContentSpinner= (Spinner) fragmentView.findViewById(R.id.addFilterContentSpinner);
        ArrayAdapter adapterContent= new ArrayAdapter<String>(getActivity(),
                R.layout.filter_spinner_item, getResources().getStringArray(R.array.Distance));
        filterContentSpinner.setAdapter(adapterContent);

        filterTypeSpinner.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        filterTypeSpinner.setSelection(0);

        filterContentSpinner.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        filterContentSpinner.setSelection(0);
        filterContentSpinner.setVisibility(View.VISIBLE);

        EditText userNameTextField=(EditText) fragmentView.findViewById(R.id.addFilterTextInput);
        userNameTextField.setVisibility(View.GONE);

        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ArrayAdapter adapter;
                switch(position){
                    case 0:
                        filterContentSpinner.setVisibility(View.VISIBLE);
                        userNameTextField.setVisibility(View.GONE);
                        adapter = new ArrayAdapter<String>(getActivity(),
                                R.layout.filter_spinner_item, getResources().getStringArray(R.array.Distance));
                        filterContentSpinner.setAdapter(adapter);
                        break;
                    case 1:
                        userNameTextField.setVisibility(View.VISIBLE);
                        filterContentSpinner.setVisibility(View.GONE);
                        break;
                    case 2:
                        filterContentSpinner.setVisibility(View.VISIBLE);
                        userNameTextField.setVisibility(View.GONE);
                        adapter = new ArrayAdapter<String>(getActivity(),
                                R.layout.filter_spinner_item, getResources().getStringArray(R.array.Price));
                        filterContentSpinner.setAdapter(adapter);
                        break;
                    case 3:
                        filterContentSpinner.setVisibility(View.GONE);
                        userNameTextField.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
