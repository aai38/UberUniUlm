package de.uni_ulm.uberuniulm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.BookedRide;
import de.uni_ulm.uberuniulm.model.OfferedRide;

public class CustomAdapter extends ArrayAdapter<OfferedRide> implements View.OnClickListener{

    private ArrayList<OfferedRide> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtDestination;
        TextView txtDeparture;
        TextView txtDate;
        TextView txtTime;
        ImageView rating;
        TextView txtPrice;
        TextView txtPlaces;
        ImageView picture;
    }

    public CustomAdapter(ArrayList<OfferedRide> data, Context context) {
        super(context, R.layout.ride_item_main_page, data);
        this.dataSet = data;
        this.mContext=context;

    }

    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        OfferedRide bookedRide=(OfferedRide) object;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        OfferedRide offeredRide = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.ride_item_main_page, parent, false);
            viewHolder.txtDestination = (TextView) convertView.findViewById(R.id.TextViewDestination);
            viewHolder.txtDeparture = (TextView) convertView.findViewById(R.id.textViewDeparture);
            viewHolder.txtDate = (TextView) convertView.findViewById(R.id.textViewDate);
            viewHolder.picture = (ImageView) convertView.findViewById(R.id.imageView);
            viewHolder.rating = (ImageView) convertView.findViewById(R.id.ratingView);
            viewHolder.txtPlaces = (TextView) convertView.findViewById(R.id.TextViewPlaces);
            viewHolder.txtPrice = (TextView) convertView.findViewById(R.id.TextViewPrice);
            viewHolder.txtTime = (TextView) convertView.findViewById(R.id.TextViewTime);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }


        lastPosition = position;

        viewHolder.txtDestination.setText(offeredRide.getDestination());
        viewHolder.txtDeparture.setText(offeredRide.getDeparture());
        viewHolder.txtDate.setText(offeredRide.getDate().toString());
        viewHolder.txtTime.setText(offeredRide.getTime().toString());
        viewHolder.txtPrice.setText(offeredRide.getPrice() + "€");
        viewHolder.txtPlaces.setText((offeredRide.getPlaces() - offeredRide.getPlaces_open()) + "/" + offeredRide.getPlaces());
        //viewHolder.rating.setImageIcon();
        Drawable myDrawable = convertView.getContext().getDrawable(R.drawable.start_register_profile_photo);
        viewHolder.picture.setImageDrawable(myDrawable);

        // Return the completed view to render on screen
        return convertView;
    }
}