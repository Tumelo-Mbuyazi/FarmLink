package com.example.farmlink.Auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.activities.farmer.FarmerDashboardActivity;
import com.example.farmlink.R;
import com.example.farmlink.models.User;
import com.example.farmlink.activities.student.StudentDashboardActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "FarmLinkPrefs";
    private static final String KEY_LAST_VISITED = "last_visited_login";
    private static final String KEY_SAVED_EMAIL = "saved_email";

    // Views
    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin;
    private TextView tvFarmer, tvStudent;
    private TextView tvSignUp;
    private TextView tvForgotPassword;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedRole = "farmer";

    // Lifecycle Variables
    private long startTime;
    private boolean doubleBackToExitPressedOnce = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "========== ON CREATE ==========");
        setContentView(R.layout.auth_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews();

        // Setup role toggle
        setupRoleToggle();

        // Setup click listeners
        setupClickListeners();

        // Setup Back Navigation
        setupBackNavigation();

        // Restore saved state
        if (savedInstanceState != null) {
            restoreSavedState(savedInstanceState);
        }

        // Load saved email from SharedPreferences
        loadSavedEmail();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "onCreate: User already logged in, checking role");
            checkUserRoleAndNavigate(mAuth.getCurrentUser().getUid());
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: Back pressed");

                if (doubleBackToExitPressedOnce) {
                    finish();
                    return;
                }

                doubleBackToExitPressedOnce = true;
                Toast.makeText(LoginActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();

                handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "========== ON START ==========");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "========== ON RESUME ==========");

        startTime = System.currentTimeMillis();

        // Update last visited timestamp
        updateLastVisited();

        // Clear any temporary errors
        clearErrors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "========== ON PAUSE ==========");

        // Save session duration
        long sessionDuration = System.currentTimeMillis() - startTime;
        saveSessionDuration(sessionDuration);

        // Save email for auto-fill next time
        saveEmail();
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
        clearErrors();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "========== ON SAVE INSTANCE STATE ==========");

        // Save current state
        outState.putString("email", etEmail.getText().toString());
        outState.putString("selected_role", selectedRole);
        outState.putBoolean("is_loading", !btnLogin.isEnabled());
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
        Log.w(TAG, "onLowMemory: System is running low on memory");
        // Clear caches if needed
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initializing views");

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvFarmer = findViewById(R.id.tvFarmer);
        tvStudent = findViewById(R.id.tvStudent);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Verify views are not null
        verifyViews();
    }

    private void verifyViews() {
        if (etEmail == null) Log.e(TAG, "etEmail is NULL!");
        if (etPassword == null) Log.e(TAG, "etPassword is NULL!");
        if (btnLogin == null) Log.e(TAG, "btnLogin is NULL!");
    }

    private void setupRoleToggle() {
        Log.d(TAG, "setupRoleToggle: Setting up role toggle");

        tvFarmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Farmer role selected");
                selectedRole = "farmer";
                updateRoleUI();
            }
        });

        tvStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Student role selected");
                selectedRole = "student";
                updateRoleUI();
            }
        });
    }

    private void updateRoleUI() {
        Log.d(TAG, "updateRoleUI: Updating UI for role: " + selectedRole);

        if (selectedRole.equals("farmer")) {
            tvFarmer.setBackground(getDrawable(R.drawable.role_selected_modern));
            tvFarmer.setTextColor(getColor(android.R.color.white));
            tvStudent.setBackground(null);
            tvStudent.setTextColor(getColor(R.color.gray_text));
            btnLogin.setBackgroundColor(getColor(R.color.farmer_green));
        } else {
            tvStudent.setBackground(getDrawable(R.drawable.role_selected_modern));
            tvStudent.setTextColor(getColor(android.R.color.white));
            tvFarmer.setBackground(null);
            tvFarmer.setTextColor(getColor(R.color.gray_text));
            btnLogin.setBackgroundColor(getColor(R.color.student_blue));
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login button clicked");
                performLogin();
            }
        });

        tvForgotPassword.setOnClickListener(v -> {
            Log.d(TAG, "Forgot password clicked");
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sign up clicked");
                navigateToSignUp();
            }
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "performLogin: Attempting login for email: " + email);

        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            Log.w(TAG, "Login failed: Email is required");
            tilEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.w(TAG, "Login failed: Invalid email format");
            tilEmail.setError("Please enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Log.w(TAG, "Login failed: Password is required");
            tilPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            Log.w(TAG, "Login failed: Password too short");
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign In");

                        if (task.isSuccessful()) {
                            Log.d(TAG, "performLogin: Login successful for: " + email);
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkUserRoleAndNavigate(user.getUid());
                        } else {
                            // Sign in fails
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Authentication failed";
                            Log.e(TAG, "performLogin: Login failed - " + errorMessage);
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkUserRoleAndNavigate(String userId) {
        Log.d(TAG, "checkUserRoleAndNavigate: Checking role for user: " + userId);

        // Check user's role from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String userRole = document.getString("role");
                                Log.d(TAG, "checkUserRoleAndNavigate: User role = " + userRole);

                                // Verify role matches selected role
                                if (userRole != null && userRole.equals(selectedRole)) {
                                    Log.d(TAG, "checkUserRoleAndNavigate: Role matches, navigating to dashboard");
                                    navigateToDashboard(userRole);
                                } else {
                                    // Role mismatch - sign out and show error
                                    Log.w(TAG, "checkUserRoleAndNavigate: Role mismatch. Expected: " + selectedRole + ", Got: " + userRole);
                                    mAuth.signOut();
                                    Toast.makeText(LoginActivity.this,
                                            "Please login with correct role.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Log.w(TAG, "checkUserRoleAndNavigate: User document doesn't exist");
                                // User document doesn't exist - create one
                                createUserRoleDocument(userId);
                            }
                        } else {
                            Log.e(TAG, "checkUserRoleAndNavigate: Error checking user role", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    "Error checking user role", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createUserRoleDocument(String userId) {
        Log.d(TAG, "createUserRoleDocument: Creating user document for: " + userId);

        // Create user document with selected role
        User user = new User(userId, mAuth.getCurrentUser().getEmail(), selectedRole);

        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserRoleDocument: User document created successfully");
                            navigateToDashboard(selectedRole);
                        } else {
                            Log.e(TAG, "createUserRoleDocument: Error creating user profile", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    "Error creating user profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToDashboard(String role) {
        Log.d(TAG, "navigateToDashboard: Navigating to dashboard for role: " + role);

        Intent intent;
        if (role.equals("farmer")) {
            intent = new Intent(LoginActivity.this, FarmerDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();
        Log.d(TAG, "handleForgotPassword: Forgot password requested for: " + email);

        if (TextUtils.isEmpty(email)) {
            Log.w(TAG, "handleForgotPassword: Email is empty");
            tilEmail.setError("Enter your email to reset password");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.w(TAG, "handleForgotPassword: Invalid email format");
            tilEmail.setError("Please enter a valid email");
            return;
        }

        // Show loading
        btnLogin.setEnabled(false);
        btnLogin.setText("Sending...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign In");

                        if (task.isSuccessful()) {
                            Log.d(TAG, "handleForgotPassword: Reset email sent to: " + email);
                            Toast.makeText(LoginActivity.this,
                                    "Password reset email sent! Check your inbox.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            String error = task.getException() != null ?
                                    task.getException().getMessage() : "Failed to send reset email";
                            Log.e(TAG, "handleForgotPassword: Failed - " + error);
                            Toast.makeText(LoginActivity.this,
                                    error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToSignUp() {
        Log.d(TAG, "navigateToSignUp: Navigating to SignupActivity");
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    // ==================== SHARED PREFERENCES METHODS ====================

    private void loadSavedEmail() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_SAVED_EMAIL, "");
        if (!TextUtils.isEmpty(savedEmail)) {
            etEmail.setText(savedEmail);
            Log.d(TAG, "loadSavedEmail: Loaded saved email: " + savedEmail);
        }
    }

    private void saveEmail() {
        String email = etEmail.getText().toString().trim();
        if (!TextUtils.isEmpty(email)) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(KEY_SAVED_EMAIL, email).apply();
            Log.d(TAG, "saveEmail: Saved email to preferences");
        }
    }

    private void updateLastVisited() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long lastVisited = System.currentTimeMillis();
        prefs.edit().putLong(KEY_LAST_VISITED, lastVisited).apply();
        Log.d(TAG, "updateLastVisited: " + lastVisited);
    }

    private void saveSessionDuration(long duration) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long totalTime = prefs.getLong("total_login_time", 0);
        long newTotal = totalTime + duration;
        prefs.edit().putLong("total_login_time", newTotal).apply();
        Log.d(TAG, "Session duration: " + duration + "ms, Total: " + newTotal + "ms");
    }

    // ==================== UI HELPER METHODS ====================

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        Log.d(TAG, "clearErrors: Cleared all input errors");
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        Log.d(TAG, "restoreSavedState: Restoring saved state");

        if (savedInstanceState.containsKey("email")) {
            etEmail.setText(savedInstanceState.getString("email"));
        }
        if (savedInstanceState.containsKey("selected_role")) {
            selectedRole = savedInstanceState.getString("selected_role");
            updateRoleUI();
        }
        if (savedInstanceState.containsKey("is_loading")) {
            boolean isLoading = savedInstanceState.getBoolean("is_loading");
            if (isLoading) {
                btnLogin.setEnabled(false);
                btnLogin.setText("Signing in...");
            }
        }
    }

    private void cleanup() {
        Log.d(TAG, "cleanup: Cleaning up resources");
        handler.removeCallbacksAndMessages(null);
        // Remove any pending network callbacks if needed
    }
}
