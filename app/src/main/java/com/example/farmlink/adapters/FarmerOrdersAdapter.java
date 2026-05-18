package com.example.farmlink.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farmlink.R;
import com.example.farmlink.models.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FarmerOrdersAdapter extends RecyclerView.Adapter<FarmerOrdersAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public FarmerOrdersAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders != null ? orders : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_farmer_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders != null ? newOrders : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvDate, tvTotal, tvStatus, tvItemCount;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
        }

        void bind(Order order, OnOrderClickListener listener) {
            // Order ID
            String orderId = order.getOrderId();
            String shortId = orderId.length() > 8 ? orderId.substring(0, 8) : orderId;
            tvOrderId.setText("#" + shortId.toUpperCase());

            // Customer Name
            String customerName = order.getStudentName();
            tvCustomerName.setText(customerName != null ? customerName : "Unknown Customer");

            // Date
            if (order.getDate() != null) {
                tvDate.setText(order.getDate());
            } else if (order.getTimestamp() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                tvDate.setText(sdf.format(new Date(order.getTimestamp())));
            } else {
                tvDate.setText("Unknown date");
            }

            // Total Amount
            tvTotal.setText(String.format("R%.2f", order.getTotalAmount()));

            // Status
            String status = order.getStatus();
            tvStatus.setText(status != null ? status.toUpperCase() : "PENDING");

            // Status colors
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "delivered":
                        tvStatus.setBackgroundColor(tvStatus.getContext().getColor(android.R.color.holo_green_light));
                        break;
                    case "shipped":
                        tvStatus.setBackgroundColor(tvStatus.getContext().getColor(android.R.color.holo_purple));
                        break;
                    case "processing":
                        tvStatus.setBackgroundColor(tvStatus.getContext().getColor(android.R.color.holo_blue_light));
                        break;
                    case "pending":
                        tvStatus.setBackgroundColor(tvStatus.getContext().getColor(android.R.color.holo_orange_light));
                        break;
                    case "cancelled":
                        tvStatus.setBackgroundColor(tvStatus.getContext().getColor(android.R.color.holo_red_light));
                        break;
                    default:
                        tvStatus.setBackgroundColor(tvStatus.getContext().getColor(android.R.color.darker_gray));
                }
            }

            // Item count
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            tvItemCount.setText(itemCount + " item" + (itemCount != 1 ? "s" : ""));

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }
    }
}