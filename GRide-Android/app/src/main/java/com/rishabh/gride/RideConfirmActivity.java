package com.rishabh.gride;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import androidx.appcompat.app.AppCompatActivity;

public class RideConfirmActivity extends AppCompatActivity {

    TextView tvPickup, tvDrop, tvDistance, tvFare;
    Button btnConfirm, btnCancel;
    // Parsed coordinates (used in multiple places)
    double pickupLat, pickupLng, dropLat, dropLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_confirm);

        tvPickup = findViewById(R.id.tvPickup);
        tvDrop = findViewById(R.id.tvDrop);
        tvDistance = findViewById(R.id.tvDistance);
        tvFare = findViewById(R.id.tvFare);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);

        // Get data from HomeActivity
        String pickup = getIntent().getStringExtra("pickup");
        String drop = getIntent().getStringExtra("drop");
        float distance = getIntent().getFloatExtra("distance", 0);
        int fare = getIntent().getIntExtra("fare", 0);

        // -------- PARSE COORDINATES --------
        String[] pickupParts = pickup.split(",");
        String[] dropParts = drop.split(",");

        pickupLat = Double.parseDouble(pickupParts[0].trim());
        pickupLng = Double.parseDouble(pickupParts[1].trim());
        dropLat = Double.parseDouble(dropParts[0].trim());
        dropLng = Double.parseDouble(dropParts[1].trim());

        // -------- CONVERT TO READABLE ADDRESSES --------
        String pickupAddress = getAddressFromLatLng(pickupLat, pickupLng);
        String dropAddress = getAddressFromLatLng(dropLat, dropLng);

        // Set data to UI
        tvPickup.setText("Pickup: " + pickupAddress);
        tvDrop.setText("Drop: " + dropAddress);
        tvDistance.setText("Distance: " + String.format("%.2f", distance) + " km");
        tvFare.setText("Fare: ₹" + fare);

        btnConfirm.setOnClickListener(v -> {

            // Get JWT token
            String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                    .getString("TOKEN", null);

            if (token == null) {
                Toast.makeText(this,
                        "Please login first",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService =
                    ApiClient.getClient().create(ApiService.class);

            Map<String, Object> body = new HashMap<>();
            body.put("pickup_lat", pickupLat);
            body.put("pickup_lng", pickupLng);
            body.put("pickup_address", pickupAddress);

            body.put("drop_lat", dropLat);
            body.put("drop_lng", dropLng);
            body.put("drop_address", dropAddress);

            body.put("distance_km", distance);
            body.put("fare", fare);


            apiService.createRide(token, body)
                    .enqueue(new Callback<Map<String, Object>>() {

                        @Override
                        public void onResponse(Call<Map<String, Object>> call,
                                               Response<Map<String, Object>> response) {

                            if (response.isSuccessful() && response.body() != null) {

                                //  EXTRACT rideId from backend response
                                int rideId = ((Double) response.body().get("rideId")).intValue();

                                Toast.makeText(RideConfirmActivity.this,
                                        "Ride booked successfully",
                                        Toast.LENGTH_SHORT).show();

                                //  PASS rideId to PaymentActivity
                                Intent intent = new Intent(
                                        RideConfirmActivity.this,
                                        PaymentActivity.class
                                );
                                intent.putExtra("fare", fare);
                                intent.putExtra("rideId", rideId);
                                startActivity(intent);

                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call,
                                              Throwable t) {
                            Toast.makeText(RideConfirmActivity.this,
                                    t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private String getAddressFromLatLng(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0); // full readable address
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown location";
    }

}
