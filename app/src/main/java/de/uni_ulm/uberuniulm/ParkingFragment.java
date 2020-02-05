package de.uni_ulm.uberuniulm;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;

import java.util.ArrayList;

import de.uni_ulm.uberuniulm.model.ParkingSpot;
import de.uni_ulm.uberuniulm.model.ParkingSpots;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ParkingFragment extends Fragment implements OnMapReadyCallback, TomtomMapCallback.OnMapLongClickListener {
    public View fragmentView;
    private TomtomMap tomtomMap;
    private SearchApi searchApi;
    private Fragment mapFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        fragmentView = inflater.inflate(R.layout.content_parking_page, container, false);


        LinearLayout mapFragment=(LinearLayout) fragmentView.findViewById(R.id.parkingFragmentMapContainer);
        mapFragment.setVisibility(View.VISIBLE);
        initTomTomServices();
        initUIViews();
        setupUIViewListeners();

        return fragmentView;
    }



    @Override
    public void onMapReady(@NonNull TomtomMap map) {
        ParkingSpots parkings=new ParkingSpots();
        tomtomMap = map;
        tomtomMap.setMyLocationEnabled(true);
        tomtomMap.addOnMapLongClickListener(this);
        tomtomMap.getMarkerSettings().setMarkersClustering(true);
        tomtomMap.removeMarkers();
        tomtomMap.getMarkerSettings().setMarkerBalloonViewAdapter(new TypedBallonViewAdapter());
        ArrayList<ParkingSpot> parkingSpots=parkings.getAll();
        for(ParkingSpot spot: parkingSpots){
            SimpleMarkerBalloon balloon = new SimpleMarkerBalloon(spot.getName());
            MarkerBuilder markerBuilder = new MarkerBuilder(spot.getPosition())
                    .markerBalloon(balloon);

            Marker m = tomtomMap.addMarker(markerBuilder);
        }
        tomtomMap.getUiSettings().setCameraPosition(
                CameraPosition
                        .builder(parkings.getSpotByName("P26").getPosition())
                        .zoom(14)
                        .build()
        );
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {}

    private void initTomTomServices() {
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
        searchApi = OnlineSearchApi.create(this.getContext());
    }

    private void initUIViews() {}

    private void setupUIViewListeners() {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void handleApiError(Throwable e) {
        Toast.makeText(getActivity(), getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
    }

}
