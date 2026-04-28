package com.rishabh.gride;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rishabh.gride.adapters.RideAdapter;
import com.rishabh.gride.models.Ride;
import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.media.Ringtone;
import android.media.RingtoneManager;

import android.net.Uri;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class DriverHomeActivity extends AppCompatActivity {

    private RecyclerView rv;
    private int lastRideCount = 0;

    //  Polling handler
    private Handler handler = new Handler();

    //  Prevent duplicate API calls
    private boolean isFetching = false;

    //  Polling task
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            fetchRides();
            handler.postDelayed(this, 3000); // every 3 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        requestLocationPermission();

        rv = findViewById(R.id.rvRides);
        rv.setLayoutManager(new LinearLayoutManager(this));

        //  Start polling
        handler.post(pollingRunnable);
    }

    private void fetchRides() {

        //  Avoid overlapping calls
        if (isFetching) return;
        isFetching = true;

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            isFetching = false;
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Log.d("DRIVER", "Fetching rides...");

        api.getAvailableRides("Bearer " + token)
                .enqueue(new Callback<List<Ride>>() {

                    @Override
                    public void onResponse(Call<List<Ride>> call,
                                           Response<List<Ride>> response) {

                        isFetching = false; // reset flag

                        if (response.isSuccessful() && response.body() != null) {

                            List<Ride> rides = response.body();
                            if (rides.size() > lastRideCount) {

                                // 🔥 Ignore first load
                                if (lastRideCount != 0) {

                                    Toast.makeText(DriverHomeActivity.this,
                                            "New Ride Available!",
                                            Toast.LENGTH_LONG).show();

                                    playNotificationSound();
                                }
                            }

                            lastRideCount = rides.size();

                            Log.d("DRIVER", "Rides count: " + rides.size());

                            if (rides.isEmpty()) {
                                // Optional: don't spam toast every 3 sec
                                Log.d("DRIVER", "No rides available");
                            }

                            rv.setAdapter(new RideAdapter(rides,
                                    ride -> acceptRide(ride)));

                        } else {
                            Toast.makeText(DriverHomeActivity.this,
                                    "Failed to load rides",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Ride>> call, Throwable t) {

                        isFetching = false; //  reset flag

                        Log.e("DRIVER", "Error: " + t.getMessage());

                        Toast.makeText(DriverHomeActivity.this,
                                "Error fetching rides",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void acceptRide(Ride ride) {

        int rideId = ride.id;
        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.acceptRide("Bearer " + token, rideId)
                .enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> response) {

                        Log.d("DRIVER", "Accept response code: " + response.code());

                        if (response.isSuccessful()) {

                            Log.d("DRIVER", "Accept success hit");

                            Toast.makeText(DriverHomeActivity.this,
                                    "Ride accepted",
                                    Toast.LENGTH_SHORT).show();

                            //  Move to DriverRideActivity
                            Intent intent = new Intent(DriverHomeActivity.this,
                                    DriverRideActivity.class);
                            intent.putExtra("rideId", rideId);
                            intent.putExtra("pickupLat", ride.pickup_lat);
                            intent.putExtra("pickupLng", ride.pickup_lng);
                            startActivity(intent);

                        } else {
                            Toast.makeText(DriverHomeActivity.this,
                                    "Accept failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call,
                                          Throwable t) {

                        Log.e("DRIVER", "Accept error: " + t.getMessage());

                        Toast.makeText(DriverHomeActivity.this,
                                "Error accepting ride",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //  Stop polling
    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(pollingRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.post(pollingRunnable);
    }


    private void playNotificationSound() {

        try {

            Uri notification = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Ringtone ringtone = RingtoneManager
                    .getRingtone(getApplicationContext(), notification);

            ringtone.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void requestLocationPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    101
            );
        }
    }
}