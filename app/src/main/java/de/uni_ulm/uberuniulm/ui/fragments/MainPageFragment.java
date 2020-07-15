package de.uni_ulm.uberuniulm.ui.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLng;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.uni_ulm.uberuniulm.ChatPage;
import de.uni_ulm.uberuniulm.MainPage;
import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.StartPage;
import de.uni_ulm.uberuniulm.model.notifications.NotificationsManager;
import de.uni_ulm.uberuniulm.model.ride.BookedRide;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.parking.ParkingSpots;
import de.uni_ulm.uberuniulm.model.ride.RideLoader;
import de.uni_ulm.uberuniulm.ui.main.ClickListener;
import de.uni_ulm.uberuniulm.ui.main.OfferListAdapter;
import kotlin.Triple;


public class MainPageFragment extends Fragment {

    public View fragmentView;
    ArrayList<Triple<ArrayList,OfferedRide, Float>> offeredRides;
    RecyclerView offerRecyclerView;
    private OfferListAdapter adapter;
    private DatabaseReference myRef;
    private OfferedRide offeredRide;
    private String userId;
    private ArrayList<LinearLayout> filterItems;
    private ConstraintLayout noEntrysLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_main_page, container, false);
        LinearLayout mapFragment = fragmentView.findViewById(R.id.mainPageFragmentContainer);
        mapFragment.setVisibility(View.VISIBLE);
        SearchView departure = fragmentView.findViewById(R.id.searchViewDeparture);
        SearchView destination = fragmentView.findViewById(R.id.searchViewDestination);
        ImageButton addFilterBttn = fragmentView.findViewById(R.id.addFilterBttn);
        filterItems = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("Users");

        addFilterBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout filterDialog = (FrameLayout) fragmentView.findViewById(R.id.addFilterDialog);
                filterDialog.setVisibility(View.VISIBLE);
                setUpFilterDialog();
            }
        });

        ImageButton filterConfirmBttn = fragmentView.findViewById(R.id.addFilterConfirmBttn);
        filterConfirmBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner filterTypeSpinner = fragmentView.findViewById(R.id.addFilterTypeSpinner);
                int filterType = filterTypeSpinner.getSelectedItemPosition();
                String[] filters = getResources().getStringArray(R.array.filters);
                LinearLayout filterContainer = (LinearLayout) fragmentView.findViewById(R.id.filterOverview);
                LinearLayout filterItem = null;
                Boolean itemExists = false;

                for (LinearLayout item : filterItems) {
                    if (item.getContentDescription().equals(String.valueOf(filterType))) {
                        filterItem = item;
                        itemExists = true;
                    }
                }

                if (!itemExists) {
                    filterItem = (LinearLayout) inflater.inflate(R.layout.filter_overview_item, null, false);
                    filterItem.setContentDescription(filters[filterType]);
                }

                TextView filterItemText = (TextView) filterItem.findViewById(R.id.filterItemText);

                if (filters[filterType].equals("Offeror")) {
                    EditText usernameTextField = fragmentView.findViewById(R.id.addFilterTextInput);
                    String offerorName = usernameTextField.getText().toString();
                    adapter.setUsernameFilter(offerorName);
                    filterItemText.setText(filters[filterType] + ": " + offerorName);
                } else if (filters[filterType].equals("Date")) {
                    EditText dateTextField = (EditText) fragmentView.findViewById(R.id.addFilterDateTextField);
                    EditText timeTextField = (EditText) fragmentView.findViewById(R.id.addFilterTimeTextField);
                    String date = dateTextField.getText().toString();
                    String time = timeTextField.getText().toString();
                    adapter.setDateFilter(date, time);
                    filterItemText.setText(filters[filterType] + ": " + date + " " + time);
                } else {
                    Spinner filterContentSpinner = fragmentView.findViewById(R.id.addFilterContentSpinner);
                    int filterContent = filterContentSpinner.getSelectedItemPosition();
                    adapter.setFilter(filterType, filterContent);
                    String filterName = filters[filterType];
                    if (!filters[filterType].equals("Hide booked up rides")) {
                        filterItemText.setText(filters[filterType] + ": " + fragmentView.getResources().getStringArray(getResId(filterName, R.array.class))[filterContent]);
                    } else {
                        filterItemText.setText(filters[filterType]);
                    }
                }

                ImageButton filterItemCloseButton = (ImageButton) filterItem.findViewById(R.id.filterItemCloseBttn);

                LinearLayout finalFilterItem = filterItem;
                filterItemCloseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (LinearLayout item : filterItems) {
                            if (item.getContentDescription().equals(filters[filterType])) {
                                filterItems.remove(item);
                            }
                        }
                        int filterType = Integer.parseInt(finalFilterItem.getContentDescription().toString());
                        filterContainer.removeView((ViewGroup) view.getParent());
                        adapter.deleteFilter(filterType);
                    }
                });

                FrameLayout filterDialog = (FrameLayout) fragmentView.findViewById(R.id.addFilterDialog);
                filterDialog.setVisibility(View.INVISIBLE);

                if (!itemExists) {
                    filterItem.setContentDescription(String.valueOf(filterType));
                    filterContainer.addView(filterItem);
                    filterItems.add(filterItem);
                }

            }
        });


        ImageButton filterCancelBttn = fragmentView.findViewById(R.id.addFilterCancelBttn);
        filterCancelBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FrameLayout filterDialog = (FrameLayout) fragmentView.findViewById(R.id.addFilterDialog);
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

        offeredRides = new ArrayList();

        offerRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.mainPageOfferRecyclerView);
        offerRecyclerView.setHasFixedSize(true);
        offerRecyclerView.setLayoutManager(new LinearLayoutManager(fragmentView.getContext()));

        noEntrysLayout = (ConstraintLayout) fragmentView.findViewById(R.id.noEntryContainer);

        if (offeredRides.isEmpty()) {
            offerRecyclerView.setVisibility(View.GONE);
            noEntrysLayout.setVisibility(View.VISIBLE);
        }
        else {
            offerRecyclerView.setVisibility(View.VISIBLE);
            noEntrysLayout.setVisibility(View.GONE);
        }

        ParkingSpots parkingSpots = new ParkingSpots();

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        userId = pref.getString("UserKey", "");

        RideLoader rideLoader= new RideLoader(getContext());
        rideLoader.getOfferedRides(this);

        return fragmentView;
    }

    public void updateOffers(ArrayList<Triple<ArrayList, OfferedRide, Float>> rides){
        offeredRides= rides;

        if(adapter!=null){
            offerRecyclerView= fragmentView.findViewById(R.id.mainPageOfferRecyclerView);
            offerRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else{
            setOfferAdapter();
        }

        if (offeredRides.isEmpty()) {
            offerRecyclerView.setVisibility(View.GONE);
            noEntrysLayout.setVisibility(View.VISIBLE);
        }
        else {
            offerRecyclerView.setVisibility(View.VISIBLE);
            noEntrysLayout.setVisibility(View.GONE);
        }
    }

    private void setOfferAdapter(){
        for (int i = 0; i < offeredRides.size(); i++) {
            Date date1 = null;
            try {
                date1 = new SimpleDateFormat("dd/MM/yyyy").parse(offeredRides.get(i).getSecond().getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date1.compareTo(Calendar.getInstance().getTime()) < 0) {
                offeredRides.remove(i);
            } else {
                adapter = new OfferListAdapter(fragmentView.getContext(), offeredRides, new ClickListener() {
                    @Override
                    public void onPositionClicked(int position) {

                        Triple<ArrayList,OfferedRide, Float> clickedRideTriple = offeredRides.get(position);
                        OfferedRide clickedRide = (OfferedRide) clickedRideTriple.getSecond();
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());


                        if (!clickedRide.getBookedUsers().contains(userId)) {
                            if(clickedRide.getPlaces_open()>0) {
                                dialog.setItems(getResources().getStringArray(R.array.bookoptions), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int pos) {
                                        if (pos == 0) {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                                            alert.setMessage("You want to book this ride?");
                                            alert.setTitle("Book Ride");


                                            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {

                                                    //TODO hier eigentlich Benachrichtigung an Fahrer
                                                    if (clickedRide.getPlaces_open() > 0) {
                                                        Date date = Calendar.getInstance().getTime();
                                                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                                                        myRef.child(clickedRide.getUserId()).child("offeredRides").child(String.valueOf(clickedRide.getzIndex())).child("bookedUsers").child(userId).setValue(formatter.format(date));
                                                        int places = clickedRide.getPlaces_open() - 1;
                                                        myRef.child(clickedRide.getUserId()).child("offeredRides").child(String.valueOf(clickedRide.getzIndex())).child("places_open").setValue(places);

                                                        BookedRide bookedRide = new BookedRide(clickedRide.getUserId(), clickedRide.getzIndex());
                                                        final SharedPreferences pref = new ObscuredSharedPreferences(
                                                                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("BookedRideId", Context.MODE_PRIVATE));
                                                        int obookedRideId = pref.getInt("obookedRideId", 0);

                                                        myRef.child(userId).child("obookedRides").child(String.valueOf(obookedRideId)).setValue(bookedRide);
                                                        SharedPreferences.Editor editor = pref.edit();
                                                        editor.putInt("BookedRideId", obookedRideId + 1);
                                                        editor.apply();
                                                        NotificationsManager notificationManager = new NotificationsManager();
                                                        notificationManager.setUp(getContext());
                                                        notificationManager.subscribeToTopic(clickedRide.getUserId() + "_" + clickedRide.getzIndex());
                                                    } else {
                                                        Toast.makeText(fragmentView.getContext(), "Oops, looks like somebody was a little faster than you.", Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                }
                                            });

                                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    //TODO what ever you want to do with No option.
                                                }
                                            });

                                            alert.show();
                                        } else {
                                            Intent intent = new Intent(fragmentView.getContext(), ChatPage.class);
                                            intent.putExtra("SENDERID", userId);
                                            intent.putExtra("RECEIVERNAME", clickedRideTriple.getFirst().get(1).toString());
                                            intent.putExtra("RECEIVERID", clickedRideTriple.getFirst().get(0).toString());
                                            startActivity(intent);

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
                            }else{
                                AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                                alert.setMessage("Ride already booked up. Do you want to send the rider a message?");
                                alert.setTitle("Booked up!");


                                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Intent intent = new Intent(fragmentView.getContext(), ChatPage.class);
                                        intent.putExtra("SENDERID", userId);
                                        intent.putExtra("RECEIVERNAME", clickedRideTriple.getFirst().get(1).toString());
                                        intent.putExtra("RECEIVERID", clickedRideTriple.getFirst().get(0).toString());
                                        startActivity(intent);
                                    }

                                });

                                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //TODO what ever you want to do with No option.
                                    }
                                });

                                alert.show();

                            }

                        } else {
                            dialog.setItems(getResources().getStringArray(R.array.cancelbookingoptions), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int pos) {
                                    if (pos == 0) {
                                        AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                                        alert.setMessage("Do you really want to cancel your booking?");
                                        alert.setTitle("Book Ride");


                                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                DatabaseReference myRef = database.getReference().child("Users");
                                                final SharedPreferences pref = new ObscuredSharedPreferences(
                                                        fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("BookedRideId", Context.MODE_PRIVATE));
                                                int obookedRidesId = pref.getInt("oBookedRidesId", 0);
                                                myRef.child(userId).child("obookedRides").child(String.valueOf(obookedRidesId)).removeValue();
                                                myRef.child(clickedRide.getUserId()).child("offeredRides").child(String.valueOf(clickedRide.getzIndex())).child("bookedUsers").child(userId).removeValue();
                                                int places = clickedRide.getPlaces_open() + 1;
                                                myRef.child(clickedRide.getUserId()).child("offeredRides").child(String.valueOf(clickedRide.getzIndex())).child("places_open").setValue(places);
                                                clickedRide.getBookedUsers().remove(userId);

                                                SharedPreferences.Editor editor = pref.edit();
                                                editor.putInt("BookedRideId", obookedRidesId - 1);
                                                editor.apply();
                                                NotificationsManager notificationManager=new NotificationsManager();
                                                notificationManager.setUp(getContext());
                                                notificationManager.unsubscribeTopic(clickedRide.getUserId()+"_"+clickedRide.getzIndex());
                                            }
                                        });

                                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                            }
                                        });

                                        alert.show();
                                    } else {
                                        Intent intent = new Intent(fragmentView.getContext(), ChatPage.class);
                                        intent.putExtra("SENDERID", userId);
                                        intent.putExtra("RECEIVERNAME", clickedRideTriple.getFirst().get(1).toString());
                                        intent.putExtra("RECEIVERID", clickedRideTriple.getFirst().get(0).toString());
                                        startActivity(intent);
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
                    }

                    @Override
                    public void onOfferClicked(int position) {
                        Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
                        Triple clickedRidePair = offeredRides.get(position);
                        OfferedRide clickedRide = (OfferedRide) clickedRidePair.getSecond();
                        intent.putExtra("RATING", (float) clickedRidePair.getThird());
                        intent.putExtra("USER", (ArrayList) clickedRidePair.getFirst());
                        intent.putExtra("RIDE", clickedRide);
                        intent.putExtra("VIEWTYPE", "RIDEOVERVIEW");
                        startActivity(intent);
                    }

                    @Override
                    public void onMarkClicked(View view, int position) {
                        OfferedRide ride = offeredRides.get(position).getSecond();
                        Boolean notMarkedYet = ride.getObservers().contains(userId);
                        if (!userId.equals(ride.getUserId())) {
                            Button markBttn = (Button) view;
                            if (notMarkedYet) {
                                ride.unmarkRide(userId);
                                markBttn.setBackgroundResource(R.drawable.ic_unmarked);
                            } else {
                                ride.markRide(userId);
                                markBttn.setBackgroundResource(R.drawable.ic_marked);
                            }

                            myRef.child(offeredRides.get(position).getFirst().get(0).toString()).child("offeredRides").child(String.valueOf(ride.getzIndex())).child("observers").setValue(ride.getObservers());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onEditClicked(int position) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setTitle("Edit your offer");
                        dialog.setItems(getResources().getStringArray(R.array.editoptions), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int pos) {
                                if (pos == 0) {
                                    Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
                                    Triple clickedRidePair = offeredRides.get(pos);
                                    OfferedRide clickedRide = (OfferedRide) clickedRidePair.getSecond();
                                    intent.putExtra("RATING", (float) clickedRidePair.getThird());
                                    intent.putExtra("USER", (ArrayList) clickedRidePair.getFirst());
                                    intent.putExtra("RIDE", clickedRide);
                                    intent.putExtra("VIEWTYPE", "EDITOFFER");
                                    startActivity(intent);
                                } else {
                                    AlertDialog.Builder dialogWarning = new AlertDialog.Builder(getActivity());
                                    dialogWarning.setTitle("Warning!");
                                    Triple clickedRidePair = offeredRides.get(pos);
                                    OfferedRide clickedRide = (OfferedRide) clickedRidePair.getSecond();
                                    if (clickedRide.getPlaces() > clickedRide.getPlaces_open()) {
                                        dialogWarning.setMessage("Do you really want to delete this ride? The people who booked your trip will be notified.");
                                    } else {
                                        dialogWarning.setMessage("Do you really want to delete this ride?");
                                    }
                                    dialogWarning.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogWarning, int which) {
                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference myRef = database.getReference().child("Users");
                                            myRef.child(userId).child("offeredRides").child(String.valueOf(offeredRides.get(position).getSecond().getzIndex())).removeValue();
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

                offerRecyclerView.setAdapter(adapter);
            }
        }
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

        LinearLayout dateContainer= (LinearLayout) fragmentView.findViewById(R.id.addFilterDateContainer);

        filterTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ArrayAdapter adapter;
                switch(position){
                    case 0:
                        filterContentSpinner.setVisibility(View.VISIBLE);
                        userNameTextField.setVisibility(View.GONE);
                        dateContainer.setVisibility(View.GONE);
                        adapter = new ArrayAdapter<String>(getActivity(),
                                R.layout.filter_spinner_item, getResources().getStringArray(R.array.Distance));
                        filterContentSpinner.setAdapter(adapter);
                        break;
                    case 1:
                        userNameTextField.setVisibility(View.VISIBLE);
                        filterContentSpinner.setVisibility(View.GONE);
                        dateContainer.setVisibility(View.GONE);
                        break;
                    case 2:
                        filterContentSpinner.setVisibility(View.VISIBLE);
                        userNameTextField.setVisibility(View.GONE);
                        dateContainer.setVisibility(View.GONE);
                        adapter = new ArrayAdapter<String>(getActivity(),
                                R.layout.filter_spinner_item, getResources().getStringArray(R.array.Price));
                        filterContentSpinner.setAdapter(adapter);
                        break;
                    case 3:
                        filterContentSpinner.setVisibility(View.GONE);
                        userNameTextField.setVisibility(View.GONE);
                        dateContainer.setVisibility(View.GONE);
                        break;
                    case 4:
                        filterContentSpinner.setVisibility(View.GONE);
                        userNameTextField.setVisibility(View.GONE);
                        dateContainer.setVisibility(View.VISIBLE);


                        EditText dateTextField= (EditText) fragmentView.findViewById(R.id.addFilterDateTextField);
                        Calendar calendar= Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        dateTextField.setText(sdf.format(calendar.getTime()));
                        dateTextField.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                selectDate(view);
                            }
                        });

                        EditText timeTextField= (EditText) fragmentView.findViewById(R.id.addFilterTimeTextField);
                        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                        timeTextField.setText(timeFormatter.format(calendar.getTime()));
                        timeTextField.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                selectTime(view);
                            }
                        });
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    private static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    private void selectDate(View view){
        EditText dateTextField= (EditText) fragmentView.findViewById(R.id.addFilterDateTextField);
        DatePickerDialog.OnDateSetListener mDateSetListener = null;
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                cal.set(year, month, day);
                cal.add(Calendar.MONTH, 1);
                String date = dayOfMonth + "/" + cal.getTime().getMonth() + "/" + year;
                dateTextField.setText(date);
            }
        };

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.spinnerDatePickerStyle,mDateSetListener,year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getActivity().getColor(R.color.colorSlightlyTransparentBlack)));
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    
    private void selectTime(View view){
        EditText timeTextField= (EditText) fragmentView.findViewById(R.id.addFilterTimeTextField);
        TimePickerDialog.OnTimeSetListener mTimeSetListener;
        final Calendar myCalender = Calendar.getInstance();
        int hour = myCalender.get(Calendar.HOUR_OF_DAY);
        int minute = myCalender.get(Calendar.MINUTE);
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {
                    myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    myCalender.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                    String time = hourOfDay + ":" + minute;
                    timeTextField.setText(time);
                }
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), R.style.timePickerStyle, mTimeSetListener, hour, minute, true);
        timePickerDialog.setTitle("Choose time:");
        timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getActivity().getColor(R.color.colorSlightlyTransparentBlack)));
        timePickerDialog.show();
        timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
    }

}
