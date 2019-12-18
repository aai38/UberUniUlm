package de.uni_ulm.uberuniulm;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tomtom.online.sdk.map.BaseBalloonViewAdapter;
import com.tomtom.online.sdk.map.BaseMarkerBalloon;
import com.tomtom.online.sdk.map.Marker;

import de.uni_ulm.uberuniulm.model.ParkingSpot;
import de.uni_ulm.uberuniulm.model.ParkingSpots;

public class TypedBallonViewAdapter extends BaseBalloonViewAdapter<BaseMarkerBalloon> {
    /**
     * Taking layout from balloon view model.
     * @param marker to be inflated.
     * @param markerBalloon
     * @return
     */
    @Override
    public int getLayout(Marker marker, BaseMarkerBalloon markerBalloon) {
        if (isTextBalloon(markerBalloon)){
            return R.layout.map_parking_marker_info;
        }else{
            return R.layout.map_parking_marker_info;
        }
    }

    private boolean isTextBalloon(BaseMarkerBalloon markerBalloon) {
        return markerBalloon.getProperty(BaseMarkerBalloon.KEY_TEXT) != null;
    }

    /**
     * Bind text view for single line balloon view.
     * @param view   the root view of inflating layout.
     * @param marker value which is used to fill layout.
     * @param markerBalloon balloon model.
     */
    @Override
    public void onBindView(View view, Marker marker, BaseMarkerBalloon markerBalloon) {
        if (isTextBalloon(markerBalloon)){
            ParkingSpot spot= new ParkingSpots().getSpotByName(markerBalloon.toString());
            TextView nameText = view.findViewById(R.id.mapMarkerSlotNameText);
            nameText.setText(spot.getName());
            TextView fillText = view.findViewById(R.id.mapMarkerFillInFoText);
            fillText.setText(spot.getFill()+" / "+spot.getCapacity());
            TextView warningText = view.findViewById(R.id.mapMarkerInfoWarningText);
            //textView.setText(spot.getName());
            ProgressBar progressbar = view.findViewById(R.id.mapMarkerInfoProgressBar);
            progressbar.setMax(100);
            progressbar.setProgress(spot.getFill()*100/spot.getCapacity());
        }
    }

}
