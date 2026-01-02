package com.rishabh.gride;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvRole;
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        btnLogout = findViewById(R.id.btnLogout);

        // Read saved user data
        String name = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("NAME", "N/A");
        String email = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("EMAIL", "N/A");
        String role = getSharedPreferences("AUTH", MODE_PRIVATE)
                .getString("ROLE", "USER");

        tvName.setText("Name: " + name);
        tvEmail.setText("Email: " + email);
        tvRole.setText("Role: " + role);

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        getSharedPreferences("AUTH", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
