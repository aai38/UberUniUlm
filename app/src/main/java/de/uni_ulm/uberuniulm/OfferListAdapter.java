package de.uni_ulm.uberuniulm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import de.uni_ulm.uberuniulm.model.User;

public class OfferListAdapter extends RecyclerView.Adapter<OfferListAdapter.OfferViewHolder>  implements View.OnClickListener{

    private ArrayList<Pair<String,OfferedRide>> dataSet;
    Context mContext;

    public OfferListAdapter(Context mContext, ArrayList<Pair<String,OfferedRide>> offeredRides) {
        this.mContext = mContext;
        dataSet = offeredRides;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.ride_item_main_page, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder viewHolder, int position) {
        String userID=dataSet.get(position).first;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+userID.substring(1)+".jpg");
        Log.d("IMAGETOBEDISPLAYED", "profile_images/"+userID.substring(1)+".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        profileImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ImageView image = (ImageView) viewHolder.picture;
                image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),
                        image.getHeight(), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int id = mContext.getResources().getIdentifier("de.uni_ulm.uberuniulm:drawable/" + "start_register_profile_photo.png", null, null);
                viewHolder.picture.setImageResource(id);
            }
        });
        OfferedRide offeredRide = dataSet.get(position).second;

        viewHolder.txtDestination.setText(offeredRide.getDestination());
        viewHolder.txtDeparture.setText(offeredRide.getDeparture());
        viewHolder.txtDate.setText(offeredRide.getDate().toString());
        viewHolder.txtTime.setText(offeredRide.getTime().toString());
        viewHolder.txtPrice.setText(offeredRide.getPrice() + "€");
        viewHolder.txtPlaces.setText((offeredRide.getPlaces() - offeredRide.getPlaces_open()) + "/" + offeredRide.getPlaces());
        //viewHolder.rating.setImageIcon();
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onClick(View v) {
        int position=(Integer) v.getTag();
        Object object= dataSet.get(position);
        OfferedRide bookedRide=(OfferedRide) object;
    }


    class OfferViewHolder extends RecyclerView.ViewHolder {

        TextView txtDestination, txtDeparture, txtDate, txtPlaces, txtPrice, txtTime;
        ImageView picture;
        RatingBar rating;

        public OfferViewHolder(@NonNull View convertView) {
            super(convertView);

            txtDestination = (TextView) convertView.findViewById(R.id.TextViewDestination);
            txtDeparture = (TextView) convertView.findViewById(R.id.textViewDeparture);
            txtDate = (TextView) convertView.findViewById(R.id.textViewDate);
            picture = (ImageView) convertView.findViewById(R.id.profileImageDrawer);
            rating = (RatingBar) convertView.findViewById(R.id.ratingView);
            txtPlaces = (TextView) convertView.findViewById(R.id.TextViewPlaces);
            txtPrice = (TextView) convertView.findViewById(R.id.TextViewPrice);
            txtTime = (TextView) convertView.findViewById(R.id.TextViewTime);
        }
    }
}
