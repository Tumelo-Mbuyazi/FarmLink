package com.example.farmlink.activities.student;




import android.content.Intent;
import android.os.Bundle;
import android.view.View;                    // ← ADD THIS
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.R;
import com.example.farmlink.adapters.CartAdapter;
import com.example.farmlink.models.Cart;
import com.example.farmlink.models.CartItem;
import com.example.farmlink.utils.CartManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;


public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";

    // Views
    private ImageView btnBack;
    private RecyclerView rvCartItems;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private MaterialButton btnCheckout, btnClearCart, btnContinueShopping;
    private ProgressBar progressBar;
    private View emptyCartView, bottomSection;

    // Data
    private CartManager cartManager;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    // Constants
    private static final double DELIVERY_FEE = 25.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_cart);

        cartManager = CartManager.getInstance(this);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCartItems = findViewById(R.id.rvCartItems);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnClearCart = findViewById(R.id.btnClearCart);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        progressBar = findViewById(R.id.progressBar);
        emptyCartView = findViewById(R.id.emptyCartView);
        bottomSection = findViewById(R.id.bottomSection);

        tvDeliveryFee.setText(String.format("R%.2f", DELIVERY_FEE));
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, cartManager, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged() {
                updateTotals();
            }

            @Override
            public void onItemRemoved() {
                updateTotals();
                if (cartItems.isEmpty()) {
                    showEmptyCart(true);
                }
            }
        });
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCheckout.setOnClickListener(v -> proceedToCheckout());

        btnClearCart.setOnClickListener(v -> clearCart());

        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentMarketplaceActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadCart() {
        showProgress(true);

        cartManager.initCart(cart -> {
            if (cart != null) {
                cartItems = cart.getItems();
                if (cartItems == null) cartItems = new ArrayList<>();
            } else {
                cartItems = new ArrayList<>();
            }

            runOnUiThread(() -> {
                cartAdapter.updateItems(cartItems);
                updateTotals();

                if (cartItems.isEmpty()) {
                    showEmptyCart(true);
                } else {
                    showEmptyCart(false);
                }

                showProgress(false);
            });
        });

        // Set cart change listener
        cartManager.setOnCartChangedListener(new CartManager.OnCartChangedListener() {
            @Override
            public void onCartChanged(Cart cart) {
                cartItems = cart.getItems();
                if (cartItems == null) cartItems = new ArrayList<>();
                cartAdapter.updateItems(cartItems);
                updateTotals();
                showEmptyCart(cartItems.isEmpty());
            }

            @Override
            public void onCartItemAdded(CartItem item) {
                loadCart();
            }

            @Override
            public void onCartItemRemoved(String productId) {
                loadCart();
            }

            @Override
            public void onCartItemQuantityChanged(String productId, int newQuantity) {
                updateTotals();
            }
        });
    }

    private void updateTotals() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }

        double total = subtotal + DELIVERY_FEE;

        tvSubtotal.setText(String.format("R%.2f", subtotal));
        tvTotal.setText(String.format("R%.2f", total));
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivity(intent);
    }

    private void clearCart() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear Cart")
                .setMessage("Are you sure you want to remove all items from your cart?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    cartManager.clearCart(new CartManager.OnCartActionListener() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadCart();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(CartActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyCart(boolean empty) {
        if (empty) {
            rvCartItems.setVisibility(View.GONE);
            emptyCartView.setVisibility(View.VISIBLE);
            bottomSection.setVisibility(View.GONE);
            btnClearCart.setVisibility(View.GONE);
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            emptyCartView.setVisibility(View.GONE);
            bottomSection.setVisibility(View.VISIBLE);
            btnClearCart.setVisibility(View.VISIBLE);
        }
    }
}