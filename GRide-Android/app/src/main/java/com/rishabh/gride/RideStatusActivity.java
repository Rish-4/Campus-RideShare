package com.rishabh.gride;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideStatusActivity extends AppCompatActivity {

    private TextView tvStatus, tvDriverName, tvVehicle, tvPhone, tvDriverRating;
    private Button btnCallDriver, btnCancelRide;

    private int fare;
    private int rideId;
    private String driverPhone = "";

    private android.os.Handler handler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_status);

        rideId = getIntent().getIntExtra("rideId", -1);

        // Get fare
        fare = getIntent().getIntExtra("fare", 0);

        // Bind UI
        tvStatus = findViewById(R.id.tvStatus);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvVehicle = findViewById(R.id.tvVehicle);
        tvPhone = findViewById(R.id.tvPhone);
        tvDriverRating = findViewById(R.id.tvDriverRating);
        btnCallDriver = findViewById(R.id.btnCallDriver);
        btnCancelRide = findViewById(R.id.btnCancelRide);

        // Call driver
        btnCallDriver.setOnClickListener(v -> {
            if (!driverPhone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + driverPhone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Phone not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel ride
        btnCancelRide.setOnClickListener(v -> updateRideStatus("CANCELLED"));

        startPolling();
    }

    // 🔄 Poll backend every 3 sec
    private void startPolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchRideStatus();
                handler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    // 🌐 Fetch ride status
    private void fetchRideStatus() {

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getRideStatus(token, rideId)
                .enqueue(new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            Map<String, Object> data = response.body();

                            String status = data.get("status").toString();

                            // Driver details
                            String name = String.valueOf(data.get("driver_name"));
                            String phone = String.valueOf(data.get("driver_phone"));
                            String vehicle = data.get("vehicle_name") + " - " +
                                    data.get("vehicle_number");

                            driverPhone = phone;

                            tvDriverName.setText("Name: " + name);
                            tvPhone.setText("Phone: " + phone);
                            tvVehicle.setText("Vehicle: " + vehicle);

                            updateUI(status);
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

    // 🎨 Update UI
    private void updateUI(String status) {

        switch (status) {

            case "ACCEPTED":
                tvStatus.setText("Driver is on the way");
                btnCancelRide.setVisibility(View.VISIBLE);
                btnCallDriver.setVisibility(View.VISIBLE);
                break;

            case "ARRIVED":
                tvStatus.setText("Driver has arrived");
                btnCancelRide.setVisibility(View.VISIBLE);
                btnCallDriver.setVisibility(View.VISIBLE);
                break;

            case "STARTED":
                tvStatus.setText("Ride in progress");
                btnCancelRide.setVisibility(View.GONE);
                btnCallDriver.setVisibility(View.GONE);
                break;

            case "ENDED":
                tvStatus.setText("Ride ended");

                // 🔥 Go to payment
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra("rideId", rideId);
                intent.putExtra("fare", fare);
                startActivity(intent);
                finish();
                break;

            case "COMPLETED":
                tvStatus.setText("Ride completed");
                btnCancelRide.setVisibility(View.GONE);
                btnCallDriver.setVisibility(View.GONE);
                break;

            /*case "COMPLETED":
                tvStatus.setText("Ride completed");

                btnCancelRide.setVisibility(View.GONE);
                btnCallDriver.setVisibility(View.GONE);

                // Optional: redirect to home (safety)
                Intent homeIntent = new Intent(this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
                break;*/

            case "CANCELLED":
                tvStatus.setText("Ride cancelled");
                btnCancelRide.setVisibility(View.GONE);
                btnCallDriver.setVisibility(View.GONE);
                break;

                /*case "COMPLETED":
                tvStatus.setText("Ride completed");

                btnCancelRide.setVisibility(View.GONE);
                btnCallDriver.setVisibility(View.GONE);

                // Optional: redirect to home (safety)
                Intent homeIntent = new Intent(this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
                break;*/
        }
    }

    // ❌ Cancel ride
    private void updateRideStatus(String newStatus) {

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new java.util.HashMap<>();
        body.put("status", newStatus);

        api.updateRideStatus(token, rideId, body)
                .enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(RideStatusActivity.this,
                                    "Ride cancelled",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        Toast.makeText(RideStatusActivity.this,
                                "Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}