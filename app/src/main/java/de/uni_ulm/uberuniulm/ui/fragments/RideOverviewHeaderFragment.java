package de.uni_ulm.uberuniulm.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.uni_ulm.uberuniulm.ChatPage;
import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.notifications.NotificationsManager;
import de.uni_ulm.uberuniulm.model.ride.BookedRide;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import kotlin.Triple;

public class RideOverviewHeaderFragment extends Fragment {
    public View fragmentView;
    private MapPage mapPage;
    private TextView userNameText, startText, goalText, dateText, timeText, carInfoText, priceText;
    private ImageView closeBttn;
    private ImageButton bookBttn, markBttn, bookersBttn;
    private RatingBar ratingBar;
    private com.mikhaellopez.circularimageview.CircularImageView profilePhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_ride_overview, container, false);
        mapPage = (MapPage) getActivity();

        userNameText= fragmentView.findViewById(R.id.rideOverviewUserNameText);
        startText= fragmentView.findViewById(R.id.rideOfferStartText);
        goalText= fragmentView.findViewById(R.id.rideOfferGoalText);
        dateText= fragmentView.findViewById(R.id.rideOfferDateText);
        timeText= fragmentView.findViewById(R.id.rideOfferTimeText);
        profilePhoto= fragmentView.findViewById(R.id.offerOverviewProfileImageDrawer);
        carInfoText= fragmentView.findViewById(R.id.rideOverviewCarInfoText);
        closeBttn= fragmentView.findViewById(R.id.rideOverviewCloseBttn);
        markBttn= fragmentView.findViewById(R.id.rideOverviewMarkButton);
        bookBttn= fragmentView.findViewById(R.id.rideOverviewBookingButton);
        bookersBttn= fragmentView.findViewById(R.id.rideOverviewBookersButton);
        ratingBar = fragmentView.findViewById(R.id.ratingView);


        closeBttn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mapPage.closeMapView();
                    }
                });

        Triple<ArrayList, OfferedRide, Float> rideData= mapPage.getRideData();
        ArrayList userData=rideData.getFirst();
        OfferedRide ride=rideData.getSecond();
        float rating = rideData.getThird();

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        String userId = pref.getString("UserKey", "");

        if(userId.equals(userData.get(0))){
            markBttn.setVisibility(View.GONE);
            bookBttn.setImageResource(R.drawable.ic_edit_gray);
            bookBttn.setContentDescription("edit");
            bookBttn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(fragmentView.getContext(), MapPage.class);
                            intent.putExtra("USER", mapPage.getRideData().getFirst());
                            intent.putExtra("RIDE", mapPage.getRideData().getSecond());
                            intent.putExtra("RATING", mapPage.getRideData().getThird());
                            intent.putExtra("VIEWTYPE", "EDITOFFER");
                            startActivity(intent);
                        }
                    });

            if(ride.getBookedUsers().size()==0){
                bookersBttn.setVisibility(View.INVISIBLE);
            }else {
                bookersBttn.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mapPage.showBookers();
                            }
                        });
            }

        }else {
            if(ride.getObservers().contains(userId)){
                markBttn.setImageResource(R.drawable.ic_marked_primary);
            }else{
                markBttn.setImageResource(R.drawable.ic_unmarked_primary);
            }

            bookersBttn.setVisibility(View.INVISIBLE);
            bookBttn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());


                            if (!ride.getBookedUsers().contains(userId)) {
                                if (ride.getPlaces_open() > 0) {
                                    dialog.setItems(getResources().getStringArray(R.array.bookoptions), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int pos) {
                                            if (pos == 0) {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                                                alert.setMessage("You want to book this ride?");
                                                alert.setTitle("Book Ride");


                                                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                        DatabaseReference myRef = database.getReference().child("Users");
                                                        if (ride.getPlaces_open() > 0) {
                                                            Date date = Calendar.getInstance().getTime();
                                                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                                                            myRef.child(ride.getUserId()).child("offeredRides").child(String.valueOf(ride.getzIndex())).child("bookedUsers").child(userId).setValue(formatter.format(date));
                                                            int places = ride.getPlaces_open() - 1;
                                                            myRef.child(ride.getUserId()).child("offeredRides").child(String.valueOf(ride.getzIndex())).child("places_open").setValue(places);

                                                            BookedRide bookedRide = new BookedRide(ride.getUserId(), ride.getzIndex());
                                                            final SharedPreferences pref = new ObscuredSharedPreferences(
                                                                    fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("BookedRideId", Context.MODE_PRIVATE));
                                                            int obookedRideId = pref.getInt("obookedRideId", 0);

                                                            myRef.child(userId).child("obookedRides").child(String.valueOf(obookedRideId)).setValue(bookedRide);
                                                            SharedPreferences.Editor editor = pref.edit();
                                                            editor.putInt("BookedRideId", obookedRideId + 1);
                                                            editor.apply();
                                                            NotificationsManager notificationManager = new NotificationsManager();
                                                            notificationManager.setUp(getContext());
                                                            notificationManager.subscribeToTopic(ride.getUserId() + "_" + ride.getzIndex());
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
                                                intent.putExtra("RECEIVERNAME", rideData.getFirst().get(1).toString());
                                                intent.putExtra("RECEIVERID", rideData.getFirst().get(0).toString());
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
                                } else {
                                    Intent intent = new Intent(fragmentView.getContext(), ChatPage.class);
                                    intent.putExtra("SENDERID", userId);
                                    intent.putExtra("RECEIVERNAME", rideData.getFirst().get(1).toString());
                                    intent.putExtra("RECEIVERID", rideData.getFirst().get(0).toString());
                                    startActivity(intent);
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

                                                    //TODO hier eigentlich Benachrichtigung an Fahrer
                                                    myRef.child(userId).child("obookedRides").child(String.valueOf(obookedRidesId)).removeValue();
                                                    myRef.child(ride.getUserId()).child("offeredRides").child(String.valueOf(ride.getzIndex())).child("bookedUsers").child(userId).removeValue();
                                                    int places = ride.getPlaces_open() + 1;
                                                    myRef.child(ride.getUserId()).child("offeredRides").child(String.valueOf(ride.getzIndex())).child("places_open").setValue(places);
                                                    ride.getBookedUsers().remove(userId);

                                                    SharedPreferences.Editor editor = pref.edit();
                                                    editor.putInt("BookedRideId", obookedRidesId - 1);
                                                    editor.apply();
                                                    NotificationsManager notificationManager = new NotificationsManager();
                                                    notificationManager.setUp(getContext());
                                                    notificationManager.unsubscribeTopic(ride.getUserId() + "_" + ride.getzIndex());
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
                                            intent.putExtra("RECEIVERNAME", rideData.getFirst().get(1).toString());
                                            intent.putExtra("RECEIVERID", rideData.getFirst().get(0).toString());
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
                    });

            markBttn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ride.getObservers().contains(userId)){
                        mapPage.markRide(false);
                        markBttn.setImageResource(R.drawable.ic_unmarked_primary);
                    }else{
                        mapPage.markRide(true);
                        markBttn.setImageResource(R.drawable.ic_marked_primary);
                    }
                }
            });
        }

        dateText.setText(ride.getDate());
        timeText.setText(ride.getTime());
        startText.setText(ride.getDeparture());
        goalText.setText(ride.getDestination());
        carInfoText.setText(ride.getPlaces()-ride.getPlaces_open()+"/"+ride.getPlaces());
        Log.d("CARINFO", String.valueOf(ride.getPrice()));

        priceText= fragmentView.findViewById(R.id.rideOfferPriceText);
        priceText.setText(ride.getPrice()+"€");


        ratingBar.setRating(rating);



        userNameText.setText((String) userData.get(1));

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+userData.get(0)+".jpg");
        ImageView image = (ImageView) profilePhoto;
        Glide.with(getContext())
                .load(profileImageRef)
                .centerCrop()
                .skipMemoryCache(true) //2
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.start_register_profile_photo)
                .thumbnail(/*sizeMultiplier=*/ 0.25f)
                .into(profilePhoto);


        return fragmentView;
    }

}
