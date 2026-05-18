package com.example.farmlink.activities.student;



import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.R;
import com.example.farmlink.activities.farmer.FarmerOrdersActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView tvOrderId, tvDate, tvPaymentMethod, tvAddress, tvTotal;
    private MaterialButton btnTrackOrder, btnContinueShopping;
    private TextView btnViewOrders;

    private FirebaseFirestore db;
    private String orderId;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        db = FirebaseFirestore.getInstance();

        // Get data from intent
        orderId = getIntent().getStringExtra("order_id");
        totalAmount = getIntent().getDoubleExtra("total_amount", 0);
        String paymentMethod = getIntent().getStringExtra("payment_method");
        String deliveryAddress = getIntent().getStringExtra("delivery_address");
        String fullName = getIntent().getStringExtra("full_name");

        initViews();
        displayOrderDetails(paymentMethod, deliveryAddress);
        setupClickListeners();
        saveOrderToFirestore(paymentMethod, deliveryAddress, fullName);
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvDate = findViewById(R.id.tvDate);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvAddress = findViewById(R.id.tvAddress);
        tvTotal = findViewById(R.id.tvTotal);
        btnTrackOrder = findViewById(R.id.btnTrackOrder);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        btnViewOrders = findViewById(R.id.btnViewOrders);
    }

    private void displayOrderDetails(String paymentMethod, String deliveryAddress) {
        // Display order ID
        if (orderId != null && !orderId.isEmpty()) {
            String shortOrderId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;
            tvOrderId.setText("#" + shortOrderId.toUpperCase());
        } else {
            tvOrderId.setText("#ORD" + System.currentTimeMillis());
        }

        // Display current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));

        // Display payment method
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            tvPaymentMethod.setText(paymentMethod);
        } else {
            tvPaymentMethod.setText("Cash on Delivery");
        }

        // Display delivery address
        if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
            tvAddress.setText(deliveryAddress);
        } else {
            tvAddress.setText("Address not specified");
        }

        // Display total amount
        tvTotal.setText(String.format("R%.2f", totalAmount));
    }

    private void setupClickListeners() {
        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, StudentMarketplaceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnTrackOrder.setOnClickListener(v -> {
            // Navigate to Order Tracking
            Intent intent = new Intent(OrderConfirmationActivity.this, OrderTrackingActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });

        btnViewOrders.setOnClickListener(v -> {
            // Navigate to My Orders
            Intent intent = new Intent(OrderConfirmationActivity.this, MyOrdersActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void saveOrderToFirestore(String paymentMethod, String deliveryAddress, String fullName) {
        if (orderId == null || orderId.isEmpty()) return;

        // Update order with additional details if needed
        // Most data is already saved from CheckoutActivity
    }

    @Override
    public void onBackPressed() {
        // Go to marketplace instead of back to checkout
        Intent intent = new Intent(OrderConfirmationActivity.this, StudentMarketplaceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}