package de.uni_ulm.uberuniulm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.permission.AndroidPermissionChecker;
import com.tomtom.online.sdk.location.LocationSource;
import com.tomtom.online.sdk.location.LocationSourceFactory;
import com.tomtom.online.sdk.location.LocationUpdateListener;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderFullAddress;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.uni_ulm.uberuniulm.model.ParkingSpots;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NewOfferPage extends AppCompatActivity implements LocationUpdateListener, NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, TomtomMapCallback.OnMapLongClickListener {
    private TomtomMap tomtomMap;
    private SearchApi searchApi;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng latLngCurrentPosition;
    private LatLng latLngDeparture;
    private LatLng latLngDestination;
    private AutoCompleteTextView atvDepartureLocation;
    private AutoCompleteTextView atvDestinationLocation;
    private final Handler searchTimerHandler = new Handler();
    private LocationSource locationSource;

    private static final int AUTOCOMPLETE_SEARCH_DELAY_MILLIS = 600;
    private static final int AUTOCOMPLETE_SEARCH_THRESHOLD = 3;
    private static final LatLng DEFAULT_DEPARTURE_LATLNG = new LatLng(52.376368, 4.908113);
    private static final LatLng DEFAULT_DESTINATION_LATLNG = new LatLng(52.3076865, 4.767424099999971);
    private static final int PERMISSION_REQUEST_LOCATION = 0;

    private static final int SEARCH_FUZZY_LVL_MIN = 2;

    private ArrayAdapter<String> searchAdapter;
    private List<String> searchAutocompleteList;
    private Map<String, LatLng> searchResultsMap;
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_new_ride);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LinearLayout mapFragment=(LinearLayout) findViewById(R.id.newOfferActivityMapContainer);
        mapFragment.setVisibility(View.VISIBLE);
        searchApi=OnlineSearchApi.create(this);

        initTomTomServices();
        initSearchFieldsWithDefaultValues();
        initWhereSection();
        initTomTomServices();
        initUIViews();
        setupUIViewListeners();
    }

    public void onNewOfferActivityCancelBttn(View view){
        Intent intent = new Intent(NewOfferPage.this, MainPage.class);
        startActivity(intent);
    }

    public void onNewOfferActivityConfirmBttn(View view){
        Intent intent = new Intent(NewOfferPage.this, MainPage.class);
        startActivity(intent);
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

        LatLng currentPosition=getCurrentPosition();
        tomtomMap.getUiSettings().setCameraPosition(
                CameraPosition
                        .builder(currentPosition)
                        .zoom(14)
                        .build()
        );
        SimpleMarkerBalloon balloon = new SimpleMarkerBalloon("Current Position");
        MarkerBuilder markerBuilder = new MarkerBuilder(currentPosition)
                .markerBalloon(balloon);

        Marker m = tomtomMap.addMarker(markerBuilder);

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {}

    private void initTomTomServices() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
        searchApi = OnlineSearchApi.create(this);
    }

    private void initUIViews() {}

    private void setupUIViewListeners() {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void handleApiError(Throwable e) {
        Toast.makeText(NewOfferPage.this, getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private LatLng getCurrentPosition(){
        ParkingSpots parkings=new ParkingSpots();
        Location currentUserLocation=tomtomMap.getUserLocation();
        if(currentUserLocation!=null) {
            return new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getAltitude());
        }else{
            return parkings.getSpotByName("P26").getPosition();
        }
    }

    private void initSearchFieldsWithDefaultValues() {
        atvDepartureLocation = findViewById(R.id.newOfferActivityStartEditText);
        atvDestinationLocation = findViewById(R.id.newOfferActivityDestinationEditText);
        initLocationSource();
        initDepartureWithDefaultValue();
        initDestinationWithDefaultValue();
    }

    private void initLocationSource() {
        AndroidPermissionChecker permissionChecker = AndroidPermissionChecker.createLocationChecker(this);
        if(permissionChecker.ifNotAllPermissionGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
        LocationSourceFactory locationSourceFactory = new LocationSourceFactory();
        locationSource = locationSourceFactory.createDefaultLocationSource(this, this,  LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(2000)
                .setInterval(5000));
    }

    private void initDepartureWithDefaultValue() {
        latLngDeparture = DEFAULT_DEPARTURE_LATLNG;
        setAddressForLocation(latLngDeparture, atvDepartureLocation);
    }

    private void initDestinationWithDefaultValue() {
        latLngDestination = DEFAULT_DESTINATION_LATLNG;
        setAddressForLocation(latLngDestination, atvDestinationLocation);
    }

    private void setAddressForLocation(LatLng location, final AutoCompleteTextView autoCompleteTextView) {
        searchApi.reverseGeocoding(new ReverseGeocoderSearchQueryBuilder(location.getLatitude(), location.getLongitude()).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<ReverseGeocoderSearchResponse>() {
                    @Override
                    public void onSuccess(ReverseGeocoderSearchResponse reverseGeocoderSearchResponse) {
                        List addressesList = reverseGeocoderSearchResponse.getAddresses();
                        if (!addressesList.isEmpty()) {
                            String address = ((ReverseGeocoderFullAddress) addressesList.get(0)).getAddress().getFreeformAddress();
                            autoCompleteTextView.setText(address);
                            autoCompleteTextView.dismissDropDown();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(NewOfferPage.this, "Error setting address", Toast.LENGTH_LONG).show();
                        Log.e("newOfferActivity", "Error setting address", e);
                    }
                });
    }

    private void searchAddress(final String searchWord, final AutoCompleteTextView autoCompleteTextView) {
        searchApi.search(new FuzzySearchQueryBuilder(searchWord)
                .withLanguage(Locale.getDefault().toLanguageTag())
                .withTypeAhead(true)
                .withMinFuzzyLevel(SEARCH_FUZZY_LVL_MIN).build())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<FuzzySearchResponse>() {
                    @Override
                    public void onSuccess(FuzzySearchResponse fuzzySearchResponse) {
                        if (!fuzzySearchResponse.getResults().isEmpty()) {
                            searchAutocompleteList.clear();
                            searchResultsMap.clear();
                            if (autoCompleteTextView == atvDepartureLocation && latLngCurrentPosition != null) {
                                String currentLocationTitle = "Current position";
                                searchAutocompleteList.add(currentLocationTitle);
                                searchResultsMap.put(currentLocationTitle, latLngCurrentPosition);
                            }
                            for (FuzzySearchResult result : fuzzySearchResponse.getResults()) {
                                String addressString = result.getAddress().getFreeformAddress();
                                searchAutocompleteList.add(addressString);
                                searchResultsMap.put(addressString, result.getPosition());
                            }
                            searchAdapter.clear();
                            searchAdapter.addAll(searchAutocompleteList);
                            searchAdapter.getFilter().filter("");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(NewOfferPage.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initWhereSection() {
        searchAutocompleteList = new ArrayList<>();
        searchResultsMap = new HashMap<>();
        searchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, searchAutocompleteList);

        setTextWatcherToAutoCompleteField(atvDepartureLocation);
        setTextWatcherToAutoCompleteField(atvDestinationLocation);
    }

    private void setTextWatcherToAutoCompleteField(final AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setAdapter(searchAdapter);
        autoCompleteTextView.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTimerHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0) {
                    if (s.length() >= AUTOCOMPLETE_SEARCH_THRESHOLD) {
                        searchRunnable = () -> searchAddress(s.toString(), autoCompleteTextView);
                        searchAdapter.clear();
                        searchTimerHandler.postDelayed(searchRunnable, AUTOCOMPLETE_SEARCH_DELAY_MILLIS);
                    }
                }
            }
        });
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);
            if (autoCompleteTextView == atvDepartureLocation) {
                latLngDeparture = searchResultsMap.get(item);
            } else if (autoCompleteTextView == atvDestinationLocation) {
                latLngDestination = searchResultsMap.get(item);
            }
            hideKeyboard(view);
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (in != null) {
            in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (latLngCurrentPosition == null) {
            latLngCurrentPosition = new LatLng(location);
            locationSource.deactivate();
        }
    }

    private abstract class BaseTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    }
}