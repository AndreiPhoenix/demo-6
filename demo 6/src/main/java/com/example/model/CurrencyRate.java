package com.example.model;

import java.math.BigDecimal;

public class CurrencyRate {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;

    public CurrencyRate(String fromCurrency, String toCurrency, BigDecimal rate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
    }

    // getters and setters
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public BigDecimal getRate() { return rate; }
}