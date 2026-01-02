package com.rishabh.gride;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AvailableRidesActivity extends AppCompatActivity {

    List<Map<String, Object>> rideList = new ArrayList<>();
    ListView listRides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_rides);

        listRides = findViewById(R.id.listRides);

        loadAvailableRides();
    }

    private void loadAvailableRides() {

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getAvailableRides(token)
                .enqueue(new Callback<List<Map<String, Object>>>() {

                    @Override
                    public void onResponse(
                            Call<List<Map<String, Object>>> call,
                            Response<List<Map<String, Object>>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(
                                    AvailableRidesActivity.this,
                                    "No rides found",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        rideList = response.body();
                        List<String> display = new ArrayList<>();

                        for (Map<String, Object> ride : rideList) {

                            String pickup = ride.get("pickup_address") != null
                                    ? ride.get("pickup_address").toString()
                                    : "Unknown pickup";

                            String drop = ride.get("drop_address") != null
                                    ? ride.get("drop_address").toString()
                                    : "Unknown drop";

                            String distance = ride.get("distance_km") != null
                                    ? ride.get("distance_km").toString()
                                    : "0";

                            String fare = ride.get("fare").toString();

                            display.add(
                                    "Pickup: " + pickup +
                                            "\nDrop: " + drop +
                                            "\nDistance: " + distance + " km" +
                                            "\nFare: ₹" + fare
                            );
                        }

                        listRides.setAdapter(
                                new ArrayAdapter<>(
                                        AvailableRidesActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        display
                                )
                        );
                        listRides.setOnItemClickListener((parent, view, position, id) -> {

                            int rideId = ((Double) rideList.get(position).get("id")).intValue();
                            acceptRide(rideId);
                        });
                    }

                    @Override
                    public void onFailure(
                            Call<List<Map<String, Object>>> call,
                            Throwable t) {

                        Toast.makeText(
                                AvailableRidesActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
    private void acceptRide(int rideId) {

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.acceptRide(token, rideId)
                .enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(
                            Call<Map<String, String>> call,
                            Response<Map<String, String>> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    AvailableRidesActivity.this,
                                    "Ride accepted",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadAvailableRides(); // refresh list
                        } else {
                            Toast.makeText(
                                    AvailableRidesActivity.this,
                                    "Unable to accept ride",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Map<String, String>> call,
                            Throwable t) {

                        Toast.makeText(
                                AvailableRidesActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
