package com.example.farmlink.activities.farmer;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    // Views
    private ImageView btnBack;
    private TextInputEditText etProductName, etFarmerName, etPrice, etUnit;
    private TextInputEditText etQuantity, etCategory, etDescription;
    private SwitchMaterial switchOrganic;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmer_add_product);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Check login
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        preFillFarmerName();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etProductName = findViewById(R.id.etProductName);
        etFarmerName = findViewById(R.id.etFarmerName);
        etPrice = findViewById(R.id.etPrice);
        etUnit = findViewById(R.id.etUnit);
        etQuantity = findViewById(R.id.etQuantity);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);
        switchOrganic = findViewById(R.id.switchOrganic);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> addProductToFirestore());
    }

    private void preFillFarmerName() {
        // Try to get farmer name from Firestore
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            etFarmerName.setText(name);
                        } else {
                            // Use email prefix as fallback
                            String email = currentUser.getEmail();
                            if (email != null) {
                                String fallbackName = email.split("@")[0];
                                etFarmerName.setText(fallbackName);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Use email prefix as fallback
                    String email = currentUser.getEmail();
                    if (email != null) {
                        String fallbackName = email.split("@")[0];
                        etFarmerName.setText(fallbackName);
                    }
                });
    }

    private void addProductToFirestore() {
        // Validate inputs
        String productName = etProductName.getText().toString().trim();
        String farmerName = etFarmerName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isOrganic = switchOrganic.isChecked();

        if (productName.isEmpty()) {
            etProductName.setError("Product name required");
            etProductName.requestFocus();
            return;
        }

        if (farmerName.isEmpty()) {
            etFarmerName.setError("Farmer name required");
            etFarmerName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Price required");
            etPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etPrice.setError("Enter valid price");
            etPrice.requestFocus();
            return;
        }

        if (unit.isEmpty()) {
            etUnit.setError("Unit required (kg, bag, etc.)");
            etUnit.requestFocus();
            return;
        }

        if (quantityStr.isEmpty()) {
            etQuantity.setError("Quantity required");
            etQuantity.requestFocus();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter valid quantity");
            etQuantity.requestFocus();
            return;
        }

        if (category.isEmpty()) {
            category = "General";
        }

        // Show progress
        showProgress(true);

        // Create product data
        String productId = db.collection("products").document().getId();
        long timestamp = System.currentTimeMillis();
        String status = quantity > 0 ? "active" : "out_of_stock";

        Map<String, Object> productData = new HashMap<>();
        productData.put("productId", productId);
        productData.put("farmerId", currentUser.getUid());
        productData.put("farmerName", farmerName);
        productData.put("name", productName);
        productData.put("price", price);
        productData.put("unit", unit);
        productData.put("quantity", quantity);
        productData.put("category", category);
        productData.put("description", description);
        productData.put("isOrganic", isOrganic);
        productData.put("status", status);
        productData.put("averageRating", 0.0);
        productData.put("timestamp", timestamp);

        // Save to Firestore
        db.collection("products").document(productId)
                .set(productData)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(AddProductActivity.this,
                            "✅ " + productName + " added successfully!",
                            Toast.LENGTH_LONG).show();

                    // Clear form or go back
                    clearForm();

                    // Optional: Navigate back to dashboard
                    // finish();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(AddProductActivity.this,
                            "❌ Failed to add product: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        etProductName.setText("");
        etPrice.setText("");
        etQuantity.setText("");
        etDescription.setText("");
        switchOrganic.setChecked(false);
        etProductName.requestFocus();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
        btnSubmit.setText(show ? "Adding..." : "➕ Add Product");
    }
}