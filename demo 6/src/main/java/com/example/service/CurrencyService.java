package com.example.service;

import com.example.model.CurrencyRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    // Имитация получения курса валют из внешнего API
    public CompletableFuture<CurrencyRate> getCurrencyRate(String fromCurrency, String toCurrency) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Получение курса валют {}-{} в потоке {}",
                    fromCurrency, toCurrency, Thread.currentThread().getName());

            // Имитация задержки API
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Прервано получение курса валют", e);
            }

            // Имитация данных из API
            BigDecimal rate;
            if ("USD".equals(fromCurrency) && "EUR".equals(toCurrency)) {
                rate = new BigDecimal("0.85");
            } else if ("USD".equals(fromCurrency) && "RUB".equals(toCurrency)) {
                rate = new BigDecimal("90.50");
            } else {
                rate = BigDecimal.ONE; // По умолчанию
            }

            return new CurrencyRate(fromCurrency, toCurrency, rate);
        });
    }
}