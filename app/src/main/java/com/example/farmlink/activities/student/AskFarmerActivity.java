package com.example.farmlink.activities.student;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AskFarmerActivity extends AppCompatActivity {

    private AutoCompleteTextView actvFarmer, actvCategory;
    private TextInputEditText etTitle, etQuestion;
    private MaterialButton btnSubmitQuestion;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String[] farmers = {"John's Farm", "Green Acres Farm", "Sunrise Organics", "Fresh Harvest Farm"};
    private String[] categories = {"Planting", "Pest Control", "Harvesting", "Soil Health", "Irrigation", "Livestock"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_ask_farmer);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinners();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSubmitQuestion.setOnClickListener(v -> submitQuestion());
    }

    private void initViews() {
        actvFarmer = findViewById(R.id.actvFarmer);
        actvCategory = findViewById(R.id.actvCategory);
        etTitle = findViewById(R.id.etTitle);
        etQuestion = findViewById(R.id.etQuestion);
        btnSubmitQuestion = findViewById(R.id.btnSubmitQuestion);
    }

    private void setupSpinners() {
        ArrayAdapter<String> farmerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, farmers);
        actvFarmer.setAdapter(farmerAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(categoryAdapter);
    }

    private void submitQuestion() {
        String farmer = actvFarmer.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String question = etQuestion.getText().toString().trim();

        if (farmer.isEmpty()) {
            actvFarmer.setError("Please select a farmer");
            return;
        }

        if (title.isEmpty()) {
            etTitle.setError("Please enter a title");
            return;
        }

        if (question.isEmpty()) {
            etQuestion.setError("Please enter your question");
            return;
        }

        btnSubmitQuestion.setEnabled(false);
        btnSubmitQuestion.setText("Submitting...");

        Map<String, Object> questionData = new HashMap<>();
        questionData.put("studentId", mAuth.getCurrentUser().getUid());
        questionData.put("farmerName", farmer);
        questionData.put("category", category);
        questionData.put("title", title);
        questionData.put("question", question);
        questionData.put("status", "pending");
        questionData.put("timestamp", System.currentTimeMillis());

        db.collection("questions").add(questionData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Question submitted successfully! Farmer will respond soon.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmitQuestion.setEnabled(true);
                    btnSubmitQuestion.setText("Submit Question");
                });
    }
}