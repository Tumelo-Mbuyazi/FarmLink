package com.example.farmlink.activities.student;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.farmlink.Auth.LoginActivity;
import com.example.farmlink.R;
import com.example.farmlink.StudentAIActivity;
import java.util.Calendar;

public class StudentDashboardActivity extends AppCompatActivity {

    private static final String TAG = "StudentDashboard";
    private static final String PREFS_NAME = "FarmLinkPrefs";

    // Views
    private TextView tvGreeting, tvUserName;
    private TextView tvPoints, tvLevel, tvStreak;
    private TextView tvEnrolledCourses, tvCompletedCourses, tvCertificatesCount;
    private BottomNavigationView bottomNavigation;
    private NestedScrollView nestedScrollView;
    private FloatingActionButton fabAI;  // ← Changed to regular FAB

    // Course Card Views
    private MaterialCardView courseCard1, courseCard2, courseCard3;
    private TextView tvCourse1Title, tvCourse1Progress, tvCourse2Title, tvCourse2Progress, tvCourse3Title, tvCourse3Progress;
    private ProgressBar progressBar1, progressBar2, progressBar3;
    private ImageView btnNotification;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Variables
    private String userId;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "========== ON CREATE ==========");
        setContentView(R.layout.student_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        // Initialize Views
        initViews();

        // Setup FAB - ONLY ONCE
        setupFAB();

        // Setup handlers
        setupClickListeners();
        setupBottomNavigation();

        // Load data
        loadUserFromFirestore();
        loadEnrolledCourses();
        loadStats();

        if (savedInstanceState != null) {
            restoreSavedState(savedInstanceState);
        }
    }

    private void setupFAB() {
        if (fabAI != null) {
            fabAI.setOnClickListener(v -> {
                Toast.makeText(StudentDashboardActivity.this, "Opening AI Assistant...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StudentDashboardActivity.this, StudentAIActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "FAB is NULL! Check layout ID");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            navigateToLogin();
        } else {
            refreshData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGreeting();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tvUserName != null) {
            outState.putString("user_name", tvUserName.getText().toString());
        }
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserName = findViewById(R.id.tvUserName);
        tvPoints = findViewById(R.id.tvPoints);
        tvLevel = findViewById(R.id.tvLevel);
        tvStreak = findViewById(R.id.tvStreak);
        tvEnrolledCourses = findViewById(R.id.tvEnrolledCourses);
        tvCompletedCourses = findViewById(R.id.tvCompletedCourses);
        tvCertificatesCount = findViewById(R.id.tvCertificatesCount);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        fabAI = findViewById(R.id.fabAI);  // This finds the FAB

        // Course Cards
        courseCard1 = findViewById(R.id.courseCard1);
        courseCard2 = findViewById(R.id.courseCard2);
        courseCard3 = findViewById(R.id.courseCard3);

        tvCourse1Title = findViewById(R.id.tvCourse1Title);
        tvCourse1Progress = findViewById(R.id.tvCourse1Progress);
        tvCourse2Title = findViewById(R.id.tvCourse2Title);
        tvCourse2Progress = findViewById(R.id.tvCourse2Progress);
        tvCourse3Title = findViewById(R.id.tvCourse3Title);
        tvCourse3Progress = findViewById(R.id.tvCourse3Progress);

        progressBar1 = findViewById(R.id.progressBar1);
        progressBar2 = findViewById(R.id.progressBar2);
        progressBar3 = findViewById(R.id.progressBar3);

        btnNotification = findViewById(R.id.btnNotification);
    }

    private void setupClickListeners() {
        if (courseCard1 != null) {
            courseCard1.setOnClickListener(v -> openCourse("course_maize"));
        }
        if (courseCard2 != null) {
            courseCard2.setOnClickListener(v -> openCourse("course_organic"));
        }
        if (courseCard3 != null) {
            courseCard3.setOnClickListener(v -> openCourse("course_soil"));
        }

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show());
        }

        TextView tvSeeAllCourses = findViewById(R.id.tvSeeAllCourses);
        if (tvSeeAllCourses != null) {
            tvSeeAllCourses.setOnClickListener(v -> {
                startActivity(new Intent(this, StudentLearningActivity.class));
            });
        }
    }

    private void openCourse(String courseId) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("course_id", courseId);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;

            Intent intent = null;
            if (itemId == R.id.nav_learning) intent = new Intent(this, StudentLearningActivity.class);
            else if (itemId == R.id.nav_marketplace) intent = new Intent(this, StudentMarketplaceActivity.class);
            else if (itemId == R.id.nav_connect) intent = new Intent(this, StudentConnectActivity.class);
            else if (itemId == R.id.nav_profile) intent = new Intent(this, StudentProfileActivity.class);

            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = (hour < 12) ? "Good morning" : (hour < 16) ? "Good afternoon" : "Good evening";
        if (tvGreeting != null) tvGreeting.setText(greeting);
    }

    private void loadUserFromFirestore() {
        if (userId == null) return;
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                if (name != null && tvUserName != null) tvUserName.setText(name);

                Long points = doc.getLong("learningPoints");
                if (points != null && tvPoints != null) tvPoints.setText(String.valueOf(points));

                Long streak = doc.getLong("learningStreak");
                if (streak != null && tvStreak != null) tvStreak.setText(String.format("%d days", streak));

                String level = doc.getString("level");
                if (level != null && tvLevel != null) tvLevel.setText(level);
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error loading user", e));
    }

    private void loadEnrolledCourses() {
        if (userId == null) return;
        db.collection("enrollments")
                .whereEqualTo("studentId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    if (tvEnrolledCourses != null) tvEnrolledCourses.setText(String.valueOf(query.size()));
                    int completed = 0;
                    for (QueryDocumentSnapshot doc : query) {
                        Boolean isComp = doc.getBoolean("completed");
                        if (isComp != null && isComp) completed++;
                    }
                    if (tvCompletedCourses != null) tvCompletedCourses.setText(String.valueOf(completed));
                });
    }

    private void loadStats() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (tvPoints != null && (tvPoints.getText().toString().equals("0") || tvPoints.getText().toString().isEmpty())) {
            tvPoints.setText(prefs.getString("total_points", "0"));
        }
        if (tvStreak != null && (tvStreak.getText().toString().equals("0 days") || tvStreak.getText().toString().isEmpty())) {
            tvStreak.setText(prefs.getString("learning_streak", "0 days"));
        }
    }

    private void refreshData() {
        loadUserFromFirestore();
        loadEnrolledCourses();
        loadStats();
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        if (tvUserName != null && savedInstanceState.containsKey("user_name")) {
            tvUserName.setText(savedInstanceState.getString("user_name", "Student"));
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
