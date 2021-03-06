package de.uni_ulm.uberuniulm.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.Rating;
import de.uni_ulm.uberuniulm.model.encryption.ObscuredSharedPreferences;
import de.uni_ulm.uberuniulm.model.ride.OfferedRide;
import de.uni_ulm.uberuniulm.model.filter.Filters;
import kotlin.Triple;

public class OfferListAdapter extends RecyclerView.Adapter<OfferListAdapter.OfferViewHolder>  implements View.OnClickListener{

    private ArrayList<Triple<ArrayList,OfferedRide, Float>> dataSet;
    private final ClickListener listener;
    Context mContext;
    Filters filters;
    private ArrayList<Triple<ArrayList,OfferedRide, Float>> dataSetCopy = new ArrayList<>();

    public OfferListAdapter(Context mContext, ArrayList<Triple<ArrayList,OfferedRide, Float>> offeredRides, ClickListener listener) {
        this.listener = listener;
        this.mContext = mContext;
        filters= new Filters(mContext);
        dataSet = offeredRides;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        for(int i=0; i<dataSet.size();i++){
            if(!dataSetCopy.contains(dataSet.get(i))){
                dataSetCopy.add(dataSet.get(i));
            }
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.ride_item_main_page, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder viewHolder, int position) {

            String userID = (String) dataSet.get(position).getFirst().get(0);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference profileImageRef = storageRef.child("profile_images/" + userID + ".jpg");
            ImageView image = (ImageView) viewHolder.picture;
            Glide.with(mContext)
                    .load(profileImageRef)
                    .centerCrop()
                    .placeholder(R.drawable.start_register_profile_photo)
                    .skipMemoryCache(true) //2
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .thumbnail(/*sizeMultiplier=*/ 0.2f)
                    .into(image);

            OfferedRide offeredRide = dataSet.get(position).getSecond();

            SharedPreferences pref = new ObscuredSharedPreferences(mContext, mContext.getSharedPreferences("UserKey", Context.MODE_PRIVATE));
            String userId = pref.getString("UserKey", "");
            viewHolder.markBttn.setContentDescription("mark");

            if (userId.equals(userID)) {
                viewHolder.book.setImageResource(R.drawable.ic_edit_gray);
                viewHolder.book.setContentDescription("edit");
                viewHolder.markBttn.setBackgroundResource(R.drawable.ic_marked);
                viewHolder.markBttn.setText(String.valueOf(offeredRide.getObservers().size()));
            } else {
                if (offeredRide.getObservers().contains(userId)) {
                    viewHolder.markBttn.setBackgroundResource(R.drawable.ic_marked);
                } else {
                    viewHolder.markBttn.setBackgroundResource(R.drawable.ic_unmarked);
                }
                viewHolder.book.setImageResource(R.drawable.ic_book);
                viewHolder.book.setContentDescription("book");
            }
            viewHolder.txtDestination.setText(offeredRide.getDestination());
            viewHolder.txtDeparture.setText(offeredRide.getDeparture());
            viewHolder.txtDate.setText(offeredRide.getDate().toString());
            viewHolder.txtTime.setText(offeredRide.getTime().toString());
            viewHolder.txtPrice.setText(offeredRide.getPrice() + "€");
            viewHolder.txtPlaces.setText((offeredRide.getPlaces() - offeredRide.getPlaces_open()) + "/" + offeredRide.getPlaces());


            viewHolder.rating.setRating(dataSet.get(position).getThird());



    }

    public void filterDeparture(String text) {
        dataSet.clear();
        if(text.isEmpty()){
            dataSet.addAll(dataSetCopy);
        } else{
            text = text.toLowerCase();
            for(Triple item: dataSetCopy){
                OfferedRide offeredRide = (OfferedRide) item.getSecond();
                if(offeredRide.getDeparture().toLowerCase().contains(text)){
                    dataSet.add(item);
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

        dataSet.clear();
        if(text.isEmpty()){
            dataSet.addAll(dataSetCopy);
        } else{
            text = text.toLowerCase();
            for(Triple item: dataSetCopy){
                OfferedRide offeredRide = (OfferedRide) item.getSecond();
                if(offeredRide.getDestination().toLowerCase().contains(text)){
                    dataSet.add(item);
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
        Button markBttn;
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
            markBttn = (Button) convertView.findViewById(R.id.markBttn);

            book.setOnClickListener(this);
            markBttn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == book.getId()) {
                if(book.getContentDescription().equals("edit")){
                    listenerRef.get().onEditClicked(getAdapterPosition());
                }else {
                    listenerRef.get().onPositionClicked(getAdapterPosition());
                }
            }else if(view.getId() == markBttn.getId()) {
                listenerRef.get().onMarkClicked(view, getAdapterPosition());
            } else{
                listenerRef.get().onOfferClicked(getAdapterPosition());
            }
        }


    }
}
