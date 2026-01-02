package com.rishabh.gride;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RideStatusActivity extends AppCompatActivity {

    TextView tvStatus;
    Button btnStartRide, btnEndRide;
    Button btnComplete, btnCancel;
    private int rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_status);

        rideId = getIntent().getIntExtra("rideId", -1);

        tvStatus = findViewById(R.id.tvStatus);
        btnStartRide = findViewById(R.id.btnStartRide);
        btnEndRide = findViewById(R.id.btnEndRide);

        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);

        btnComplete.setOnClickListener(v -> updateRide("COMPLETED"));
        btnCancel.setOnClickListener(v -> updateRide("CANCELLED"));

        btnStartRide.setOnClickListener(v -> {
            tvStatus.setText("Ride In Progress");
            btnStartRide.setEnabled(false);
            btnEndRide.setEnabled(true);

            Toast.makeText(this, "Ride started", Toast.LENGTH_SHORT).show();
        });

        btnEndRide.setOnClickListener(v -> {
            tvStatus.setText("Ride Completed");
            btnEndRide.setEnabled(false);

            Toast.makeText(this, "Ride completed successfully", Toast.LENGTH_LONG).show();
        });
    }

    private void updateRide(String status) {

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null || rideId == -1) {
            Toast.makeText(this,
                    "Invalid ride",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService =
                ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("status", status);

        apiService.updateRideStatus(token, rideId, body)
                .enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(RideStatusActivity.this,
                                    "Ride " + status,
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RideStatusActivity.this,
                                    "Failed to update status",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        Toast.makeText(RideStatusActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
