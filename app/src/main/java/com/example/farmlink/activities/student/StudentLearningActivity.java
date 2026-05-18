package com.example.farmlink.activities.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.R;
import com.example.farmlink.StudentAIActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.example.farmlink.StudentAIActivity;

public class StudentLearningActivity extends AppCompatActivity {

    private static final String TAG = "StudentLearning";

    // Views
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabAI;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_learning);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupFAB();
        setupClickListeners();
        setupBottomNavigation();

    }

    private void setupFAB() {
        if (fabAI != null) {
            fabAI.setOnClickListener(v -> {
                Toast.makeText(StudentLearningActivity.this, "Opening AI Assistant...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StudentLearningActivity.this, StudentAIActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "FAB is NULL! Check layout ID");
        }
    }

    private void initViews() {

        bottomNavigation = findViewById(R.id.bottomNavigation);
        fabAI = findViewById(R.id.fabAI);
    }

    private void setupClickListeners() {
        View category1 = findViewById(R.id.cardCategory1);
        if (category1 != null) {
            category1.setOnClickListener(v -> Log.d(TAG, "Category 1 clicked"));
        }
    }


    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;
        
        bottomNavigation.setSelectedItemId(R.id.nav_learning);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_learning) return true;
            
            Intent intent = null;
            if (itemId == R.id.nav_home) intent = new Intent(this, StudentDashboardActivity.class);
            else if (itemId == R.id.nav_marketplace) intent = new Intent(this, StudentMarketplaceActivity.class);
            else if (itemId == R.id.nav_connect) intent = new Intent(this, StudentConnectActivity.class);
            else if (itemId == R.id.nav_profile) intent = new Intent(this, StudentProfileActivity.class);
            // Handle AI Assistant navigation if the activity exists
            // else if (itemId == R.id.nav_ai) intent = new Intent(this, StudentAIActivity.class);

            if (intent != null) {
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        // Navigate back to Dashboard instead of closing app or crashing
        Intent intent = new Intent(this, StudentDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
