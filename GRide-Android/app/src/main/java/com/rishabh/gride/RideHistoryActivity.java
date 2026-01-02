package com.rishabh.gride;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rishabh.gride.adapters.RideAdapter;
import com.rishabh.gride.models.Ride;
import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        RecyclerView rv = findViewById(R.id.rvRides);
        rv.setLayoutManager(new LinearLayoutManager(this));

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getMyRides(token).enqueue(new Callback<List<Ride>>() {
            @Override
            public void onResponse(Call<List<Ride>> call, Response<List<Ride>> res) {
                if (res.isSuccessful() && res.body() != null) {
                    rv.setAdapter(new RideAdapter(res.body()));
                } else {
                    Toast.makeText(RideHistoryActivity.this,
                            "Failed to load rides", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Ride>> call, Throwable t) {
                Toast.makeText(RideHistoryActivity.this,
                        t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
