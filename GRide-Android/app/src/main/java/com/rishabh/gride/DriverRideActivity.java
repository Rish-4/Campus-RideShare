package com.rishabh.gride;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import com.google.maps.android.PolyUtil;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;
import android.net.Uri;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DriverRideActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 101;

    private GoogleMap mMap;

    private FloatingActionButton btnNavigate;
    private double pickupLat;
    private double pickupLng;

    private int rideId;

    private Button btnArrived;
    private TextView tvDistance;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private boolean firstLocationReceived = false;

    private Marker pickupMarker;

    private Polyline routePolyline;

    // 🔥 Prevent route spam
    private long lastRouteUpdate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_ride);

        // 🔥 Intent data
        rideId = getIntent().getIntExtra("rideId", -1);

        pickupLat = getIntent().getDoubleExtra("pickupLat", 0);
        pickupLng = getIntent().getDoubleExtra("pickupLng", 0);

        // 🔥 Views
        btnArrived = findViewById(R.id.btnArrived);
        tvDistance = findViewById(R.id.tvDistance);

        btnNavigate = findViewById(R.id.btnNavigate);


        btnArrived.setEnabled(false);

        // 🔥 Location client
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        // 🔥 Map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 🔥 Arrived click
        btnArrived.setOnClickListener(v -> markArrived());

        btnNavigate.setOnClickListener(v -> openNavigation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        // 🔥 Map settings
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // 🔥 Pickup marker
        LatLng pickupLocation =
                new LatLng(pickupLat, pickupLng);

        pickupMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(pickupLocation)
                        .title("Pickup")
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED
                        ))
        );

        // 🔥 Camera
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                        pickupLocation,
                        16f
                )
        );

        requestLocationPermission();
    }

    private void requestLocationPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE
            );

            return;
        }

        // 🔥 Blue dot + arrow
        mMap.setMyLocationEnabled(true);

        startLocationUpdates();
    }

    private void startLocationUpdates() {

        LocationRequest request = LocationRequest.create();

        request.setInterval(3000);
        request.setFastestInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult result) {

                if (result == null) return;

                for (Location location : result.getLocations()) {

                    updateDriverLocation(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void updateDriverLocation(Location driverLocation) {

        LatLng driverLatLng = new LatLng(
                driverLocation.getLatitude(),
                driverLocation.getLongitude()
        );

        // 🔥 FIRST TIME ONLY
        if (!firstLocationReceived) {

            firstLocationReceived = true;

            // 🔥 Draw initial route immediately
            drawRouteFromBackend(
                    driverLatLng,
                    new LatLng(pickupLat, pickupLng)
            );

            // 🔥 Show both points nicely
            showOverview(driverLatLng,
                    new LatLng(pickupLat, pickupLng));
        }

        // 🔥 Distance calculation
        float[] results = new float[1];

        Location.distanceBetween(
                driverLocation.getLatitude(),
                driverLocation.getLongitude(),
                pickupLat,
                pickupLng,
                results
        );

        float distanceMeters = results[0];

        float distanceKm = distanceMeters / 1000f;

        tvDistance.setText(
                String.format("%.1f km away", distanceKm)
        );

        // 🔥 Enable ARRIVED
        if (distanceMeters <= 60) {

            btnArrived.setEnabled(true);
            btnArrived.setText("ARRIVED");

        } else {

            btnArrived.setEnabled(false);
            btnArrived.setText("Reach pickup first");
        }

        // 🔥 Refresh route every 10 sec
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRouteUpdate > 10000) {

            lastRouteUpdate = currentTime;

            android.util.Log.d("PAKISTAN", "TRYING TO DRAW ROUTE");
            drawRouteFromBackend(
                    driverLatLng,
                    new LatLng(pickupLat, pickupLng)
            );
        }
    }

    private void drawRouteFromBackend(
            LatLng pickup,
            LatLng drop
    ) {

        android.util.Log.d("PAKISTAN", "drawRouteFromBackend CALLED");
        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null) return;

        ApiService api =
                ApiClient.getClient().create(ApiService.class);

        api.getRoute(
                "Bearer " + token,
                pickup.latitude,
                pickup.longitude,
                drop.latitude,
                drop.longitude
        ).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(
                    Call<Map<String, Object>> call,
                    Response<Map<String, Object>> response) {

                if (!response.isSuccessful()
                        || response.body() == null) {
                    return;
                }

                android.util.Log.d(
                        "PAKISTAN",
                        String.valueOf(response.body())
                );

                String encodedPolyline =
                        response.body()
                                .get("polyline")
                                .toString();

                List<LatLng> points =
                        PolyUtil.decode(encodedPolyline);

                // 🔥 Remove old route
                if (routePolyline != null) {
                    routePolyline.remove();
                }

                // 🔥 Draw route
                routePolyline = mMap.addPolyline(
                        new PolylineOptions()
                                .addAll(points)
                                .color(Color.parseColor("#1976D2"))
                                .width(12f)
                                .startCap(new RoundCap())
                                .endCap(new RoundCap())
                                .jointType(JointType.ROUND)
                );
            }

            @Override
            public void onFailure(
                    Call<Map<String, Object>> call,
                    Throwable t) {

                android.util.Log.e(
                        "PAKISTAN",
                        t.getMessage()
                );
            }
        });
    }

    private void markArrived() {

        String token = getSharedPreferences(
                "AUTH",
                MODE_PRIVATE
        ).getString("TOKEN", null);

        ApiService api =
                ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("status", "ARRIVED");

        api.updateRideStatus(
                        "Bearer " + token,
                        rideId,
                        body
                )
                .enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(
                            Call<Map<String, String>> call,
                            Response<Map<String, String>> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    DriverRideActivity.this,
                                    "Marked as arrived",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    DriverRideActivity.this,
                                    "Failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Map<String, String>> call,
                            Throwable t) {

                        Toast.makeText(
                                DriverRideActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (fusedLocationClient != null
                && locationCallback != null) {

            fusedLocationClient
                    .removeLocationUpdates(locationCallback);
        }
    }

    private void openNavigation() {

        Uri gmmIntentUri = Uri.parse(
                "google.navigation:q="
                        + pickupLat + ","
                        + pickupLng
        );

        Intent mapIntent =
                new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {

            startActivity(mapIntent);

        } else {

            Toast.makeText(
                    this,
                    "Google Maps not installed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showOverview(
            LatLng driver,
            LatLng pickup
    ) {

        com.google.android.gms.maps.model.LatLngBounds.Builder builder =
                new com.google.android.gms.maps.model.LatLngBounds.Builder();

        builder.include(driver);
        builder.include(pickup);

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        200
                )
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == LOCATION_PERMISSION_CODE) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                requestLocationPermission();
            }
        }
    }
}

