package de.uni_ulm.uberuniulm.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import de.uni_ulm.uberuniulm.model.filter.Filters;

public class OfferListAdapter extends RecyclerView.Adapter<OfferListAdapter.OfferViewHolder>  implements View.OnClickListener{

    private ArrayList<Pair<ArrayList,OfferedRide>> dataSet;
    private final ClickListener listener;
    Context mContext;
    Filters filters;
    private ArrayList<Pair<ArrayList,OfferedRide>> dataSetCopy = new ArrayList<>();

    public OfferListAdapter(Context mContext, ArrayList<Pair<ArrayList,OfferedRide>> offeredRides, ClickListener listener) {
        this.listener = listener;
        this.mContext = mContext;
        filters= new Filters(mContext);
        dataSet = offeredRides;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        dataSetCopy.addAll(dataSet);
        View view = LayoutInflater.from(mContext).inflate(R.layout.ride_item_main_page, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder viewHolder, int position) {


        String userID=(String) dataSet.get(position).first.get(0);
        Float rating= (Float) dataSet.get(position).first.get(2);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/"+userID+".jpg");

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
            }
        });
        OfferedRide offeredRide = dataSet.get(position).second;

        SharedPreferences pref = new ObscuredSharedPreferences(mContext, mContext.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
        String userId = pref.getString("UserKey", "");

        if(userId.equals(userID)){
            viewHolder.book.setImageResource(R.drawable.ic_edit_gray);
            viewHolder.book.setContentDescription("edit");
        }else{
            viewHolder.book.setImageResource(android.R.drawable.ic_dialog_email);
            viewHolder.book.setContentDescription("book");
        }

        viewHolder.txtDestination.setText(offeredRide.getDestination());
        viewHolder.txtDeparture.setText(offeredRide.getDeparture());
        viewHolder.txtDate.setText(offeredRide.getDate().toString());
        viewHolder.txtTime.setText(offeredRide.getTime().toString());
        viewHolder.txtPrice.setText(offeredRide.getPrice() + "€");
        viewHolder.txtPlaces.setText((offeredRide.getPlaces() - offeredRide.getPlaces_open()) + "/" + offeredRide.getPlaces());
        if(rating<=0){
            viewHolder.rating.setRating(0);
        }else{
            viewHolder.rating.setRating(rating);
        }

    }

    public void filterDeparture(String text) {
        Log.e("dataSetcopy", dataSetCopy.toString());
        dataSet.clear();
        if(text.isEmpty()){
            dataSet.addAll(dataSetCopy);
        } else{
            text = text.toLowerCase();
            for(Pair item: dataSetCopy){
                Log.e("item", item.second.toString());
                OfferedRide offeredRide = (OfferedRide) item.second;
                if(offeredRide.getDeparture().toLowerCase().contains(text)){
                    dataSet.add(item);
                    Log.e("dataset", dataSet.toString());
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setFilter(int filtertype, int contentIndex){
        dataSet=filters.newFilter(dataSetCopy, filtertype, contentIndex);
        notifyDataSetChanged();
    }

    public void deleteFilter(int filtertype){
        dataSet=filters.deleteFilter(dataSetCopy, filtertype);
        notifyDataSetChanged();
    }

    public void setUsernameFilter(String username){
        dataSet= filters.setUsernameFilter(dataSetCopy, username);
        notifyDataSetChanged();
    }

    public void setDateFilter(String date, String time){
        dataSet=filters.setDateFilter(dataSetCopy, date, time);
        notifyDataSetChanged();
    }

    public void filterDestination(String text) {

        Log.e("dataSetcopy", dataSetCopy.toString());
        dataSet.clear();
        if(text.isEmpty()){
            dataSet.addAll(dataSetCopy);
        } else{
            text = text.toLowerCase();
            for(Pair item: dataSetCopy){
                Log.e("item", item.second.toString());
                OfferedRide offeredRide = (OfferedRide) item.second;
                if(offeredRide.getDestination().toLowerCase().contains(text)){
                    dataSet.add(item);
                    Log.e("dataset", dataSet.toString());
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public void onClick(View v) {
        int position=(Integer) v.getTag();

    }


    class OfferViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView txtDestination, txtDeparture, txtDate, txtPlaces, txtPrice, txtTime;
        ImageView picture;
        RatingBar rating;
        ImageButton book;
        private WeakReference<ClickListener> listenerRef;

        public OfferViewHolder(@NonNull View convertView) {
            super(convertView);
            convertView.setOnClickListener(this);

            listenerRef = new WeakReference<>(listener);

            txtDestination = (TextView) convertView.findViewById(R.id.TextViewDestination);
            txtDeparture = (TextView) convertView.findViewById(R.id.textViewDeparture);
            txtDate = (TextView) convertView.findViewById(R.id.textViewDate);
            picture = (ImageView) convertView.findViewById(R.id.profileImageDrawer);
            rating = (RatingBar) convertView.findViewById(R.id.ratingView);
            txtPlaces = (TextView) convertView.findViewById(R.id.TextViewPlaces);
            txtPrice = (TextView) convertView.findViewById(R.id.TextViewPrice);
            txtTime = (TextView) convertView.findViewById(R.id.TextViewTime);
            book = (ImageButton) convertView.findViewById(R.id.bookingButton);

            book.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == book.getId()) {
                Log.d("listener", book.getId() + "");
                if(book.getContentDescription().equals("edit")){
                    listenerRef.get().onEditClicked(getAdapterPosition());
                }else {
                    listenerRef.get().onPositionClicked(getAdapterPosition());
                }
            } else {
                listenerRef.get().onOfferClicked(getAdapterPosition());
            }
        }


    }
}
