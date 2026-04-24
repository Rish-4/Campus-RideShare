package com.rishabh.gride;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.appbar.MaterialToolbar;
import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import android.os.AsyncTask;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.maps.android.PolyUtil;


/**
 * HomeActivity
 * ------------
 * Main screen of the Campus Ride app.
 * Handles:
 * - Google Map display
 * - Pickup & drop selection
 * - Campus validation
 * - Fare calculation
 * - Zoom to current user location
 */
public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Google Map instance
    private GoogleMap mMap;

    // polyline for route
    private Polyline routePolyline;

    // Stores selected pickup & drop coordinates
    private LatLng pickupLatLng = null;
    private LatLng dropLatLng = null;

    // Markers for pickup & drop
    private Marker pickupMarker, dropMarker;

    /* ---------------- CAMPUS CONFIG ---------------- */

    // Campus center coordinates (replace with actual campus)
    private static final double CAMPUS_LAT = 30.27265885537235;
    private static final double CAMPUS_LNG = 78.00105675642672;

    // Campus radius in meters (geofence)
    private static final float CAMPUS_RADIUS = 1000; // 1 km

    /* ---------------- FARE CONFIG ---------------- */

    private static final int BASE_FARE = 20;
    private static final int PER_KM_RATE = 10;

    /* ---------------- LOCATION ---------------- */

    // Fused location provider for current location
    private FusedLocationProviderClient fusedLocationClient;

    // Permission request code
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI
        // UI elements
        Button btnBookRide = findViewById(R.id.btnBookRide);

        // Load Google Map fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Handle "Book Ride" button click
        btnBookRide.setOnClickListener(v -> handleBookRide());

        // Trigger search when user taps on pickup field (quick manual trigger)
        etPickup.setOnClickListener(v -> {
            searchLocation(etPickup.getText().toString(), true);
        });

        // Trigger search when user presses the "Search" button on keyboard (IME action)
        // This makes the input behave like a real search bar
        etPickup.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(etPickup.getText().toString(), true);
                return true;
            }
            return false;
        });

        // Same keyboard search functionality for drop location
        etDrop.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(etDrop.getText().toString(), false);
                return true;
            }
            return false;
        });

        // Trigger search when pickup field loses focus
        // (fallback mechanism in case user doesn’t press search button)
        etPickup.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchLocation(etPickup.getText().toString(), true);
            }
        });
        
        // Same fallback mechanism for drop field
        etDrop.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchLocation(etDrop.getText().toString(), false);
            }
        });
    }

    /**
     * Called when Google Map is ready to use
     */


    @Override
     public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Enable blue dot for user's current location
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Move camera to user's current location
        moveToCurrentLocation();

        // Handle pickup & drop selection via map taps
        mMap.setOnMapClickListener(latLng -> {

            if (pickupLatLng == null) {
                // First tap → pickup location
                pickupLatLng = latLng;
                pickupMarker = mMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title("Pickup Location")
                );
            }
            else if (dropLatLng == null) {
                // Second tap → drop location
                dropLatLng = latLng;
                dropMarker = mMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title("Drop Location")
                );
                // DRAW ROUTE AFTER BOTH POINTS ARE SET
                drawRouteFromBackend(pickupLatLng, dropLatLng);
            }

            else {
                // Third tap → reset and select new pickup
                mMap.clear();
                pickupLatLng = latLng;
                dropLatLng = null;

                pickupMarker = mMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title("Pickup Location")
                );
            }
        });
    }

    private void drawRouteFromBackend(LatLng pickup, LatLng drop) {
        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null) return;

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getRoute(
                token,
                pickup.latitude,
                pickup.longitude,
                drop.latitude,
                drop.longitude
        ).enqueue(new retrofit2.Callback<>() {

            @Override
            public void onResponse(
                    retrofit2.Call<java.util.Map<String, Object>> call,
                    retrofit2.Response<java.util.Map<String, Object>> response) {

                Log.d("ROUTE_DEBUG", "Backend response received: " + response.body());
                if (!response.isSuccessful() || response.body() == null) return;

                // ✅ Get encoded polyline string from backend
                String encodedPolyline =
                        response.body().get("polyline").toString();

                // ✅ Decode polyline into LatLng points
                List<LatLng> points =
                        PolyUtil.decode(encodedPolyline);

                // Remove old route if exists
                if (routePolyline != null) {
                    routePolyline.remove();
                }

                // Draw new blue route
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
                    retrofit2.Call<java.util.Map<String, Object>> call,
                    Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Zooms the map to user's location when permission granted for 1st time
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Permission granted → now move map to user's location
                moveToCurrentLocation();

                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // Permission denied → inform user
                Toast.makeText(this,
                        "Location permission is required to show your position",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Zooms the map to user's last known location
     */
    private void moveToCurrentLocation() {

        // Check runtime permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            // Request permission if not granted
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }

        // Fetch last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null && mMap != null) {
                        LatLng currentLatLng = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        // Move camera to current location
                        mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                        currentLatLng,
                                        16f // street-level zoom
                                )
                        );
                    }
                });
    }

    /**
     * Handles booking logic when user clicks "Book Ride"
     */
    private void handleBookRide() {

        // Ensure pickup & drop are selected
        if (pickupLatLng == null || dropLatLng == null) {
            toast("Select pickup and drop locations");
            return;
        }

        // Check campus rule
        boolean pickupInside = isInsideCampus(pickupLatLng);
        boolean dropInside = isInsideCampus(dropLatLng);

        if (!(pickupInside || dropInside)) {
            toast("Either pickup or drop must be inside campus");
            return;
        }

        // Calculate distance & fare
        float distanceKm = calculateDistanceKm(pickupLatLng, dropLatLng);
        int fare = calculateFare(distanceKm);

        // Show estimated fare
        Intent intent = new Intent(this, RideConfirmActivity.class);
        intent.putExtra("pickup", pickupLatLng.latitude + ", " + pickupLatLng.longitude);
        intent.putExtra("drop", dropLatLng.latitude + ", " + dropLatLng.longitude);
        intent.putExtra("distance", distanceKm);
        intent.putExtra("fare", fare);
        startActivity(intent);

    }

    /**
     * Checks whether a location lies inside campus radius
     */
    private boolean isInsideCampus(LatLng location) {
        float[] result = new float[1];

        Location.distanceBetween(
                location.latitude,
                location.longitude,
                CAMPUS_LAT,
                CAMPUS_LNG,
                result
        );

        return result[0] <= CAMPUS_RADIUS;
    }

    /**
     * Calculates distance between pickup & drop in kilometers
     */
    private float calculateDistanceKm(LatLng start, LatLng end) {
        float[] result = new float[1];

        Location.distanceBetween(
                start.latitude,
                start.longitude,
                end.latitude,
                end.longitude,
                result
        );

        return result[0] / 1000; // meters → km
    }

    /**
     * Calculates fare based on distance
     */
    private int calculateFare(float distanceKm) {
        return BASE_FARE + Math.round(distanceKm * PER_KM_RATE);
    }

    /**
     * Utility method to show Toast messages
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_history) {
            startActivity(
                    new Intent(this, RideHistoryActivity.class)
            );
            return true;
        }

        if (id == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }

        if (id == R.id.menu_logout) {

            // Clear token
            getSharedPreferences("AUTH", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            // Go back to login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class FetchRouteTask extends AsyncTask<String, Void, List<LatLng>> {

        @Override
        protected List<LatLng> doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                Log.d("ROUTE_JSON", sb.toString());

                JSONObject json = new JSONObject(sb.toString());
                JSONArray routes = json.getJSONArray("routes");

                if (routes.length() == 0) return null;

                JSONObject overviewPolyline =
                        routes.getJSONObject(0)
                                .getJSONObject("overview_polyline");

                String encoded = overviewPolyline.getString("points");
                return decodePolyline(encoded);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<LatLng> points) {
            if (points != null && mMap != null) {
                mMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .width(10f)
                        .color(Color.BLUE));
            }
        }
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return poly;
    }

    private void searchLocation(String locationName, boolean isPickup) {
        if (locationName.isEmpty()) {
            toast("Enter location");
            return;
        }

        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(locationName, 1);

            if (addressList != null && !addressList.isEmpty()) {

                Address address = addressList.get(0);
                LatLng latLng = new LatLng(
                        address.getLatitude(),
                        address.getLongitude()
                );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));

                if (isPickup) {
                    pickupLatLng = latLng;

                    if (pickupMarker != null) pickupMarker.remove();

                    pickupMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .title("Pickup Location")
                    );

                } else {
                    dropLatLng = latLng;

                    if (dropMarker != null) dropMarker.remove();

                    dropMarker = mMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .title("Drop Location")
                    );

                    // 🔥 draw route when both selected
                    if (pickupLatLng != null) {
                        drawRouteFromBackend(pickupLatLng, dropLatLng);
                    }
                }

            } else {
                toast("Location not found");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
