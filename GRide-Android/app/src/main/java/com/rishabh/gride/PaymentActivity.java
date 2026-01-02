package com.rishabh.gride;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
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


/**
 * PaymentActivity
 * ----------------
 * Handles payment selection for a ride.
 * Supported methods:
 * - UPI
 * - Wallet
 * - Cash
 *
 * This is UI + logic only (no real payment gateway yet).
 */
public class PaymentActivity extends AppCompatActivity {

    private TextView tvFare;
    private RadioGroup rgPayment;
    private Button btnPay;

    int rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize UI elements
        tvFare = findViewById(R.id.tvFare);
        rgPayment = findViewById(R.id.rgPayment);
        btnPay = findViewById(R.id.btnPay);

        // Get fare value from previous screen
        int fare = getIntent().getIntExtra("fare", 0);
        tvFare.setText("Fare: ₹" + fare);
        rideId = getIntent().getIntExtra("rideId", -1);

        // Handle Pay button click
        btnPay.setOnClickListener(v -> handlePayment(fare));
    }

    /**
     * Handles payment validation and navigation
     */
    private void handlePayment(int fare) {

        // Check if any payment option is selected
        int selectedId = rgPayment.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this,
                    "Please select a payment method",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Identify selected payment method
        String paymentMethod;

        if (selectedId == R.id.rbUPI) {
            paymentMethod = "UPI";
        } else if (selectedId == R.id.rbWallet) {
            paymentMethod = "Wallet";
        } else {
            paymentMethod = "Cash";
        }

        // Temporary success message
        if (paymentMethod.equals("UPI")) {
            payUsingUPI(
                    String.valueOf(fare),
                    "rishabhatt.sombhatt@okaxis",     // dummy UPI ID
                    "GRide"
            );
            return;
        }

        markRideAsPaid(rideId);
        // Navigate to Ride Status screen
        Intent intent = new Intent(this, RideStatusActivity.class);
        intent.putExtra("rideId", rideId);   //  REQUIRED
        startActivity(intent);


        // Optional: prevent user from going back to payment screen
        finish();
    }

    private void payUsingUPI(String amount, String upiId, String name) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)      // UPI ID
                .appendQueryParameter("pn", name)       // Payee name
                .appendQueryParameter("tn", "Campus Ride Payment")
                .appendQueryParameter("am", amount)     // Amount
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiIntent = new Intent(Intent.ACTION_VIEW);
        upiIntent.setData(uri);

        Intent chooser = Intent.createChooser(upiIntent, "Pay with UPI");

        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, 101);
        } else {
            Toast.makeText(this,
                    "No UPI app found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == RESULT_OK || resultCode == 11) {

                if (data != null) {
                    String response = data.getStringExtra("response");

                    if (response != null && response.toLowerCase().contains("success")) {
                        Toast.makeText(this,
                                "Payment Successful",
                                Toast.LENGTH_LONG).show();

                        markRideAsPaid(rideId);
                        Intent intent = new Intent(this, RideStatusActivity.class);
                        intent.putExtra("rideId", rideId);   //  REQUIRED
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Payment Failed or Cancelled",
                                Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this,
                        "Payment Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void markRideAsPaid(int rideId) {

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        if (token == null || rideId == -1) {
            Toast.makeText(this,
                    "Unable to update ride status",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("status", "PAID");

        api.updateRideStatus(token, rideId, body)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> response) {
                        // No UI needed here
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call,
                                          Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

}
