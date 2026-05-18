package com.example.farmlink.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.activities.farmer.FarmerDashboardActivity;
import com.example.farmlink.R;
import com.example.farmlink.models.User;
import com.example.farmlink.activities.student.StudentDashboardActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etLocation, etPassword, etConfirmPassword;
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilLocation, tilPassword, tilConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView tvFarmer, tvStudent, tvLogin;
    private CheckBox cbTerms;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedRole = "farmer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews();

        // Setup role toggle
        setupRoleToggle();

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etLocation = findViewById(R.id.etLocation);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilLocation = findViewById(R.id.tilLocation);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        tvFarmer = findViewById(R.id.tvFarmer);
        tvStudent = findViewById(R.id.tvStudent);
        tvLogin = findViewById(R.id.tvLogin);
        cbTerms = findViewById(R.id.cbTerms);

        // Set initial button color
        btnSignUp.setBackgroundColor(getColor(R.color.farmer_green));

        // Hide location field initially (only for farmers)
        tilLocation.setVisibility(View.VISIBLE); // Farmers are default
    }

    private void setupRoleToggle() {
        tvFarmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRole = "farmer";
                updateRoleUI();
                tilLocation.setVisibility(View.VISIBLE);
                tilLocation.setHint("Farm Location");
            }
        });

        tvStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRole = "student";
                updateRoleUI();
                tilLocation.setVisibility(View.GONE);
            }
        });
    }

    private void updateRoleUI() {
        if (selectedRole.equals("farmer")) {
            tvFarmer.setBackground(getDrawable(R.drawable.role_selected));
            tvFarmer.setTextColor(getColor(android.R.color.white));
            tvStudent.setBackground(null);
            tvStudent.setTextColor(getColor(android.R.color.darker_gray));
            btnSignUp.setBackgroundColor(getColor(R.color.farmer_green));
        } else {
            tvStudent.setBackground(getDrawable(R.drawable.role_selected));
            tvStudent.setTextColor(getColor(android.R.color.white));
            tvFarmer.setBackground(null);
            tvFarmer.setTextColor(getColor(android.R.color.darker_gray));
            btnSignUp.setBackgroundColor(getColor(R.color.student_blue));
        }
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSignUp();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login
            }
        });
    }

    private void performSignUp() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation logic
        if (TextUtils.isEmpty(fullName)) { tilFullName.setError("Full name is required"); return; }
        if (TextUtils.isEmpty(email)) { tilEmail.setError("Email is required"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { tilEmail.setError("Valid email required"); return; }
        if (TextUtils.isEmpty(phone)) { tilPhone.setError("Phone is required"); return; }
        if (selectedRole.equals("farmer") && TextUtils.isEmpty(location)) { tilLocation.setError("Location required"); return; }
        if (password.length() < 6) { tilPassword.setError("Min 6 characters"); return; }
        if (!password.equals(confirmPassword)) { tilConfirmPassword.setError("Passwords mismatch"); return; }
        if (!cbTerms.isChecked()) { Toast.makeText(this, "Agree to terms", Toast.LENGTH_SHORT).show(); return; }

        btnSignUp.setEnabled(false);
        btnSignUp.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            
                            // Create User object with all data
                            User user = new User(userId, email, selectedRole);
                            user.setName(fullName);
                            user.setPhone(phone);
                            user.setLocation(location);

                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                            navigateToDashboard();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            btnSignUp.setEnabled(true);
                                            btnSignUp.setText("Create Account");
                                        }
                                    });
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Failed";
                            Toast.makeText(SignupActivity.this, "Sign up failed: " + error, Toast.LENGTH_SHORT).show();
                            btnSignUp.setEnabled(true);
                            btnSignUp.setText("Create Account");
                        }
                    }
                });
    }

    private void navigateToDashboard() {
        Intent intent;
        if (selectedRole.equals("farmer")) {
            intent = new Intent(SignupActivity.this, FarmerDashboardActivity.class);
        } else {
            intent = new Intent(SignupActivity.this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
