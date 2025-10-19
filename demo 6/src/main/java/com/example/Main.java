package com.example;

import com.example.config.AsyncConfig;
import com.example.service.DataAggregationService;
import com.example.model.FinalResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Запуск приложения для демонстрации CompletableFuture");

        AsyncConfig asyncConfig = new AsyncConfig();
        DataAggregationService service = new DataAggregationService(asyncConfig);

        try {
            // Список идентификаторов для обработки
            List<String> userIds = List.of("user1", "user2", "user3", "user4", "user5");

            logger.info("=== АСИНХРОННАЯ ОБРАБОТКА ===");
            List<FinalResult> asyncResults = service.fetchAllDataAsync(userIds);
            asyncResults.forEach(result ->
                    logger.info("Асинхронный результат: {}", result)
            );

            // Небольшая пауза между тестами
            Thread.sleep(1000);

            logger.info("=== СИНХРОННАЯ ОБРАБОТКА ===");
            List<FinalResult> syncResults = service.fetchAllDataSync(userIds);
            syncResults.forEach(result ->
                    logger.info("Синхронный результат: {}", result)
            );

        } catch (Exception e) {
            logger.error("Ошибка в главном методе: {}", e.getMessage(), e);
        } finally {
            // Корректное завершение ExecutorService
            asyncConfig.shutdown();
            logger.info("Приложение завершено");
        }
    }
}