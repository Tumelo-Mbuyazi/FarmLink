package com.example.farmlink.activities.farmer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.Auth.LoginActivity;
import com.example.farmlink.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FarmerProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvFarmLocation;
    private TextView tvStatProducts, tvStatOrders, tvStatRating;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmer_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadProfileData();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvFarmLocation = findViewById(R.id.tvFarmLocation);
        tvStatProducts = findViewById(R.id.tvStatProducts);
        tvStatOrders = findViewById(R.id.tvStatOrders);
        tvStatRating = findViewById(R.id.tvStatRating);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadProfileData() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        tvProfileName.setText(document.getString("name"));
                        tvProfileEmail.setText(document.getString("email"));
                        String location = document.getString("location");
                        if (location != null && !location.isEmpty()) {
                            tvFarmLocation.setText("📍 " + location);
                        }
                    }
                });
        
        // Mock stats - in a real app these would be fetched from Firestore
        tvStatProducts.setText("12");
        tvStatOrders.setText("8");
        tvStatRating.setText("4.8");
    }

    private void setupClickListeners() {
        findViewById(R.id.layoutEditProfile).setOnClickListener(v -> 
            Toast.makeText(this, "Edit Profile Coming Soon", Toast.LENGTH_SHORT).show());
        
        findViewById(R.id.layoutChangePassword).setOnClickListener(v -> 
            Toast.makeText(this, "Change Password Coming Soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, FarmerDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_products) {
                startActivity(new Intent(this, FarmerProductsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, FarmerOrdersActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}