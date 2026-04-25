package com.rishabh.gride;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompletionActivity extends AppCompatActivity {

    int rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completion);

        rideId = getIntent().getIntExtra("rideId", -1);

        Button btnSubmit = findViewById(R.id.btnSubmitReview);
        Button btnHome = findViewById(R.id.btnGoHome);

        btnSubmit.setOnClickListener(v -> submitReview());

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void submitReview() {

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText etReview = findViewById(R.id.etReview);

        int rating = (int) ratingBar.getRating();
        String review = etReview.getText().toString();

        Map<String, Object> body = new HashMap<>();
        body.put("rating", rating);
        body.put("review", review);

        api.submitReview("Bearer " + token, rideId, body)
                .enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> response) {

                        if (response.isSuccessful()) {
                            Toast.makeText(CompletionActivity.this,
                                    "Thanks for your feedback!",
                                    Toast.LENGTH_SHORT).show();

                            // Go to Home
                            startActivity(new Intent(CompletionActivity.this, HomeActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        Toast.makeText(CompletionActivity.this,
                                "Error submitting review",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}