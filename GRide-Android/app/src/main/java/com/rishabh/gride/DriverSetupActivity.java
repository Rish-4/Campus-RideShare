package com.rishabh.gride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverSetupActivity extends AppCompatActivity {

    private EditText etVehicleName, etVehicleNumber;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_setup);

        etVehicleName = findViewById(R.id.etVehicleName);
        etVehicleNumber = findViewById(R.id.etVehicleNumber);
        btnSave = findViewById(R.id.btnSaveVehicle);

        btnSave.setOnClickListener(v -> saveDriverData());
    }

    private void saveDriverData() {

        String vehicleName = etVehicleName.getText().toString().trim();
        String vehicleNumber = etVehicleNumber.getText().toString().trim();

        if (vehicleName.isEmpty() || vehicleNumber.isEmpty()) {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("TOKEN", null);

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("vehicle_name", vehicleName);
        body.put("vehicle_number", vehicleNumber);

        api.createDriverProfile(token, body)
                .enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(Call<Map<String, String>> call,
                                           Response<Map<String, String>> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(DriverSetupActivity.this,
                                    "Profile saved", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(
                                    DriverSetupActivity.this,
                                    DriverHomeActivity.class
                            ));
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable t) {
                        Toast.makeText(DriverSetupActivity.this,
                                "Server error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}