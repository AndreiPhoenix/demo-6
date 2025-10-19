package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncConfig {
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    private final ExecutorService executor;

    public AsyncConfig() {
        // Создаем кастомный Executor с именованными демон-потоками
        this.executor = Executors.newFixedThreadPool(10, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("custom-async-" + thread.getId());
            thread.setDaemon(true); // Демон-потоки для корректного завершения приложения
            return thread;
        });

        logger.info("Инициализирован кастомный Executor с 10 потоками");
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    // Метод для корректного завершения ExecutorService
    public void shutdown() {
        logger.info("Завершение ExecutorService...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("ExecutorService завершен");
    }

    // Утилитный метод для асинхронного выполнения с нашим executor
    public <T> CompletableFuture<T> supplyAsync(CompletableFutureSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier::get, executor);
    }

    @FunctionalInterface
    public interface CompletableFutureSupplier<T> {
        T get();
    }
}