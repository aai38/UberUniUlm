package de.uni_ulm.uberuniulm.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.MapActivity;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.OfferedRide;

public class RideOverviewHeaderFragment extends Fragment {
    public View fragmentView;
    private MapActivity mapActivity;
    private TextView userNameText, startGoalText, dateText, carInfoText, priceText;
    private ImageView closeBttn;
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
