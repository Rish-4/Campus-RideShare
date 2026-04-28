package com.rishabh.gride;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.content.Intent;
import android.widget.Toast;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WaitingActivity extends AppCompatActivity {

    private int rideId;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        rideId = getIntent().getIntExtra("rideId", -1);

        startPolling();
    }
    private void checkRideStatus() {

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.getRideStatus("Bearer " + token, rideId).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    String status = response.body().get("status").toString();

                    if (status.equals("ACCEPTED")) {

                        handler.removeCallbacksAndMessages(null); // STOP polling

                        Toast.makeText(WaitingActivity.this,
                                "Driver found!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(
                                WaitingActivity.this,
                                RideStatusActivity.class
                        );

                        intent.putExtra("rideId", rideId);
                        intent.putExtra("fare", getIntent().getIntExtra("fare", 0));

                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void startPolling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkRideStatus();
                handler.postDelayed(this, 3000); // every 3 sec
            }
        }, 3000);
    }
}