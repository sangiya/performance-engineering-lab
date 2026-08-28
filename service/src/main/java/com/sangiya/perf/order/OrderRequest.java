package com.sangiya.perf.order;

import java.math.BigDecimal;

public record OrderRequest(String customerId, String productId, int quantity, BigDecimal unitPrice) {}
