package com.example.farmlink.activities.farmer;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.farmlink.R;
import com.example.farmlink.models.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {



    // Views
    private ImageView btnBack;
    private TextInputEditText etProductName, etFarmerName, etPrice, etUnit;
    private TextInputEditText etQuantity, etCategory, etDescription;
    private SwitchMaterial switchOrganic;
    private AutoCompleteTextView etStatus;
    private MaterialButton btnUpdate, btnDelete;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Data
    private String productId;
    private Product currentProduct;

    // Status options
    private static final String[] STATUS_OPTIONS = {"active", "out_of_stock", "discontinued"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmer_edit_product);

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

        // Get product ID from intent
        productId = getIntent().getStringExtra("product_id");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupStatusDropdown();
        setupClickListeners();
        loadProductData();
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
        etStatus = findViewById(R.id.etStatus);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupStatusDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, STATUS_OPTIONS);
        etStatus.setAdapter(adapter);
        etStatus.setThreshold(1);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> updateProduct());

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadProductData() {
        showProgress(true);

        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentProduct = documentSnapshot.toObject(Product.class);
                        if (currentProduct != null) {
                            currentProduct.setProductId(documentSnapshot.getId());
                            displayProductData();
                        } else {
                            Toast.makeText(this, "Failed to load product", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    showProgress(false);
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, "Error loading product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayProductData() {
        etProductName.setText(currentProduct.getName());
        etFarmerName.setText(currentProduct.getFarmerName());
        etPrice.setText(String.valueOf(currentProduct.getPrice()));
        etUnit.setText(currentProduct.getUnit());
        etQuantity.setText(String.valueOf(currentProduct.getQuantity()));
        etCategory.setText(currentProduct.getCategory());
        etDescription.setText(currentProduct.getDescription());
        switchOrganic.setChecked(currentProduct.isOrganic());

        // Set status
        String status = currentProduct.getStatus();
        if (status == null || status.isEmpty()) {
            status = "active";
        }
        etStatus.setText(status, false);
    }

    private void updateProduct() {
        // Validate inputs
        String productName = etProductName.getText().toString().trim();
        String farmerName = etFarmerName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isOrganic = switchOrganic.isChecked();
        String status = etStatus.getText().toString().trim();

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
            etUnit.setError("Unit required");
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
            if (quantity < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter valid quantity");
            etQuantity.requestFocus();
            return;
        }

        if (category.isEmpty()) {
            category = "General";
        }

        if (status.isEmpty()) {
            status = quantity > 0 ? "active" : "out_of_stock";
        }

        // Update status based on quantity
        if (quantity == 0) {
            status = "out_of_stock";
        } else if (status.equals("out_of_stock") && quantity > 0) {
            status = "active";
        }

        showProgress(true);

        // Create update data
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", productName);
        updates.put("farmerName", farmerName);
        updates.put("price", price);
        updates.put("unit", unit);
        updates.put("quantity", quantity);
        updates.put("category", category);
        updates.put("description", description);
        updates.put("isOrganic", isOrganic);
        updates.put("status", status);
        updates.put("lastUpdated", System.currentTimeMillis());

        // Update in Firestore
        db.collection("products").document(productId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(EditProductActivity.this,
                            "✅ Product updated successfully!",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(EditProductActivity.this,
                            "❌ Failed to update: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + currentProduct.getName() + "\"?\n\nThis action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes, Delete", (dialog, which) -> deleteProduct())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProduct() {
        showProgress(true);

        db.collection("products").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(EditProductActivity.this,
                            "🗑️ Product deleted successfully!",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(EditProductActivity.this,
                            "❌ Failed to delete: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!show);
        btnDelete.setEnabled(!show);
        btnUpdate.setText(show ? "Updating..." : "💾 Update");
    }
}