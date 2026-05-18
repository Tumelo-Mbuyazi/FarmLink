package com.example.farmlink.activities.student;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RequestFarmVisitActivity extends AppCompatActivity {

    private AutoCompleteTextView actvFarmer, actvTime;
    private EditText etDate, etPeopleCount, etSpecialRequests;
    private MaterialButton btnSubmitRequest;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String[] farmers = {"John's Farm", "Green Acres Farm", "Sunrise Organics", "Fresh Harvest Farm"};
    private String[] timeSlots = {"9:00 AM", "10:00 AM", "11:00 AM", "2:00 PM", "3:00 PM", "4:00 PM"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_request_farm_visit);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinners();
        setupDatePicker();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSubmitRequest.setOnClickListener(v -> submitRequest());
    }

    private void initViews() {
        actvFarmer = findViewById(R.id.actvFarmer);
        actvTime = findViewById(R.id.actvTime);
        etDate = findViewById(R.id.etDate);
        etPeopleCount = findViewById(R.id.etPeopleCount);
        etSpecialRequests = findViewById(R.id.etSpecialRequests);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
    }

    private void setupSpinners() {
        ArrayAdapter<String> farmerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, farmers);
        actvFarmer.setAdapter(farmerAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, timeSlots);
        actvTime.setAdapter(timeAdapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                etDate.setText(date);
            }, year, month, day);

            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.show();
        });
    }

    private void submitRequest() {
        String farmer = actvFarmer.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = actvTime.getText().toString().trim();
        String peopleCount = etPeopleCount.getText().toString().trim();
        String specialRequests = etSpecialRequests.getText().toString().trim();

        if (farmer.isEmpty()) {
            actvFarmer.setError("Please select a farmer");
            return;
        }

        if (date.isEmpty()) {
            etDate.setError("Please select a date");
            return;
        }

        if (time.isEmpty()) {
            actvTime.setError("Please select a time");
            return;
        }

        if (peopleCount.isEmpty()) {
            etPeopleCount.setError("Please enter number of people");
            return;
        }

        btnSubmitRequest.setEnabled(false);
        btnSubmitRequest.setText("Sending Request...");

        Map<String, Object> visitData = new HashMap<>();
        visitData.put("studentId", mAuth.getCurrentUser().getUid());
        visitData.put("farmerName", farmer);
        visitData.put("date", date);
        visitData.put("time", time);
        visitData.put("peopleCount", Integer.parseInt(peopleCount));
        visitData.put("specialRequests", specialRequests);
        visitData.put("status", "pending");
        visitData.put("timestamp", System.currentTimeMillis());

        db.collection("farmVisits").add(visitData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Visit request sent! Farmer will confirm soon.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmitRequest.setEnabled(true);
                    btnSubmitRequest.setText("Send Request");
                });
    }
}
