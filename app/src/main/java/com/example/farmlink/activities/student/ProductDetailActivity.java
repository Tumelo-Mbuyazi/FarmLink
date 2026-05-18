package com.example.farmlink.activities.student;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.farmlink.R;
import com.example.farmlink.models.Product;
import com.example.farmlink.utils.CartManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

public class ProductDetailActivity extends AppCompatActivity {

    // Views
    private ImageView btnBack;
    private TextView tvEmoji, tvProductName, tvFarmerName, tvCategory;
    private TextView tvPrice, tvQuantity, tvStockStatus, tvDescription;
    private TextView tvSelectedQuantity, tvMaxWarning;
    private TextView btnDecrease, btnIncrease;
    private MaterialButton btnAddToCart, btnBuyNow;
    private View layoutOrganic;

    // Data
    private Product product;
    private int selectedQuantity = 1;
    private CartManager cartManager;
    private FirebaseFirestore db;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize
        cartManager = CartManager.getInstance(this);
        db = FirebaseFirestore.getInstance();

        initViews();
        loadProductData();
        setupClickListeners();
        updateQuantityDisplay();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEmoji = findViewById(R.id.tvEmoji);
        tvProductName = findViewById(R.id.tvProductName);
        tvFarmerName = findViewById(R.id.tvFarmerName);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvStockStatus = findViewById(R.id.tvStockStatus);
        tvDescription = findViewById(R.id.tvDescription);
        tvSelectedQuantity = findViewById(R.id.tvSelectedQuantity);
        tvMaxWarning = findViewById(R.id.tvMaxWarning);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        layoutOrganic = findViewById(R.id.layoutOrganic);
    }

    private void loadProductData() {
        // Get product from intent
        String productJson = getIntent().getStringExtra("product");
        String productId = getIntent().getStringExtra("product_id");

        if (productJson != null) {
            product = gson.fromJson(productJson, Product.class);
            displayProductData();
        } else if (productId != null) {
            // Load from Firestore
            loadProductFromFirestore(productId);
        } else {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProductFromFirestore(String productId) {
        db.collection("products").document(productId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        product = doc.toObject(Product.class);
                        if (product != null) {
                            product.setProductId(doc.getId());
                            displayProductData();
                        }
                    } else {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayProductData() {
        if (product == null) return;

        tvEmoji.setText(product.getEmoji());
        tvProductName.setText(product.getName());
        tvFarmerName.setText(product.getFarmerName());

        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            tvCategory.setText(product.getCategory());
        } else {
            tvCategory.setText("General");
        }

        tvPrice.setText(String.format("R%.2f/%s", product.getPrice(), product.getUnit()));
        tvQuantity.setText(product.getQuantity() + " " + product.getUnit() + " available");

        // Set description
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            tvDescription.setText(product.getDescription());
        } else {
            tvDescription.setText("No description available for this product.");
        }

        // Set stock status
        if (product.getQuantity() <= 0) {
            tvStockStatus.setText("Sold Out");
            tvStockStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.sold_out_red));
            tvStockStatus.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnAddToCart.setEnabled(false);
            btnBuyNow.setEnabled(false);
        } else if (product.getQuantity() < 10) {
            tvStockStatus.setText("Low Stock");
            tvStockStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.low_stock_yellow));
            tvStockStatus.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        } else {
            tvStockStatus.setText("In Stock");
            tvStockStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.in_stock_green));
            tvStockStatus.setTextColor(ContextCompat.getColor(this, R.color.farmer_green));
        }

        // Show organic badge
        if (product.isOrganic()) {
            layoutOrganic.setVisibility(View.VISIBLE);
        }

        // Set max quantity warning
        if (product.getQuantity() > 0) {
            tvMaxWarning.setText("Max available: " + product.getQuantity() + " " + product.getUnit());
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnDecrease.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                updateQuantityDisplay();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            if (selectedQuantity < product.getQuantity()) {
                selectedQuantity++;
                updateQuantityDisplay();
            } else {
                Toast.makeText(this, "Only " + product.getQuantity() + " available", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddToCart.setOnClickListener(v -> addToCart());

        btnBuyNow.setOnClickListener(v -> buyNow());
    }

    private void updateQuantityDisplay() {
        tvSelectedQuantity.setText(String.valueOf(selectedQuantity));

        if (selectedQuantity == product.getQuantity()) {
            btnIncrease.setEnabled(false);
            btnIncrease.setAlpha(0.5f);
        } else {
            btnIncrease.setEnabled(true);
            btnIncrease.setAlpha(1f);
        }

        if (selectedQuantity == 1) {
            btnDecrease.setEnabled(false);
            btnDecrease.setAlpha(0.5f);
        } else {
            btnDecrease.setEnabled(true);
            btnDecrease.setAlpha(1f);
        }
    }

    private void addToCart() {
        if (product == null) return;

        cartManager.addToCart(product, selectedQuantity, new CartManager.OnCartActionListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(ProductDetailActivity.this,
                        selectedQuantity + " x " + product.getName() + " added to cart",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buyNow() {
        if (product == null) return;

        // Add to cart first
        cartManager.addToCart(product, selectedQuantity, new CartManager.OnCartActionListener() {
            @Override
            public void onSuccess(String message) {
                // Then go to cart/checkout
                Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}