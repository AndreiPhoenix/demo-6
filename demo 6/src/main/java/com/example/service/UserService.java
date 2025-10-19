package com.example.service;

import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // Имитация получения данных пользователя из внешнего источника
    public CompletableFuture<User> getUserById(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Получение данных пользователя {} в потоке {}",
                    userId, Thread.currentThread().getName());

            // Имитация задержки сети/БД
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Прервано получение пользователя " + userId, e);
            }

            // Имитация ошибки для определенного пользователя
            if ("user3".equals(userId)) {
                throw new RuntimeException("Пользователь " + userId + " не найден в системе");
            }

            // Имитация данных из БД/API
            return new User(userId, "User-" + userId, userId + "@example.com");
        });
    }
}