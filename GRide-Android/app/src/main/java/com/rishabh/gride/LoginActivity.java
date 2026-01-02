package com.rishabh.gride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rishabh.gride.network.ApiClient;
import com.rishabh.gride.network.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }

            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            Map<String, String> body = new HashMap<>();
            body.put("email", email);       // from EditText
            body.put("password", password); // from EditText

            apiService.login(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call,
                                       Response<Map<String, Object>> response) {

                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(LoginActivity.this,
                                "Login failed",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> body = response.body();

                    // 1️⃣ Get token safely
                    Object tokenObj = body.get("token");
                    if (tokenObj == null) {
                        Toast.makeText(LoginActivity.this,
                                "Invalid login response",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String token = tokenObj.toString();

                    // 2️⃣ Get user safely
                    Object userObj = body.get("user");
                    if (!(userObj instanceof Map)) {
                        Toast.makeText(LoginActivity.this,
                                "User data missing",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> user = (Map<String, Object>) userObj;

                    String name = user.get("name") != null ? user.get("name").toString() : "";
                    String email = user.get("email") != null ? user.get("email").toString() : "";
                    String role = user.get("role") != null ? user.get("role").toString() : "USER";

                    // 3️⃣ Save session safely
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

                    Intent intent;
                    if (role.equalsIgnoreCase("DRIVER")) {
                        intent = new Intent(LoginActivity.this, DriverHomeActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, HomeActivity.class);
                    }

                    startActivity(intent);
                    finish();

                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(LoginActivity.this,
                            t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
