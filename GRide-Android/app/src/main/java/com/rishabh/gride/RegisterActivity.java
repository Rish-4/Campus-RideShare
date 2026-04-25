package com.rishabh.gride;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etRollNumber, etEmail, etPassword;
    Button btnRegister;
    TextView tvLogin;

    RadioButton rbUser, rbDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Bind views
        etName = findViewById(R.id.etName);
        etRollNumber = findViewById(R.id.etRollNumber); // 🔥 new
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        rbUser = findViewById(R.id.radioUser);
        rbDriver = findViewById(R.id.radioDriver);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String rollNumber = etRollNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Role selection
        String role;
        if (rbDriver.isChecked()) {
            role = "DRIVER";
        } else {
            role = "USER";
        }

        // Validation
        if (name.isEmpty() || rollNumber.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("roll_number", rollNumber); // 🔥 important
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);

        api.register(body).enqueue(new Callback<Map<String, String>>() {

            @Override
            public void onResponse(Call<Map<String, String>> call,
                                   Response<Map<String, String>> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful",
                            Toast.LENGTH_SHORT).show();

                    finish(); // back to login
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Email or Roll Number already registered",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Server error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}