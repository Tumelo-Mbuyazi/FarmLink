package com.example.farmlink.activities.student;



import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class OrderTrackingActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvOrderId, tvOrderDate, tvDeliveryAddress, tvPaymentMethod;
    private TextView tvStatusPending, tvStatusProcessing, tvStatusShipped, tvStatusDelivered;
    private TextView tvEstimatedDelivery, tvTotalAmount;
    private MaterialButton btnContactSupport;

    private FirebaseFirestore db;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order_id");

        initViews();
        setupClickListeners();
        loadOrderDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvStatusPending = findViewById(R.id.tvStatusPending);
        tvStatusProcessing = findViewById(R.id.tvStatusProcessing);
        tvStatusShipped = findViewById(R.id.tvStatusShipped);
        tvStatusDelivered = findViewById(R.id.tvStatusDelivered);
        tvEstimatedDelivery = findViewById(R.id.tvEstimatedDelivery);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnContactSupport = findViewById(R.id.btnContactSupport);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnContactSupport.setOnClickListener(v -> {
            Toast.makeText(this, "Contact support: support@farmlink.com", Toast.LENGTH_LONG).show();
        });
    }

    private void loadOrderDetails() {
        db.collection("orders").document(orderId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        displayOrderDetails(doc);
                    } else {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading order", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayOrderDetails(com.google.firebase.firestore.DocumentSnapshot doc) {
        String shortOrderId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;
        tvOrderId.setText("#" + shortOrderId.toUpperCase());
        tvOrderDate.setText(doc.getString("date"));
        tvDeliveryAddress.setText(doc.getString("deliveryAddress"));
        tvPaymentMethod.setText(doc.getString("paymentMethod"));

        Double total = doc.getDouble("totalAmount");
        tvTotalAmount.setText(String.format("R%.2f", total));

        String status = doc.getString("status");
        updateStatusUI(status);

        // Calculate estimated delivery (3 days after order)
        Long timestamp = doc.getLong("timestamp");
        if (timestamp != null) {
            long estimatedDate = timestamp + (3 * 24 * 60 * 60 * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            tvEstimatedDelivery.setText(sdf.format(new Date(estimatedDate)));
        }
    }

    private void updateStatusUI(String status) {
        // Reset all
        resetStatusViews();

        switch (status) {
            case "pending":
                tvStatusPending.setBackgroundColor(getColor(R.color.status_active));
                tvStatusPending.setTextColor(getColor(android.R.color.white));
                break;
            case "processing":
                tvStatusPending.setBackgroundColor(getColor(R.color.status_completed));
                tvStatusProcessing.setBackgroundColor(getColor(R.color.status_active));
                tvStatusProcessing.setTextColor(getColor(android.R.color.white));
                break;
            case "shipped":
                tvStatusPending.setBackgroundColor(getColor(R.color.status_completed));
                tvStatusProcessing.setBackgroundColor(getColor(R.color.status_completed));
                tvStatusShipped.setBackgroundColor(getColor(R.color.status_active));
                tvStatusShipped.setTextColor(getColor(android.R.color.white));
                break;
            case "delivered":
                tvStatusPending.setBackgroundColor(getColor(R.color.status_completed));
                tvStatusProcessing.setBackgroundColor(getColor(R.color.status_completed));
                tvStatusShipped.setBackgroundColor(getColor(R.color.status_completed));
                tvStatusDelivered.setBackgroundColor(getColor(R.color.status_active));
                tvStatusDelivered.setTextColor(getColor(android.R.color.white));
                break;
        }
    }

    private void resetStatusViews() {
        int inactiveColor = getColor(R.color.status_inactive);
        int textColor = getColor(android.R.color.black);

        tvStatusPending.setBackgroundColor(inactiveColor);
        tvStatusProcessing.setBackgroundColor(inactiveColor);
        tvStatusShipped.setBackgroundColor(inactiveColor);
        tvStatusDelivered.setBackgroundColor(inactiveColor);

        tvStatusPending.setTextColor(textColor);
        tvStatusProcessing.setTextColor(textColor);
        tvStatusShipped.setTextColor(textColor);
        tvStatusDelivered.setTextColor(textColor);
    }
}