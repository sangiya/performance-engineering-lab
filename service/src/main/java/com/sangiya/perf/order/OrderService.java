package com.sangiya.perf.order;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final int artificialDelayMs;

    private final Counter ordersPlaced;
    private final Counter ordersFailed;
    private final Timer orderPlacementTimer;

    public OrderService(
            OrderRepository repository,
            MeterRegistry meterRegistry,
            @Value("${perf.artificial-delay-ms:0}") int artificialDelayMs) {
        this.repository = repository;
        this.artificialDelayMs = artificialDelayMs;

        this.ordersPlaced = Counter.builder("orders.placed.total")
                .description("Total orders placed successfully")
                .register(meterRegistry);
        this.ordersFailed = Counter.builder("orders.failed.total")
                .description("Total order placement failures")
                .register(meterRegistry);
        this.orderPlacementTimer = Timer.builder("orders.placement.duration")
                .description("Time taken to place an order")
                .register(meterRegistry);
    }

    @Transactional
    public Order placeOrder(OrderRequest request) {
        return orderPlacementTimer.record(() -> {
            try {
                if (artificialDelayMs > 0) {
                    Thread.sleep(artificialDelayMs);
                }
                BigDecimal total = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
                Order order = new Order(request.customerId(), request.productId(), request.quantity(), total);
                Order saved = repository.save(order);
                ordersPlaced.increment();
                return saved;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new OrderPlacementException("Interrupted during order placement", e);
            } catch (Exception e) {
                ordersFailed.increment();
                log.error("Order placement failed: {}", e.getMessage());
                throw e;
            }
        });
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomer(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Transactional
    public Optional<Order> confirmOrder(Long id) {
        return repository.findById(id).map(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            return repository.save(order);
        });
    }

    @Transactional
    public Optional<Order> cancelOrder(Long id) {
        return repository.findById(id).map(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            return repository.save(order);
        });
    }
}
