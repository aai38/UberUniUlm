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

import java.util.ArrayList;
import java.util.HashMap;

import de.uni_ulm.uberuniulm.MapPage;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.ride.BookedRide;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import kotlin.Triple;

public class RideOverviewHeaderFragment extends Fragment {
    public View fragmentView;
    private MapPage mapPage;
    private TextView userNameText, startGoalText, dateText, carInfoText, priceText;
    private ImageView closeBttn;
    private ImageButton bookBttn, markBttn;
    private RatingBar ratingBar;
    private com.mikhaellopez.circularimageview.CircularImageView profilePhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_ride_overview, container, false);
        mapPage = (MapPage) getActivity();

        userNameText= fragmentView.findViewById(R.id.rideOverviewUserNameText);
        startGoalText= fragmentView.findViewById(R.id.rideOfferStartGoalText);
        dateText= fragmentView.findViewById(R.id.ridOfferDateText);
        profilePhoto= fragmentView.findViewById(R.id.offerOverviewProfileImageDrawer);
        carInfoText= fragmentView.findViewById(R.id.rideOverviewCarInfoText);
        closeBttn= fragmentView.findViewById(R.id.rideOverviewCloseBttn);
        markBttn= fragmentView.findViewById(R.id.rideOverviewMarkButton);
        bookBttn= fragmentView.findViewById(R.id.rideOverviewBookingButton);
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
            markBttn.setVisibility(View.INVISIBLE);
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


        }else {
            if(ride.getObservers().contains(userId)){
                markBttn.setImageResource(R.drawable.ic_mark_offer);
            }else{
                markBttn.setImageResource(R.drawable.ic_mark_offer_deselected);
            }

            bookBttn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(fragmentView.getContext());

                            alert.setMessage("You want to book this ride?");
                            alert.setTitle("Book Ride");


                            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference().child("Users");
                                    BookedRide bookedRide = new BookedRide(ride.getUserId(), ride.getzIndex());
                                    final SharedPreferences pref = new ObscuredSharedPreferences(
                                            fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("BookedRideId", Context.MODE_PRIVATE));
                                    int zIndex = pref.getInt("BookedRideId", 0);

                                    //hier eigentlich Benachrichtigung an Fahrer
                                    myRef.child((String) userData.get(0)).child("obookedRides").child(String.valueOf(zIndex)).setValue(bookedRide);

                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putInt("BookedRideId", zIndex + 1);
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
                    });

            markBttn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ride.getObservers().contains(userId)){
                        mapPage.markRide(false);
                        markBttn.setImageResource(R.drawable.ic_mark_offer_deselected);
                    }else{
                        mapPage.markRide(true);
                        markBttn.setImageResource(R.drawable.ic_mark_offer);
                    }
                }
            });
        }

        dateText.setText(ride.getDate()+ " "+ ride.getTime());
        startGoalText.setText(ride.getDeparture()+ " <-> "+ ride.getDestination());
        carInfoText.setText(ride.getPlaces()-ride.getPlaces_open()+"/"+ride.getPlaces());
        Log.d("CARINFO", String.valueOf(ride.getPrice()));

        priceText= fragmentView.findViewById(R.id.rideOverviewPriceText);
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
