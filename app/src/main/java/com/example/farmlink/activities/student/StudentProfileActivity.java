package com.example.farmlink.activities.student;


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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvMemberSince;
    private TextView tvCoursesEnrolled, tvCoursesCompleted, tvLearningHours, tvLearningStreak;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupBottomNavigation();
        loadUserProfile();
        loadLearningStats();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        findViewById(R.id.layoutEditProfile).setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layoutChangePassword).setOnClickListener(v -> {
            Toast.makeText(this, "Change Password - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layoutNotifications).setOnClickListener(v -> {
            Toast.makeText(this, "Notifications - Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvCoursesEnrolled = findViewById(R.id.tvCoursesEnrolled);
        tvCoursesCompleted = findViewById(R.id.tvCoursesCompleted);
        tvLearningHours = findViewById(R.id.tvLearningHours);
        tvLearningStreak = findViewById(R.id.tvLearningStreak);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        long createdAt = document.getLong("createdAt");

                        tvName.setText(name != null ? name : "Student");
                        tvEmail.setText(email);

                        if (createdAt > 0) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                            tvMemberSince.setText("Member since: " + sdf.format(new Date(createdAt)));
                        }
                    } else {
                        tvName.setText("Student");
                        tvEmail.setText(email);
                    }
                });
    }

    private void loadLearningStats() {
        String studentId = mAuth.getCurrentUser().getUid();

        db.collection("enrollments")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(query -> {
                    int enrolled = query.size();
                    int completed = 0;
                    int totalHours = 0;

                    for (var doc : query) {
                        if (doc.getBoolean("completed")) completed++;
                        totalHours += doc.getLong("hoursSpent") != null ? doc.getLong("hoursSpent").intValue() : 0;
                    }

                    tvCoursesEnrolled.setText(String.valueOf(enrolled));
                    tvCoursesCompleted.setText(String.valueOf(completed));
                    tvLearningHours.setText(String.valueOf(totalHours));
                });

        // Set learning streak (mock for now)
        tvLearningStreak.setText("🔥 Learning Streak: 5 days");
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, StudentDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_learning) {
                startActivity(new Intent(this, StudentLearningActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_marketplace) {
                startActivity(new Intent(this, StudentMarketplaceActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_connect) {
                startActivity(new Intent(this, StudentConnectActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}