package com.pes.fibness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class CreateEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    Boolean newEvent;
    String title;
    String desc;
    String date;
    String hora;
    Point place;
    String site;
    int pos;
    int id;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";

    /*                                        Date                                       */
    private static final String zero = "0";
    private static final String slash = "/";
    public final Calendar d = Calendar.getInstance();
    final int month = d.get(Calendar.MONTH);
    final int day = d.get(Calendar.DAY_OF_MONTH);
    final int year = d.get(Calendar.YEAR);

    /*                                        Hour                                       */
    private static final String two_points = ":";
    public final Calendar h = Calendar.getInstance();
    final int hour = h.get(Calendar.HOUR_OF_DAY);
    final int minute = h.get(Calendar.MINUTE);

    /*                                        Widgets                                     */
    EditText etHourPicker;
    EditText etDatePicker;
    EditText textTitle;
    EditText textDesc;
    FloatingActionButton save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapBox_ACCESS_TOKEN));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_create_event);

        etDatePicker = findViewById(R.id.et_date_picker);
        etHourPicker = findViewById(R.id.et_hour_picker);
        save = findViewById(R.id.fab_save_event);
        mapView = findViewById(R.id.mapView);
        textTitle = (EditText) findViewById(R.id.editText2);
        textDesc = (EditText) findViewById(R.id.editText4);

        getExtras();

        if(!newEvent){
            textTitle.setText(title);
            textDesc.setText(desc);
            etDatePicker.setText(date);
            etHourPicker.setText(hora);
        }

        etDatePicker.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                getDate();
            }
        });

        etHourPicker.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                getHour();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(compruebaDatos()) {
                    Evento e = new Evento();
                    e.name = title;
                    e.desc = desc;
                    e.date = date;
                    e.hour = hora;
                    e.place = place;
                    if (newEvent) {
                        e.id = 0;
                        int position = User.getInstance().addEvent(e);
                        ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/event");
                        connection.createEvent(e, User.getInstance().getId(), position);
                        addCalendarEvent();
                    } else {
                        e.id = id;
                        ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/event/" + id);
                        connection.updateEvent(e);
                        User.getInstance().updateEvent(pos, e);
                    }
                    finish();
                }
            }
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private boolean compruebaDatos() {

        if (textTitle.getText().toString().trim().length() == 0) {
            textTitle.setError(getString(R.string.PleaseAddAName));
            return false;
        }
        else if(hora == null){
            etHourPicker.setError(getString(R.string.AddAnHour));
            return false;
        }
        else if(date == null){
            etDatePicker.setError(getString(R.string.AddADate));
            return false;
        }
        else if(place == null){
            Toast.makeText(this, getString(R.string.AddAPlace), Toast.LENGTH_LONG).show();
            return false;
        }
        title = textTitle.getText().toString();
        desc = textDesc.getText().toString();
        return true;
    }

    private void getHour() {
        TimePickerDialog hourPicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String formatHour =  (hourOfDay < 10)? String.valueOf(zero + hourOfDay) : String.valueOf(hourOfDay);
                String formatMinute = (minute < 10)? String.valueOf(zero + minute):String.valueOf(minute);
                etHourPicker.setText(formatHour + two_points + formatMinute );
                hora = etHourPicker.getText().toString();
            }
        }, hour, minute, false);

        hourPicker.show();
    }

    private void getDate() {
        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final int mesActual = month + 1;
                String formatDay = (dayOfMonth < 10)? zero + dayOfMonth :String.valueOf(dayOfMonth);
                String formatMonth = (mesActual < 10)? zero + mesActual :String.valueOf(mesActual);
                etDatePicker.setText(formatDay + slash + formatMonth + slash + year);
                date = etDatePicker.getText().toString();
            }
        }, year, month, day);
        datePicker.show();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                initSearchFab();

                // Add the symbol layer icon to map for future use
                style.addImage(symbolIconId, BitmapFactory.decodeResource(
                        CreateEventActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));

                // Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);

                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);

                if (!newEvent) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromGeometry(place)}));
                    }

                    // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(place.latitude(), place.longitude()))
                                    .zoom(15)
                                    .build()), 4000);
                }
            }
        });
    }

    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapBox_ACCESS_TOKEN))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(CreateEventActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);


            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(15)
                                    .build()), 4000);

                    place = (Point) selectedCarmenFeature.geometry();
                }
            }
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    private void reverseGeocode(final Point point) {
        try {
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.mapBox_ACCESS_TOKEN))
                    .query(Point.fromLngLat(point.longitude(), point.latitude()))
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build();

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    if (response.body() != null) {
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {
                            CarmenFeature feature = results.get(0);
                            site =  feature.placeName();
                            //Toast.makeText( getBaseContext(), site, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Timber.e("Geocoding Failure: %s", throwable.getMessage());
                }
            });
        } catch (ServicesException servicesException) {
            Timber.e("Error geocoding: %s", servicesException.toString());
            servicesException.printStackTrace();
        }
    }

    public void addCalendarEvent() {
        reverseGeocode(place);
        @SuppressLint("HandlerLeak") Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String[] horas = hora.split(":");
                String[] fecha = date.split("/");

                android.icu.util.Calendar calendarEvent = android.icu.util.Calendar.getInstance();
                calendarEvent.set(Integer.parseInt(fecha[2]), Integer.parseInt(fecha[1])-1, Integer.parseInt(fecha[0]), Integer.parseInt(horas[0]), Integer.parseInt(horas[1]));
                Intent i = new Intent(Intent.ACTION_EDIT);
                i.setType("vnd.android.cursor.item/event");
                i.putExtra("beginTime", calendarEvent.getTimeInMillis());
                i.putExtra("allDay", false);
                i.putExtra("title", title);
                i.putExtra("description", desc);
                i.putExtra("eventLocation", site);

                startActivity(i);
            }
        };
        h.sendEmptyMessageDelayed(0, 300);
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        newEvent = extras.getBoolean("new");
        title = extras.getString("title");
        desc = extras.getString("desc");
        date = extras.getString("date");
        hora = extras.getString("hour");
        place = (Point) extras.get("place");
        id = extras.getInt("id");
        pos = extras.getInt("position");
    }

}

