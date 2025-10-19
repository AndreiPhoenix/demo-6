package com.example.service;

import com.example.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    // Имитация получения данных заказа из внешнего источника
    public CompletableFuture<Order> getOrderByUserId(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Получение заказа пользователя {} в потоке {}",
                    userId, Thread.currentThread().getName());

            // Имитация задержки сети/БД
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Прервано получение заказа для " + userId, e);
            }

            // Имитация данных из БД/API
            BigDecimal amount = new BigDecimal("100.50").multiply(
                    new BigDecimal(userId.replace("user", ""))
            );
            String currency = "USD";

            return new Order(userId, "order-" + userId, amount, currency);
        });
    }
}