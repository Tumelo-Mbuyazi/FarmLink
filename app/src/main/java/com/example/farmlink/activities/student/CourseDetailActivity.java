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
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.farmlink.Auth.LoginActivity;
import com.example.farmlink.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class CourseDetailActivity extends AppCompatActivity {

    private static final String TAG = "CourseDetail";
    private static final String PREFS_NAME = "FarmLinkPrefs";
    private static final String KEY_COURSE_PREFIX = "course_";

    // Views
    private ImageView btnBack;
    private TextView tvCourseEmoji, tvCourseTitle, tvCourseDescription, tvInstructor;
    private TextView tvModules, tvDuration, tvPoints, tvEnrolledStudents, tvRating;
    private ProgressBar progressBar;
    private TextView tvProgressText;
    private MaterialButton btnEnroll;
    private NestedScrollView nestedScrollView;

    // Course Data
    private String courseId;
    private String courseTitle;
    private int courseProgress;
    private boolean isEnrolled = false;

    // Firebase
    private FirebaseAuth mAuth;

    // Lifecycle Variables
    private long startTime;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "========== ON CREATE ==========");
        setContentView(R.layout.student_course_detail);

        // Get intent data
        courseId = getIntent().getStringExtra("course_id");
        courseTitle = getIntent().getStringExtra("course_title");
        courseProgress = getIntent().getIntExtra("course_progress", 0);

        Log.d(TAG, "onCreate: Course ID = " + courseId + ", Title = " + courseTitle + ", Progress = " + courseProgress);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        initViews();

        // Setup Click Listeners
        setupClickListeners();

        // Load Course Data
        loadCourseData();

        // Check Enrollment Status
        checkEnrollmentStatus();

        // Setup Back Press Handler
        setupBackNavigation();

        // Restore saved state
        if (savedInstanceState != null) {
            restoreSavedState(savedInstanceState);
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: Navigating back");
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "========== ON START ==========");

        // Check authentication
        if (mAuth.getCurrentUser() == null) {
            Log.w(TAG, "onStart: User not authenticated");
            navigateToLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "========== ON RESUME ==========");
        startTime = System.currentTimeMillis();

        // Update last viewed course
        updateLastViewed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "========== ON PAUSE ==========");

        // Save progress if enrolled
        if (isEnrolled && courseProgress > 0) {
            saveProgress();
        }

        // Save session duration
        long sessionDuration = System.currentTimeMillis() - startTime;
        saveSessionDuration(sessionDuration);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "========== ON STOP ==========");
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "========== ON DESTROY ==========");
        cleanup();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "========== ON RESTART ==========");
        refreshCourseData();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "========== ON SAVE INSTANCE STATE ==========");

        outState.putString("course_id", courseId);
        outState.putString("course_title", courseTitle);
        outState.putInt("course_progress", courseProgress);
        outState.putBoolean("is_enrolled", isEnrolled);
        outState.putString("btn_text", btnEnroll.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "========== ON RESTORE INSTANCE STATE ==========");
        restoreSavedState(savedInstanceState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w(TAG, "onLowMemory: System running low on memory");
    }

    // ==================== INITIALIZATION METHODS ====================

    private void initViews() {
        Log.d(TAG, "initViews: Initializing views");

        btnBack = findViewById(R.id.btnBack);
        tvCourseEmoji = findViewById(R.id.tvCourseEmoji);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCourseDescription = findViewById(R.id.tvCourseDescription);
        tvInstructor = findViewById(R.id.tvInstructor);
        tvModules = findViewById(R.id.tvModules);
        tvDuration = findViewById(R.id.tvDuration);
        tvPoints = findViewById(R.id.tvPoints);
        tvEnrolledStudents = findViewById(R.id.tvEnrolledStudents);
        tvRating = findViewById(R.id.tvRating);
        progressBar = findViewById(R.id.progressBar);
        tvProgressText = findViewById(R.id.tvProgressText);
        btnEnroll = findViewById(R.id.btnEnroll);
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        btnEnroll.setOnClickListener(v -> {
            Log.d(TAG, "Enroll/Continue button clicked");
            if (isEnrolled) {
                continueLearning();
            } else {
                enrollInCourse();
            }
        });
    }

    // ==================== DATA LOADING METHODS ====================

    private void loadCourseData() {
        Log.d(TAG, "loadCourseData: Loading course data for ID: " + courseId);

        if (courseId == null) return;

        switch (courseId) {
            case "course_maize":
                tvCourseEmoji.setText("🌽");
                tvCourseTitle.setText("Maize Farming Basics");
                tvCourseDescription.setText("Learn everything about modern maize farming techniques, from planting to harvest. This comprehensive course covers soil preparation, seed selection, irrigation methods, pest control, and optimal harvesting techniques for maximum yield.");
                tvInstructor.setText("👨‍🌾 John Farmer");
                tvModules.setText("8");
                tvDuration.setText("8");
                tvPoints.setText("100");
                tvEnrolledStudents.setText("1,234");
                tvRating.setText("4.8");
                break;

            case "course_soil":
                tvCourseEmoji.setText("🌱");
                tvCourseTitle.setText("Soil Preparation Guide");
                tvCourseDescription.setText("Master the art of soil preparation for optimal crop growth. Learn about soil types, nutrient management, pH balancing, organic matter incorporation, and sustainable soil practices.");
                tvInstructor.setText("👩‍🌾 Sarah Green");
                tvModules.setText("6");
                tvDuration.setText("6");
                tvPoints.setText("80");
                tvEnrolledStudents.setText("856");
                tvRating.setText("4.9");
                break;

            case "course_pest":
                tvCourseEmoji.setText("🐛");
                tvCourseTitle.setText("Pest Control Methods");
                tvCourseDescription.setText("Discover effective and eco-friendly pest control methods to protect your crops. Learn about integrated pest management, biological controls, natural pesticides, and prevention strategies.");
                tvInstructor.setText("👨‍🔬 Dr. Wilson");
                tvModules.setText("10");
                tvDuration.setText("10");
                tvPoints.setText("120");
                tvEnrolledStudents.setText("2,101");
                tvRating.setText("4.7");
                break;

            default:
                tvCourseEmoji.setText("📚");
                tvCourseTitle.setText(courseTitle != null ? courseTitle : "General Farming");
                tvCourseDescription.setText("Learn valuable farming techniques and best practices in this comprehensive course.");
                tvInstructor.setText("👨‍🌾 Expert Farmer");
                tvModules.setText("5");
                tvDuration.setText("5");
                tvPoints.setText("50");
                tvEnrolledStudents.setText("500");
                tvRating.setText("4.5");
                break;
        }

        // Set progress if enrolled
        if (courseProgress > 0) {
            showProgress(courseProgress);
        }
    }

    private void showProgress(int progress) {
        Log.d(TAG, "showProgress: Showing progress = " + progress + "%");

        if (progressBar != null) {
            progressBar.setProgress(progress);
            progressBar.setVisibility(View.VISIBLE);
            if (tvProgressText != null) {
                tvProgressText.setVisibility(View.VISIBLE);
                tvProgressText.setText(progress + "% Complete");
            }
        }
    }

    private void checkEnrollmentStatus() {
        Log.d(TAG, "checkEnrollmentStatus: Checking if user is enrolled");

        // Check SharedPreferences for enrollment status
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String enrollKey = KEY_COURSE_PREFIX + courseId;
        isEnrolled = prefs.getBoolean(enrollKey, courseProgress > 0);

        if (isEnrolled) {
            btnEnroll.setText("Continue Learning");
            btnEnroll.setBackgroundTintList(getColorStateList(android.R.color.holo_orange_dark));
            Log.d(TAG, "checkEnrollmentStatus: User is enrolled");
        } else {
            btnEnroll.setText("Enroll Now - Free");
            btnEnroll.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
            Log.d(TAG, "checkEnrollmentStatus: User is not enrolled");
        }
    }

    private void enrollInCourse() {
        Log.d(TAG, "enrollInCourse: Enrolling user in course: " + courseTitle);

        // Show loading state
        btnEnroll.setEnabled(false);
        btnEnroll.setText("Enrolling...");

        // Simulate network delay (replace with Firestore later)
        handler.postDelayed(() -> {
            // Save enrollment to SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String enrollKey = KEY_COURSE_PREFIX + courseId;
            prefs.edit().putBoolean(enrollKey, true).apply();

            isEnrolled = true;
            courseProgress = 0;
            btnEnroll.setText("Continue Learning");
            btnEnroll.setBackgroundTintList(getColorStateList(android.R.color.holo_orange_dark));
            btnEnroll.setEnabled(true);

            // Show progress bar
            showProgress(0);

            Toast.makeText(this, "Successfully enrolled in " + courseTitle, Toast.LENGTH_LONG).show();
            Log.d(TAG, "enrollInCourse: Enrollment successful");
        }, 1500);
    }

    private void continueLearning() {
        Log.d(TAG, "continueLearning: Continuing course: " + courseTitle + " at " + courseProgress + "%");

        // If course is completed, go to certificate
        if (courseProgress >= 100) {
            Toast.makeText(this, "Course completed! View your certificate.", Toast.LENGTH_SHORT).show();
            // Navigate to certificate activity
            return;
        }

        // Find first incomplete module and start it
        Toast.makeText(this, "Resuming course from " + courseProgress + "% complete", Toast.LENGTH_SHORT).show();
        // Navigate to video player or next module
    }

    private void refreshCourseData() {
        Log.d(TAG, "refreshCourseData: Refreshing course data");

        // Reload enrollment status from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String enrollKey = KEY_COURSE_PREFIX + courseId;
        boolean wasEnrolled = prefs.getBoolean(enrollKey, false);

        if (wasEnrolled != isEnrolled) {
            checkEnrollmentStatus();
        }

        // Reload progress
        int savedProgress = prefs.getInt(KEY_COURSE_PREFIX + courseId + "_progress", courseProgress);
        if (savedProgress != courseProgress) {
            courseProgress = savedProgress;
            showProgress(courseProgress);
        }
    }

    private void saveProgress() {
        Log.d(TAG, "saveProgress: Saving progress = " + courseProgress + "% for course: " + courseId);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_COURSE_PREFIX + courseId + "_progress", courseProgress).apply();
    }

    private void updateLastViewed() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong("last_viewed_course_" + courseId, System.currentTimeMillis()).apply();
        Log.d(TAG, "updateLastViewed: Updated last viewed timestamp");
    }

    private void saveSessionDuration(long duration) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long totalTime = prefs.getLong("total_course_time", 0);
        long newTotal = totalTime + duration;
        prefs.edit().putLong("total_course_time", newTotal).apply();
        Log.d(TAG, "Session duration: " + duration + "ms, Total: " + newTotal + "ms");
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        Log.d(TAG, "restoreSavedState: Restoring saved state");

        if (savedInstanceState.containsKey("course_id")) {
            courseId = savedInstanceState.getString("course_id");
        }
        if (savedInstanceState.containsKey("course_title")) {
            courseTitle = savedInstanceState.getString("course_title");
            tvCourseTitle.setText(courseTitle);
        }
        if (savedInstanceState.containsKey("course_progress")) {
            courseProgress = savedInstanceState.getInt("course_progress");
            showProgress(courseProgress);
        }
        if (savedInstanceState.containsKey("is_enrolled")) {
            isEnrolled = savedInstanceState.getBoolean("is_enrolled");
        }
        if (savedInstanceState.containsKey("btn_text")) {
            btnEnroll.setText(savedInstanceState.getString("btn_text"));
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void cleanup() {
        Log.d(TAG, "cleanup: Cleaning up resources");
        handler.removeCallbacksAndMessages(null);
    }
}
