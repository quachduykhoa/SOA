package org.example.th5.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDto {
    private Integer id;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> items;
}
