package com.restaurant.server.models;

import java.util.List;

public class Order {
    private String customerName;
    private String customerPhone;
    private List<OrderItem> items;

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Object getOrderId() {
        return orderId;
    }

    public Object getStatus() {
        return status;
    }

    public enum Status {
        pending,done,cancelled
    }
    private int orderId;
    private Status status;
    // Getters and Setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public boolean isPending() {
        return status == Status.pending;
    }
    public static class OrderItem {
        private int itemId;
        private int quantity;

        // Getters and Setters
        public int getItemId() { return itemId; }
        public void setItemId(int itemId) { this.itemId = itemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}