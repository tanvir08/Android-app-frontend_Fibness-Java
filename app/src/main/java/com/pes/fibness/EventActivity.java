package com.pes.fibness;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import java.util.Arrays;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class EventActivity extends AppCompatActivity implements OnMapReadyCallback {

    int id;
    String title;
    String desc;
    String date;
    String hour;
    Point place;
    String site;
    Boolean comunity;
    Boolean participa = false;
    int pos;

    Button delete;
    Button edit;
    Button join;
    FloatingActionButton participantes;

    private MapView mapView;
    private MapboxMap mapboxMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapBox_ACCESS_TOKEN));
        setContentView(R.layout.activity_view_event);
        getExtras();

        delete = findViewById(R.id.btn_delete_event);
        edit = findViewById(R.id.btn_edit_event);
        join = findViewById(R.id.btn_join_event);
        participantes = findViewById(R.id.fb_participantes);
        mapView = findViewById(R.id.mapEvent);

        ((TextView) findViewById(R.id.titleEvent)).setText(title);
        ((TextView) findViewById(R.id.descEvent)).setText(desc);
        ((TextView) findViewById(R.id.dateEvent)).setText(date + "  " + hour);
        new Handler().postDelayed(new Runnable(){
            public void run(){
                if(comunity){
                    delete.setVisibility(View.INVISIBLE);
                    delete.setClickable(false);
                    edit.setVisibility(View.INVISIBLE);
                    edit.setClickable(false);
                    join.setVisibility(View.VISIBLE);
                    join.setClickable(true);
                    participa = User.getInstance().participa();
                    if(participa){
                        join.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                        join.setText(R.string.Leave);
                    }
                    else{
                        join.setBackground(getResources().getDrawable(R.drawable.btn_bg_sel));
                        join.setText(R.string.Join);
                    }
                }
                else{
                    delete.setVisibility(View.VISIBLE);
                    delete.setClickable(true);
                    edit.setVisibility(View.VISIBLE);
                    edit.setClickable(true);
                    join.setVisibility(View.INVISIBLE);
                    join.setClickable(false);
                }
            }
        }, 100);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/event/" + id);
                connection.deleteEvent();
                User.getInstance().deleteEvent(pos);
                finish();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent modify_event = new Intent(EventActivity.this, CreateEventActivity.class);
                modify_event.putExtra("new", false);
                modify_event.putExtra("id", id);
                modify_event.putExtra("title", title);
                modify_event.putExtra("desc", desc);
                modify_event.putExtra("date", date);
                modify_event.putExtra("hour", hour);
                modify_event.putExtra("place", place);
                modify_event.putExtra("position", pos);
                startActivity(modify_event);
                finish();
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(participa){
                    ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/event/" + id + "/join/" + User.getInstance().getId());
                    connection.deleteParticipa();
                    User.getInstance().deleteParticipa();
                    join.setBackground(getResources().getDrawable(R.drawable.btn_bg_sel));
                    join.setText(R.string.Join);
                    participa = false;
                }
                else{
                    ConnetionAPI connection = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/event/" + id + "/join");
                    connection.createParticipa(User.getInstance().getId());
                    User.getInstance().addParticipa();
                    join.setBackground(getResources().getDrawable(R.drawable.btn_bg));
                    join.setText(R.string.Leave);
                    participa = true;
                    AddCalendarEvent();
                }
            }
        });

        participantes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent participants = new Intent(EventActivity.this, ParticipantsActivity.class);
                startActivity(participants);
            }
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
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

    public void AddCalendarEvent() {
        reverseGeocode(place);
        @SuppressLint("HandlerLeak") Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String[] hora = hour.split(":");
                String[] fecha = date.split("/");

                Calendar calendarEvent = Calendar.getInstance();
                calendarEvent.set(Integer.parseInt(fecha[2]), Integer.parseInt(fecha[1])-1, Integer.parseInt(fecha[0]), Integer.parseInt(hora[0]), Integer.parseInt(hora[1]));
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

    private void addCameraPosition() {
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(place.latitude(), place.longitude()))
                        .zoom(15)
                        .build()), 4000);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        addCameraPosition();

        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                addDestinationIconSymbolLayer(style);
                GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                source.setGeoJson(Feature.fromGeometry(place));
            }
        });
    }

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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        title = extras.getString("title");
        desc = extras.getString("desc");
        date = extras.getString("date");
        hour = extras.getString("hour");
        place = (Point) extras.get("place");
        id = extras.getInt("id");
        pos = extras.getInt("position");
        comunity = extras.getBoolean("comunity");
    }


    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {

        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

}
