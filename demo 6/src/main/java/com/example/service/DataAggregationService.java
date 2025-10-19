package com.example.service;

import com.example.config.AsyncConfig;
import com.example.model.FinalResult;
import com.example.model.User;
import com.example.model.Order;
import com.example.model.CurrencyRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class DataAggregationService {
    private static final Logger logger = LoggerFactory.getLogger(DataAggregationService.class);

    private final UserService userService;
    private final OrderService orderService;
    private final CurrencyService currencyService;
    private final ExecutorService executor;

    public DataAggregationService(AsyncConfig asyncConfig) {
        this.userService = new UserService();
        this.orderService = new OrderService();
        this.currencyService = new CurrencyService();
        this.executor = asyncConfig.getExecutor();
    }

    // Основной метод для асинхронного получения и агрегации данных
    public List<FinalResult> fetchAllDataAsync(List<String> userIds) {
        logger.info("Начало асинхронной обработки {} пользователей", userIds.size());

        long startTime = System.currentTimeMillis();

        // Получаем курс валют один раз для всех пользователей
        CompletableFuture<CurrencyRate> currencyRateFuture = currencyService.getCurrencyRate("USD", "EUR")
                .exceptionally(throwable -> {
                    logger.warn("Ошибка получения курса валют, используем курс по умолчанию: {}", throwable.getMessage());
                    return new CurrencyRate("USD", "EUR", new BigDecimal("0.85")); // Fallback
                });

        // Создаем асинхронные задачи для каждого пользователя
        List<CompletableFuture<FinalResult>> futures = userIds.stream()
                .map(userId -> {
                    // Асинхронно получаем данные пользователя с обработкой ошибок
                    CompletableFuture<User> userFuture = userService.getUserById(userId)
                            .handle((user, throwable) -> {
                                if (throwable != null) {
                                    logger.error("Ошибка получения пользователя {}: {}", userId, throwable.getMessage());
                                    // Возвращаем fallback пользователя или null в зависимости от логики
                                    return new User(userId, "Unknown User", "unknown@example.com");
                                }
                                return user;
                            });

                    // Асинхронно получаем заказ пользователя с обработкой ошибок
                    CompletableFuture<Order> orderFuture = orderService.getOrderByUserId(userId)
                            .handle((order, throwable) -> {
                                if (throwable != null) {
                                    logger.error("Ошибка получения заказа для {}: {}", userId, throwable.getMessage());
                                    // Fallback заказ
                                    return new Order(userId, "unknown-order", BigDecimal.ZERO, "USD");
                                }
                                return order;
                            });

                    // Комбинируем результаты: пользователь + заказ
                    CompletableFuture<FinalResult> combinedUserOrder = userFuture.thenCombineAsync(
                            orderFuture,
                            (user, order) -> {
                                logger.info("Комбинирование данных пользователя {} и заказа {}", user.getId(), order.getOrderId());
                                return new FinalResult(
                                        user.getId(),
                                        user.getName(),
                                        order.getOrderId(),
                                        order.getAmount(),
                                        order.getCurrency(),
                                        null, // convertedAmount будет вычислен позже
                                        null  // targetCurrency будет установлен позже
                                );
                            },
                            executor
                    );

                    // Комбинируем с курсом валют и применяем преобразование
                    return combinedUserOrder.thenCombineAsync(
                            currencyRateFuture,
                            (finalResult, currencyRate) -> {
                                // Вычисляем конвертированную сумму
                                BigDecimal convertedAmount = finalResult.getOriginalAmount()
                                        .multiply(currencyRate.getRate());

                                logger.info("Конвертация суммы для пользователя {}: {} {} -> {} {}",
                                        finalResult.getUserId(),
                                        finalResult.getOriginalAmount(), finalResult.getOriginalCurrency(),
                                        convertedAmount, currencyRate.getToCurrency());

                                return new FinalResult(
                                        finalResult.getUserId(),
                                        finalResult.getUserName(),
                                        finalResult.getOrderId(),
                                        finalResult.getOriginalAmount(),
                                        finalResult.getOriginalCurrency(),
                                        convertedAmount,
                                        currencyRate.getToCurrency()
                                );
                            },
                            executor
                    );
                })
                .collect(Collectors.toList());

        // Ожидаем завершения всех задач
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // Собираем результаты
        List<FinalResult> results = allFutures
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join) // Безопасно, т.к. все задачи завершены
                        .collect(Collectors.toList()))
                .join();

        long endTime = System.currentTimeMillis();
        logger.info("Асинхронная обработка завершена за {} мс. Обработано результатов: {}",
                (endTime - startTime), results.size());

        return results;
    }

    // Синхронная версия для сравнения производительности
    public List<FinalResult> fetchAllDataSync(List<String> userIds) {
        logger.info("Начало синхронной обработки {} пользователей", userIds.size());

        long startTime = System.currentTimeMillis();

        // Получаем курс валют
        CurrencyRate currencyRate;
        try {
            currencyRate = currencyService.getCurrencyRate("USD", "EUR").join();
        } catch (Exception e) {
            logger.warn("Ошибка получения курса валют, используем курс по умолчанию");
            currencyRate = new CurrencyRate("USD", "EUR", new BigDecimal("0.85"));
        }

        CurrencyRate finalCurrencyRate = currencyRate;
        List<FinalResult> results = userIds.stream()
                .map(userId -> {
                    try {
                        // Синхронное получение данных
                        User user = userService.getUserById(userId).join();
                        Order order = orderService.getOrderByUserId(userId).join();

                        // Вычисление конвертированной суммы
                        BigDecimal convertedAmount = order.getAmount().multiply(finalCurrencyRate.getRate());

                        return new FinalResult(
                                user.getId(),
                                user.getName(),
                                order.getOrderId(),
                                order.getAmount(),
                                order.getCurrency(),
                                convertedAmount,
                                finalCurrencyRate.getToCurrency()
                        );
                    } catch (Exception e) {
                        logger.error("Ошибка обработки пользователя {}: {}", userId, e.getMessage());
                        // Fallback результат
                        return new FinalResult(
                                userId,
                                "Error User",
                                "error-order",
                                BigDecimal.ZERO,
                                "USD",
                                BigDecimal.ZERO,
                                "EUR"
                        );
                    }
                })
                .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        logger.info("Синхронная обработка завершена за {} мс. Обработано результатов: {}",
                (endTime - startTime), results.size());

        return results;
    }
}