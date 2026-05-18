package com.example.farmlink.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.example.farmlink.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";
    private static final String PREFS_NAME = "FarmLinkPrefs";

    // Views
    private ImageView btnBack;
    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;
    private MaterialButton btnSendReset;
    private TextView tvBackToLogin;
    private View layoutBackToLogin;

    // Firebase
    private FirebaseAuth mAuth;

    // Lifecycle Variables
    private long startTime;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "========== ON CREATE ==========");
        setContentView(R.layout.auth_forgot_password);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        initViews();

        // Setup Click Listeners
        setupClickListeners();

        // Setup Back Navigation
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
                Log.d(TAG, "========== ON BACK PRESSED (Dispatcher) ==========");
                navigateToLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "========== ON RESUME ==========");
        startTime = System.currentTimeMillis();
        updateLastVisited();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "========== ON PAUSE ==========");
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
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", etEmail.getText().toString());
        outState.putBoolean("is_sending", !btnSendReset.isEnabled());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreSavedState(savedInstanceState);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        tilEmail = findViewById(R.id.tilEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        layoutBackToLogin = findViewById(R.id.layoutBackToLogin);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> navigateToLogin());
        if (layoutBackToLogin != null) {
            layoutBackToLogin.setOnClickListener(v -> navigateToLogin());
        }
        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> navigateToLogin());
        }
        btnSendReset.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();
        tilEmail.setError(null);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            return;
        }

        setLoadingState(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);
                    if (task.isSuccessful()) {
                        showSuccessDialog(email);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        btnSendReset.setEnabled(!isLoading);
        btnSendReset.setText(isLoading ? "Sending..." : "Send Reset Link");
        etEmail.setEnabled(!isLoading);
    }

    private void showSuccessDialog(String email) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Link Sent")
                .setMessage("We've sent a password reset link to:\n\n" + email)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Back to Login", (dialog, which) -> navigateToLogin())
                .setCancelable(false)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void updateLastVisited() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit().putLong("last_visited_forgot_password", System.currentTimeMillis()).apply();
    }

    private void saveSessionDuration(long duration) {
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long totalTime = prefs.getLong("total_forgot_password_time", 0);
        prefs.edit().putLong("total_forgot_password_time", totalTime + duration).apply();
    }

    private void restoreSavedState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("email")) {
            etEmail.setText(savedInstanceState.getString("email"));
        }
        if (savedInstanceState.getBoolean("is_sending", false)) {
            setLoadingState(true);
        }
    }
}
