package de.uni_ulm.uberuniulm.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationRequest;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.permission.AndroidPermissionChecker;
import com.tomtom.online.sdk.location.LocationSource;
import com.tomtom.online.sdk.location.LocationSourceFactory;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderFullAddress;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse;

import org.joda.time.Hours;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.uni_ulm.uberuniulm.MainPage;
import de.uni_ulm.uberuniulm.MapActivity;
import de.uni_ulm.uberuniulm.R;
import de.uni_ulm.uberuniulm.model.OfferedRide;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NewOfferHeaderFragment extends Fragment {
    public View fragmentView;
    private EditText startTextField, goalTextField, dateTextField, timeTextField, placesTextField, priceTextField;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;
    private ImageButton confirmBttn, closeBttn;
    private MapActivity mapActivity;
    private AutoCompleteTextView atvDepartureLocation;
    private AutoCompleteTextView atvDestinationLocation;
    private AutoCompleteTextView atvWaypointLocation;
    private List<String> searchAutocompleteList;
    private LocationSource locationSource;
    private Runnable searchRunnable;

    private SearchApi searchApi;
    private static final int SEARCH_FUZZY_LVL_MIN = 2;
    private ArrayAdapter<String> searchAdapter;
    private LatLng latLngCurrentPosition, latLngDeparture, latLngDestination;
    private static final int AUTOCOMPLETE_SEARCH_DELAY_MILLIS = 600;
    private static final int AUTOCOMPLETE_SEARCH_THRESHOLD = 2;
    private static final LatLng DEFAULT_DEPARTURE_LATLNG = new LatLng(48.418618, 9.942304);
    private static final LatLng DEFAULT_DESTINATION_LATLNG = new LatLng(48.426393, 9.960506);
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    private Boolean notInitiated=true;
    private SimpleDateFormat timeFormatter, monthFormatter;
    private Calendar cal;

    private final Handler searchTimerHandler = new Handler();

    private Map<String, LatLng> searchResultsMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_new_ride_header, container, false);
        mapActivity = (MapActivity) getActivity();

        startTextField =(EditText)  fragmentView.findViewById(R.id.newOfferActivityStartEditText);
        goalTextField = (EditText) fragmentView.findViewById(R.id.newOfferActivityDestinationEditText);
        dateTextField = (EditText) fragmentView.findViewById(R.id.newOfferActivityDateTextField);
        timeTextField = (EditText) fragmentView.findViewById(R.id.newOfferActivityTimeTextField);
        placesTextField = (EditText) fragmentView.findViewById(R.id.newOfferActivityPlacesTextField);
        priceTextField =(EditText)  fragmentView.findViewById(R.id.newOfferActivityPriceTextField);

        confirmBttn = fragmentView.findViewById(R.id.newOfferActivityConfirmBttn);
        closeBttn = fragmentView.findViewById(R.id.newOfferActivityCancelBttn);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        monthFormatter = new SimpleDateFormat("dd/MM/yyyy");
        dateTextField.setText(monthFormatter.format(cal.getTime()));
        timeFormatter = new SimpleDateFormat("HH:mm");
        timeTextField.setText(timeFormatter.format(cal.getTime()));

        Button btnRouteShow = fragmentView.findViewById(R.id.newOfferActivityGoBttn);
        btnRouteShow.setOnClickListener(v -> {
            mapActivity.drawRoute(latLngDeparture, latLngDestination);
        });

        dateTextField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = dayOfMonth + "/" + month + "/" + year;
                        cal.set(year, month, dayOfMonth);
                        dateTextField.setText(monthFormatter.format(cal.getTime()));
                    }
                };

                DatePickerDialog dialog = new DatePickerDialog(
                        mapActivity, R.style.spinnerDatePickerStyle,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(mapActivity.getColor(R.color.colorSlightlyTransparentBlack)));
                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });



        timeTextField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (view.isShown()) {
                            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            cal.set(Calendar.MINUTE, minute);
                            String time = hourOfDay + ":" + minute;
                            timeTextField.setText(time);
                        }
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(mapActivity, R.style.timePickerStyle, mTimeSetListener, hour, minute, true);
                timePickerDialog.setTitle("Choose departure:");
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(mapActivity.getColor(R.color.colorSlightlyTransparentBlack)));
                timePickerDialog.show();
                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        confirmBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int price = Integer.parseInt(priceTextField.getText().toString());

                if (price < 0) {
                    price = 0;
                }

                int places = (Integer.parseInt(placesTextField.getText().toString()));
                if(places<0) {
                    places = 0;
                }

                String date= dateTextField.getText().toString();
                String time= timeTextField.getText().toString();

                try {
                    Date today=Calendar.getInstance().getTime();
                    Date dateParsed=(monthFormatter.parse(date));
                    Date timeParsed=(timeFormatter.parse(time));
                    cal.setTime(dateParsed);
                    cal.set(Calendar.HOUR_OF_DAY, timeParsed.getHours());
                    cal.set(Calendar.MINUTE, timeParsed.getMinutes());
                    if(cal.getTime().before(today)){
                        Toast.makeText(mapActivity, getResources().getString(R.string.newOffer_date_inthepast_error), Toast.LENGTH_SHORT).show();
                    }else{
                        String departure = startTextField.getText().toString();
                        String destination = goalTextField.getText().toString();
                        mapActivity.onNewOfferActivityConfirmBttn(price, date, time, places, departure, destination);
                    }

                } catch (ParseException e) {
                    Toast.makeText(mapActivity, getResources().getString(R.string.newOffer_date_invalid_error), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                }

        });

        closeBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapActivity.closeMapView();
            }
        });

        TextView newWayPointButton= fragmentView.findViewById(R.id.newOfferWaypointEditText);
        newWayPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapActivity.addWaypoint();
            }
        });

        if(notInitiated) {
            initSearchFieldsWithDefaultValues();
            notInitiated=false;
        }

        return fragmentView;
    }


    public void setUpExistingOffer(OfferedRide ride){
        placesTextField = (EditText) fragmentView.findViewById(R.id.newOfferActivityPlacesTextField);
        priceTextField = (EditText) fragmentView.findViewById(R.id.newOfferActivityPriceTextField);
        dateTextField.setText(ride.getDate());
        timeTextField.setText(ride.getTime());
        startTextField.setText(ride.getDeparture());
        goalTextField.setText(ride.getDestination());
        String price= String.valueOf(ride.getPrice());
        String places= String.valueOf(ride.getPlaces());

        priceTextField.setText(price, TextView.BufferType.EDITABLE);
        placesTextField.setText(places, TextView.BufferType.EDITABLE);
    }

    private void initSearchFieldsWithDefaultValues() {
        atvDepartureLocation = fragmentView.findViewById(R.id.newOfferActivityStartEditText);
        atvDestinationLocation = fragmentView.findViewById(R.id.newOfferActivityDestinationEditText);

        initLocationSource();
        initWhereSection();
        initDepartureWithDefaultValue();
        initDestinationWithDefaultValue();
    }


    private void initDepartureWithDefaultValue() {
        latLngDeparture = DEFAULT_DEPARTURE_LATLNG;
        mapActivity.setAddressForLocation(latLngDeparture, atvDepartureLocation);
    }

    private void initDestinationWithDefaultValue() {
        latLngDestination = DEFAULT_DESTINATION_LATLNG;
        mapActivity.setAddressForLocation(latLngDestination, atvDestinationLocation);
    }

    private void initWhereSection() {
        searchAutocompleteList = new ArrayList<>();
        searchResultsMap = new HashMap<>();
        searchAdapter = new ArrayAdapter<String>(mapActivity, android.R.layout.simple_dropdown_item_1line, searchAutocompleteList);

        setTextWatcherToAutoCompleteField(atvDepartureLocation);
        setTextWatcherToAutoCompleteField(atvDestinationLocation);
    }

    private void initLocationSource() {
        AndroidPermissionChecker permissionChecker = AndroidPermissionChecker.createLocationChecker(mapActivity);
        if(permissionChecker.ifNotAllPermissionGranted()) {
            ActivityCompat.requestPermissions(mapActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
        LocationSourceFactory locationSourceFactory = new LocationSourceFactory();
        locationSource = locationSourceFactory.createDefaultLocationSource(mapActivity, mapActivity,  LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(2000)
                .setInterval(5000));
    }


    public abstract static class BaseTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    }

    public void setTextWatcherToAutoCompleteField(final AutoCompleteTextView autoCompleteTextView) {
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
            }else if(autoCompleteTextView == atvWaypointLocation){
                mapActivity.setWayPointPosition(searchResultsMap.get(item));
            }
            mapActivity.hideKeyboard(view);
        });
    }

    public void deactivateLocationSource(){
        locationSource.deactivate();
    }

    public void setWayPointTextField(AutoCompleteTextView wayPointTextField){
        atvWaypointLocation=wayPointTextField;
    }



    private void searchAddress(final String searchWord, final AutoCompleteTextView autoCompleteTextView) {
        mapActivity.getSearchApi().search(new FuzzySearchQueryBuilder(searchWord)
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
                        Toast.makeText(mapActivity, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
