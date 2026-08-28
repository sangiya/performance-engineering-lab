package com.sangiya.perf.order;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(repository, new SimpleMeterRegistry(), 0);
    }

    @Test
    void placeOrder_calculatesTotalAndPersists() {
        OrderRequest request = new OrderRequest("cust-1", "prod-A", 3, new BigDecimal("10.00"));
        Order saved = new Order("cust-1", "prod-A", 3, new BigDecimal("30.00"));
        saved.setStatus(OrderStatus.PENDING);
        when(repository.save(any(Order.class))).thenReturn(saved);

        Order result = service.placeOrder(request);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("30.00");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(repository).save(any(Order.class));
    }

    @Test
    void findById_delegatesToRepository() {
        Order order = new Order("c1", "p1", 1, BigDecimal.TEN);
        when(repository.findById(42L)).thenReturn(Optional.of(order));

        Optional<Order> result = service.findById(42L);

        assertThat(result).isPresent();
    }

    @Test
    void findById_returnsEmptyWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.findById(99L)).isEmpty();
    }

    @Test
    void findByCustomer_returnsAll() {
        Order o1 = new Order("c2", "p1", 1, BigDecimal.ONE);
        Order o2 = new Order("c2", "p2", 2, BigDecimal.TEN);
        when(repository.findByCustomerId("c2")).thenReturn(List.of(o1, o2));

        List<Order> results = service.findByCustomer("c2");

        assertThat(results).hasSize(2);
    }

    @Test
    void confirmOrder_updatesStatus() {
        Order order = new Order("c3", "p3", 1, BigDecimal.ONE);
        order.setStatus(OrderStatus.PENDING);
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);

        Optional<Order> result = service.confirmOrder(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void cancelOrder_updatesStatus() {
        Order order = new Order("c4", "p4", 1, BigDecimal.ONE);
        when(repository.findById(2L)).thenReturn(Optional.of(order));
        when(repository.save(order)).thenReturn(order);

        Optional<Order> result = service.cancelOrder(2L);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
