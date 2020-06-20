package de.uni_ulm.uberuniulm.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.MapActivity;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.OfferedRide;

public class RideOverviewHeaderFragment extends Fragment {
    public View fragmentView;
    private MapActivity mapActivity;
    private TextView userNameText, startGoalText, dateText, carInfoText, priceText;
    private ImageView closeBttn;
    private ImageButton bookBttn, markBttn;
    private com.mikhaellopez.circularimageview.CircularImageView profilePhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_ride_overview, container, false);
        mapActivity = (MapActivity) getActivity();

        userNameText= fragmentView.findViewById(R.id.rideOverviewUserNameText);
        startGoalText= fragmentView.findViewById(R.id.rideOfferStartGoalText);
        dateText= fragmentView.findViewById(R.id.ridOfferDateText);
        profilePhoto= fragmentView.findViewById(R.id.offerOverviewProfileImageDrawer);
        carInfoText= fragmentView.findViewById(R.id.rideOverviewCarInfoText);
        closeBttn= fragmentView.findViewById(R.id.rideOverviewCloseBttn);
        markBttn= fragmentView.findViewById(R.id.rideOverviewMarkButton);
        bookBttn= fragmentView.findViewById(R.id.rideOverviewBookingButton);


        closeBttn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mapActivity.closeMapView();
                    }
                });

        Pair<ArrayList, OfferedRide> rideData=mapActivity.getRideData();
        ArrayList userData=rideData.first;
        OfferedRide ride=rideData.second;

        SharedPreferences pref = new ObscuredSharedPreferences(
                fragmentView.getContext(), fragmentView.getContext().getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        String userId = pref.getString("UserKey", "");

        if(userId.equals(userData.get(0))){
            markBttn.setVisibility(View.INVISIBLE);
            bookBttn.setVisibility(View.INVISIBLE);
        }else {


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
                                    DatabaseReference myRef = database.getReference();
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
        }

        dateText.setText(ride.getDate()+ " "+ ride.getTime());
        startGoalText.setText(ride.getDeparture()+ " <-> "+ ride.getDestination());
        carInfoText.setText(ride.getPlaces()-ride.getPlaces_open()+"/"+ride.getPlaces());
        Log.d("CARINFO", String.valueOf(ride.getPrice()));

        priceText= fragmentView.findViewById(R.id.rideOverviewPriceText);
        priceText.setText(ride.getPrice()+"€");


        userNameText.setText((String) userData.get(1));

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+userData.get(0)+".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        profileImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ImageView image = (ImageView) profilePhoto;
                image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),
                        image.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });


        return fragmentView;
    }

}
