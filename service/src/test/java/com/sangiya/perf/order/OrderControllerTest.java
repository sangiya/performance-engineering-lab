package com.sangiya.perf.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest {

    private MockMvc mockMvc;
    private OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OrderController(orderService)).build();
    }

    @Test
    void placeOrder_returns201WithOrder() throws Exception {
        OrderRequest request = new OrderRequest("cust-1", "prod-A", 2, new BigDecimal("15.00"));
        Order order = new Order("cust-1", "prod-A", 2, new BigDecimal("30.00"));

        when(orderService.placeOrder(any())).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("cust-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrder_returns200WhenFound() throws Exception {
        Order order = new Order("cust-2", "prod-B", 1, BigDecimal.TEN);
        when(orderService.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("cust-2"));
    }

    @Test
    void getOrder_returns404WhenNotFound() throws Exception {
        when(orderService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByCustomer_returnsList() throws Exception {
        Order o1 = new Order("cust-3", "p1", 1, BigDecimal.ONE);
        Order o2 = new Order("cust-3", "p2", 2, BigDecimal.TEN);
        when(orderService.findByCustomer("cust-3")).thenReturn(List.of(o1, o2));

        mockMvc.perform(get("/api/orders").param("customerId", "cust-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void confirmOrder_returns200() throws Exception {
        Order order = new Order("c1", "p1", 1, BigDecimal.ONE);
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderService.confirmOrder(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(patch("/api/orders/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelOrder_returns200() throws Exception {
        Order order = new Order("c1", "p1", 1, BigDecimal.ONE);
        order.setStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrder(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
