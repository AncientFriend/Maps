package com.example.janis.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.Task;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int COLOR_BLACK_ARGB = 0xdd000000;
    final int REQUEST_CHECK_SETTINGS = 1;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    private List<List<LatLng>> tiles = new ArrayList<>();
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private GoogleMap mMap;
    private Logs logs;
    private FloatingActionButton fab1;
    private FusedLocationProviderClient mFusedLocationClient;
    private PolygonOptions plo;
    private Polygon polygon1 = null;
    private double maxLat = 85.1054596961173;

    private ArrayList<Location> locationArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        logs = new Logs(this);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        createLocationRequest();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    boolean minDistance = true;
                    int counter = 0;
                    for (Location loc : locationArray) {
                        logs.log("locationArray", loc.getLongitude(), loc.getLatitude() + counter++ + '\n');
                        if (!isMinDistance(loc, location)) {
                            minDistance = false;
                        }
                    }
                    if (minDistance) {
                        locationArray.add(location);
                        update();
                    }
                }
            }

            ;
        };

        fab1 = findViewById(R.id.fab_01);
        fab1.setOnClickListener(this::onClickFab);

    }

    public boolean isMinDistance(Location o, Location n) {
        return o.getLatitude() - n.getLatitude() >= 0.0005 + 0.00100005 ||
                o.getLatitude() + n.getLatitude() >= 0.0005 + 0.00100005 ||
                o.getLongitude() + n.getLongitude() >= 0.001 + 0.00100005 ||
                o.getLongitude() - n.getLongitude() >= 0.001 + 0.00100005;
    }

    public ArrayList<LatLng> getTile(Location location) {
        ArrayList<LatLng> tile = new ArrayList<>();
        double lat = calcCoord(location.getLatitude(), 0.001);
        double lng = calcCoord(location.getLongitude(), 0.002);
        double diff = 0.0000005 / 2;

        tile.add(new LatLng(lat + diff, lng + diff));
        tile.add(new LatLng(lat + 0.001 - diff, lng));
        tile.add(new LatLng(lat + 0.001 - diff, lng + 0.002 - diff));
        tile.add(new LatLng(lat, lng + 0.002 - diff));
        return tile;
    }

    public double calcCoord(double coordinate, double tileSize) {
        if(coordinate<0)
            return (((int) (coordinate / tileSize)) * tileSize)-tileSize;
        else
            return ((int) (coordinate / tileSize)) * tileSize;
    }

    public void update() {
        mMap.clear();

        plo = new PolygonOptions()
                .fillColor(COLOR_BLACK_ARGB)
                .add(new LatLng(maxLat, -180),
                        new LatLng(maxLat, -60),
                        new LatLng(maxLat, 0),
                        new LatLng(maxLat, 60),
                        new LatLng(maxLat, 179.9999999999999),
                        new LatLng(-maxLat, 179.9999999999999),
                        new LatLng(-maxLat, 60),
                        new LatLng(-maxLat, 0),
                        new LatLng(-maxLat, -60),
                        new LatLng(-maxLat, -180));

        for (int i = 0; i < locationArray.size(); i++) {
            ArrayList<LatLng> tile = getTile(locationArray.get(i));
            if(!tiles.contains(tile))
                tiles.add(tile);
        }

        for(List<LatLng> tile:tiles){
            plo.addHole(tile);
        }

        polygon1 = mMap.addPolygon(plo);
        polygon1.setTag("Overlay");
        polygon1.setStrokeColor(Color.TRANSPARENT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    public void onResume() {
        super.onResume();
        // put your code here...
        createLocationRequest();
        startLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    mMap.setMyLocationEnabled(true);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @SuppressLint("MissingPermission")
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            logs.log("task", "success");
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            MapsActivity.this.mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(MapsActivity.this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            boolean minDistance = true;
                            for (Location loc : locationArray) {
                                if (!isMinDistance(loc, location)) {
                                    minDistance = false;
                                }
                            }
                            if (minDistance) {
                                locationArray.add(location);
                            }
                            update();
                        }
                    });

        });

        task.addOnFailureListener(this, e -> {
            logs.log("task", "failure");
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    protected void onClickFab(View v) {
        update();
        ((FloatingActionButton) v).setSupportBackgroundTintList(ColorStateList.valueOf(changeHue(getColor(((FloatingActionButton) v)), 1)));
    }

    private int changeHue(@ColorInt int color, int rotate) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] += (hsv[0] + rotate) > 360 ? rotate - 360 : rotate;
        return Color.HSVToColor(hsv);
    }

    private int getColor(FloatingActionButton v) {
        return Objects.requireNonNull(v.getSupportBackgroundTintList()).getDefaultColor();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                setUp();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void setUp() {
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }
}
