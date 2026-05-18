package com.example.farmlink.adapters;



import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.R;
import com.example.farmlink.activities.farmer.EditProductActivity;
import com.example.farmlink.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;
    private Context context;
    private boolean isFarmerView = false;

    public interface OnProductClickListener {
        void onAddToCart(Product product);
        void onProductClick(Product product);
    }

    // Constructor for Student view
    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
        this.isFarmerView = false;
    }

    // Constructor for Farmer view
    public ProductAdapter(List<Product> products, boolean isFarmerView) {
        this.products = products;
        this.isFarmerView = isFarmerView;
        this.listener = null;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        // Set basic product info
        holder.tvEmoji.setText(product.getEmoji());
        holder.tvName.setText(product.getName());
        holder.tvFarmer.setText("👨‍🌾 " + product.getFarmerName());
        holder.tvPrice.setText("R" + product.getPrice() + "/" + product.getUnit());
        holder.tvQuantity.setText(product.getQuantity() + " " + product.getUnit() + " available");

        // Set stock badge
        if (product.getQuantity() <= 0) {
            holder.tvStockBadge.setText("❌ Sold Out");
            holder.tvStockBadge.setBackgroundColor(context.getColor(R.color.sold_out_red));
            holder.tvStockBadge.setTextColor(context.getColor(android.R.color.white));
            holder.btnAddToCart.setEnabled(false);
        } else if (product.getQuantity() < 10) {
            holder.tvStockBadge.setText("⚠️ Low Stock");
            holder.tvStockBadge.setBackgroundColor(context.getColor(R.color.low_stock_yellow));
            holder.tvStockBadge.setTextColor(context.getColor(android.R.color.black));
            holder.btnAddToCart.setEnabled(true);
        } else {
            holder.tvStockBadge.setText("✓ In Stock");
            holder.tvStockBadge.setBackgroundColor(context.getColor(R.color.in_stock_green));
            holder.tvStockBadge.setTextColor(context.getColor(R.color.farmer_green));
            holder.btnAddToCart.setEnabled(true);
        }

        // Handle button visibility and clicks based on view type
        if (isFarmerView) {
            // Farmer view: Show "Edit" instead of "Add to Cart"
            holder.btnAddToCart.setText("✏️ Edit");
            holder.btnAddToCart.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("product_id", product.getProductId());
                context.startActivity(intent);
            });

            // Item click also opens edit
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("product_id", product.getProductId());
                context.startActivity(intent);
            });
        } else {
            // Student view: Show "Add to Cart"
            holder.btnAddToCart.setText("🛒 Add to Cart");
            holder.btnAddToCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCart(product);
                }
            });

            // Item click for product detail
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvFarmer, tvPrice, tvStockBadge, tvQuantity;
        Button btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvName = itemView.findViewById(R.id.tvName);
            tvFarmer = itemView.findViewById(R.id.tvFarmer);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStockBadge = itemView.findViewById(R.id.tvStockBadge);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}