package com.rishabh.gride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    EditText etRollNumber, etPassword;
    Button btnLogin;
    TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etRollNumber = findViewById(R.id.etRollNumber);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void loginUser() {

        String rollNumber = etRollNumber.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (rollNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter roll number and password", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("roll_number", rollNumber); // 🔥 important
        body.put("password", password);

        api.login(body).enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call,
                                   Response<Map<String, Object>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoginActivity.this,
                            "Invalid roll number or password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> body = response.body();

                String token = body.get("token").toString();
                Map<String, Object> user = (Map<String, Object>) body.get("user");

                String name = user.get("name").toString();
                String email = user.get("email").toString();
                String role = user.get("role").toString();

                // Save session
                getSharedPreferences("AUTH", MODE_PRIVATE)
                        .edit()
                        .putString("TOKEN", token)
                        .putString("NAME", name)
                        .putString("EMAIL", email)
                        .putString("ROLE", role)
                        .apply();

                Toast.makeText(LoginActivity.this,
                        "Login successful",
                        Toast.LENGTH_SHORT).show();

                // Redirect
                if (role.equalsIgnoreCase("DRIVER")) {
                    startActivity(new Intent(LoginActivity.this,
                            DriverHomeActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this,
                            HomeActivity.class));
                }

                finish();
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Server error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}