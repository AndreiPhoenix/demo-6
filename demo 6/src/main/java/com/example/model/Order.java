package com.example.model;

import java.math.BigDecimal;

public class Order {
    private String userId;
    private String orderId;
    private BigDecimal amount;
    private String currency;

    public Order(String userId, String orderId, BigDecimal amount, String currency) {
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
    }

    // getters and setters
    public String getUserId() { return userId; }
    public String getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}