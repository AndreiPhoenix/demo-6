package com.example.model;

import java.math.BigDecimal;

public class FinalResult {
    private String userId;
    private String userName;
    private String orderId;
    private BigDecimal originalAmount;
    private String originalCurrency;
    private BigDecimal convertedAmount;
    private String targetCurrency;

    public FinalResult(String userId, String userName, String orderId,
                       BigDecimal originalAmount, String originalCurrency,
                       BigDecimal convertedAmount, String targetCurrency) {
        this.userId = userId;
        this.userName = userName;
        this.orderId = orderId;
        this.originalAmount = originalAmount;
        this.originalCurrency = originalCurrency;
        this.convertedAmount = convertedAmount;
        this.targetCurrency = targetCurrency;
    }

    // getters and setters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getOrderId() { return orderId; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public String getOriginalCurrency() { return originalCurrency; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public String getTargetCurrency() { return targetCurrency; }

    @Override
    public String toString() {
        return String.format("FinalResult{userId='%s', userName='%s', orderId='%s', " +
                        "originalAmount=%s %s, convertedAmount=%s %s}",
                userId, userName, orderId, originalAmount, originalCurrency,
                convertedAmount, targetCurrency);
    }
}