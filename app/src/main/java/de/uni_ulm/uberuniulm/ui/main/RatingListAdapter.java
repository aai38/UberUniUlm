package de.uni_ulm.uberuniulm.ui.main;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.Rating;

public class RatingListAdapter extends RecyclerView.Adapter<RatingListAdapter.RatingViewHolder> {
    Context mContext;
    private ArrayList dataSet;
    public RatingListAdapter(Context mContext, ArrayList<Rating> ratings) {
        this.mContext = mContext;
        dataSet = ratings;
    }
    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.rating_item, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        Rating rating = (Rating)dataSet.get(position);
        holder.ratingBar.setRating(rating.getStars());
        holder.textView.setText("\""+rating.getComment()+"\"");
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    class RatingViewHolder extends RecyclerView.ViewHolder{
        RatingBar ratingBar;
        TextView textView;

        public RatingViewHolder(@NonNull View convertView) {
            super(convertView);
            ratingBar = convertView.findViewById(R.id.ratingBarRating);
            textView = convertView.findViewById(R.id.textViewRating);

        }

    }
}


