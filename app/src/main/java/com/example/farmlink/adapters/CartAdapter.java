package com.example.farmlink.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.R;
import com.example.farmlink.models.CartItem;
import com.example.farmlink.utils.CartManager;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;
    private CartManager cartManager;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged();
        void onItemRemoved();
    }

    public CartAdapter(List<CartItem> cartItems, CartManager cartManager, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.cartManager = cartManager;
        this.listener = listener;
    }

    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvEmoji.setText(item.getProductEmoji() != null ? item.getProductEmoji() : "🌾");
        holder.tvName.setText(item.getProductName());
        holder.tvFarmer.setText("👨‍🌾 " + item.getFarmerName());
        holder.tvPrice.setText(String.format("R%.2f/%s", item.getPrice(), item.getUnit()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvSubtotal.setText(String.format("R%.2f", item.getSubtotal()));

        // Increase button
        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            if (newQuantity <= item.getMaxAvailable()) {
                updateQuantity(item, newQuantity, holder.getAdapterPosition());
            } else {
                Toast.makeText(holder.itemView.getContext(),
                        "Only " + item.getMaxAvailable() + " available",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Decrease button
        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            if (newQuantity >= 1) {
                updateQuantity(item, newQuantity, holder.getAdapterPosition());
            } else {
                removeItem(item, holder.getAdapterPosition());
            }
        });

        // Remove button
        holder.btnRemove.setOnClickListener(v -> removeItem(item, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    private void updateQuantity(CartItem item, int newQuantity, int position) {
        cartManager.updateQuantity(item.getProductId(), newQuantity, new CartManager.OnCartActionListener() {
            @Override
            public void onSuccess(String message) {
                item.setQuantity(newQuantity);
                notifyItemChanged(position);
                if (listener != null) listener.onQuantityChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(null, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeItem(CartItem item, int position) {
        cartManager.removeFromCart(item.getProductId(), new CartManager.OnCartActionListener() {
            @Override
            public void onSuccess(String message) {
                cartItems.remove(position);
                notifyItemRemoved(position);
                if (listener != null) listener.onItemRemoved();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(null, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvFarmer, tvPrice, tvQuantity, tvSubtotal;
        TextView btnIncrease, btnDecrease, btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvName = itemView.findViewById(R.id.tvName);
            tvFarmer = itemView.findViewById(R.id.tvFarmer);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}