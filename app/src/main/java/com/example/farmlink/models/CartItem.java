package com.example.farmlink.models;

public class CartItem {
    private String cartItemId;
    private String productId;
    private String farmerId;
    private String farmerName;
    private String productName;
    private String productEmoji;
    private double price;
    private int quantity;
    private String unit;
    private int maxAvailable; // Max stock available from farmer
    private long addedTimestamp;

    // Required empty constructor for Firestore
    public CartItem() {}

    // Full constructor
    public CartItem(String cartItemId, String productId, String farmerId, String farmerName,
                    String productName, String productEmoji, double price, int quantity,
                    String unit, int maxAvailable, long addedTimestamp) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.farmerId = farmerId;
        this.farmerName = farmerName;
        this.productName = productName;
        this.productEmoji = productEmoji;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
        this.maxAvailable = maxAvailable;
        this.addedTimestamp = addedTimestamp;
    }

    // Convenience constructor for quick creation
    public CartItem(Product product, int quantity) {
        this.productId = product.getProductId();
        this.farmerId = product.getFarmerId();
        this.farmerName = product.getFarmerName();
        this.productName = product.getName();
        this.productEmoji = getEmojiForProduct(product.getName());
        this.price = product.getPrice();
        this.quantity = quantity;
        this.unit = product.getUnit();
        this.maxAvailable = product.getQuantity();
        this.addedTimestamp = System.currentTimeMillis();
    }

    // Helper to get emoji based on product name
    private String getEmojiForProduct(String productName) {
        switch (productName.toLowerCase()) {
            case "maize": return "🌽";
            case "potatoes": return "🥔";
            case "wheat": return "🌾";
            case "soybeans": return "🌱";
            case "tomatoes": return "🍅";
            default: return "🥬";
        }
    }

    // Calculate subtotal for this item
    public double getSubtotal() {
        return price * quantity;
    }

    // Check if quantity is valid
    public boolean isValidQuantity() {
        return quantity > 0 && quantity <= maxAvailable;
    }

    // Increase quantity (with max limit)
    public boolean increaseQuantity() {
        if (quantity < maxAvailable) {
            quantity++;
            return true;
        }
        return false;
    }

    // Decrease quantity
    public boolean decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
            return true;
        }
        return false;
    }

    // Getters and Setters
    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getFarmerId() { return farmerId; }
    public void setFarmerId(String farmerId) { this.farmerId = farmerId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductEmoji() { return productEmoji; }
    public void setProductEmoji(String productEmoji) { this.productEmoji = productEmoji; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getMaxAvailable() { return maxAvailable; }
    public void setMaxAvailable(int maxAvailable) { this.maxAvailable = maxAvailable; }

    public long getAddedTimestamp() { return addedTimestamp; }
    public void setAddedTimestamp(long addedTimestamp) { this.addedTimestamp = addedTimestamp; }
}