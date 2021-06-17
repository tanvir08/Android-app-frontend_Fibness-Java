package com.pes.fibness;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class MapEditActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private DirectionsRoute currentRoute;
    private String rTitle;
    private String rDescription;
    private int rDistance;
    private int rPosition;
    private int rID;
    private boolean newRoute;
    private Button orgEditBtn;
    private Button destEditBtn;

    // variables for adding location layer
    private MapboxMap mapboxMap;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    Boolean editOrigin = false;
    Boolean editDestination = false;
    Point originPoint = null;
    Point destinationPoint = null;
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapBox_ACCESS_TOKEN));

        setContentView(R.layout.activity_edit_map_route);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        getExtras();
        if (!newRoute) addCameraPosition();


        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                mapboxMap.addOnMapClickListener(MapEditActivity.this);


                Button save = findViewById(R.id.save);
                orgEditBtn = findViewById(R.id.orig_point_btn);
                destEditBtn = findViewById(R.id.dest_point_btn);
                orgEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        orgEditBtn.setBackgroundResource(R.drawable.btn_bg_sel);
                        destEditBtn.setBackgroundResource(R.drawable.btn_bg);
                        editOrigin = true;
                        editDestination = false;
                    }
                });

                destEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        destEditBtn.setBackgroundResource(R.drawable.btn_bg_sel);
                        orgEditBtn.setBackgroundResource(R.drawable.btn_bg);
                        editOrigin = false;
                        editDestination = true;
                    }
                });

                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarM);
                toolbar.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        showEditBox();
                    }
                });

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(originPoint != null && destinationPoint != null) {
                            Ruta r = new Ruta();
                            r.name = rTitle;
                            r.description = rDescription;
                            r.distance = rDistance;
                            r.origen = originPoint;
                            r.destino = destinationPoint;
                            if (newRoute) {
                                r.id = -1;
                                User.getInstance().addRuta(r);

                                ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/route");
                                c.postUserRoute(r, User.getInstance().getId());
                            } else {
                                r.id = rID;
                                User.getInstance().updateRuta(rPosition, r);

                                ConnetionAPI c = new ConnetionAPI(getApplicationContext(), "http://10.4.41.146:3001/route/" + rID);
                                c.updateUserRoute(r);
                            }
                            finish();
                        }
                        if (originPoint == null && destinationPoint == null) Toast.makeText(getApplicationContext(), "Route points haven't been selected", Toast.LENGTH_LONG).show();
                        else if (originPoint == null) Toast.makeText(getApplicationContext(), "Origin point hasn't been selected", Toast.LENGTH_LONG).show();
                        else if (destinationPoint == null) Toast.makeText(getApplicationContext(), "Destination point hasn't been selected", Toast.LENGTH_LONG).show();
                    }
                });

                enableLocationComponent(style);

                addDestinationIconSymbolLayer(style);

                FloatingActionButton location = findViewById(R.id.set_location_tracked);
                location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        locationComponent.setCameraMode(CameraMode.TRACKING_GPS);
                    }
                });

                if (!newRoute) getRoute(originPoint, destinationPoint);

            }
        });
    }

    private void addCameraPosition() {
        com.google.android.gms.maps.model.LatLng center = LatLngBounds.builder()
                .include(new com.google.android.gms.maps.model.LatLng(originPoint.latitude(), originPoint.longitude()))
                .include(new com.google.android.gms.maps.model.LatLng(destinationPoint.latitude(), destinationPoint.longitude()))
                .build()
                .getCenter();

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(center.latitude, center.longitude))
                .zoom(14)
                .build();

        mapboxMap.setCameraPosition(position);
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

    @SuppressWarnings( {"MissingPermission"})

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(getString(R.string.mapBox_ACCESS_TOKEN))
                .origin(origin)
                .destination(destination)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Timber.d("Response code: %s", response.code());
                        if (response.body() == null) {
                            Timber.e("No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Timber.e("No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                        rDistance = currentRoute.distance().intValue();
                        TextView dist = findViewById(R.id.map_dist);
                        dist.setText("Distancia: " + rDistance + " m");

                    }

                    @Override
                    public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
                        Timber.e("Error: %s", throwable.getMessage());
                    }
                });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            if (newRoute)locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        //Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(Objects.requireNonNull(mapboxMap.getStyle()));
        } else {
            //Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            originPoint = (Point) extras.get("originPoint");
            destinationPoint = (Point)extras.get("destinationPoint");
            newRoute = extras.getBoolean("new");
            rTitle = extras.getString("routeTitle");
            rDescription = extras.getString("routeDescription");
            rPosition = extras.getInt("routePosition");
            rID = extras.getInt("routeID");
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarM);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(rTitle);
            getSupportActionBar().setSubtitle(rDescription);
        }

    }

    private void showEditBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.input_new_ruta);
        builder.setTitle("Edit " + rTitle);
        final AlertDialog dialog = builder.create();
        dialog.show();
        TextView txt = (TextView) dialog.findViewById(R.id.inputboxTitleRuta);
        txt.setText("Add a name");
        final EditText nameText = (EditText) dialog.findViewById(R.id.titleRutaInput);
        final EditText descText = (EditText) dialog.findViewById(R.id.descRutaInput);
        nameText.setText(rTitle);
        descText.setText(rDescription);
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> rutasList = User.getInstance().getRutasNames();
                if (nameText.getText().toString().trim().length() == 0) {
                    nameText.setError("Please, add a name");
                }
                else if (rutasList.contains(nameText.getText().toString()) &&
                        !rutasList.get(rPosition).equals(nameText.getText().toString())){
                    nameText.setError("This name is already used");
                }
                else {
                    rTitle = nameText.getText().toString();
                    rDescription = descText.getText().toString();
                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarM);
                    setSupportActionBar(toolbar);
                    getSupportActionBar().setTitle(rTitle);
                    getSupportActionBar().setSubtitle(rDescription);
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        if (editOrigin) {
            originPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
            if (source != null) {
                source.setGeoJson(Feature.fromGeometry(originPoint));
            }
        }
        else if (editDestination) {
            destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
            if (source != null) {
                source.setGeoJson(Feature.fromGeometry(destinationPoint));
            }
        }

        if (originPoint != null && destinationPoint != null)getRoute(originPoint, destinationPoint);
        return true;
    }
}
