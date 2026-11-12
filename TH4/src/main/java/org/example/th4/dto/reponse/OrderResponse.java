package org.example.th4.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.th4.entity.Order;
import org.example.th4.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor
public class OrderResponse {
    private Integer id;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> items;

    public static OrderResponse of(Order o, List<OrderItem> items) {
        return new OrderResponse(o.getId(), o.getCustomerName(), o.getCustomerEmail(),
                o.getTotalAmount(), o.getStatus(), o.getCreatedAt(), o.getUpdatedAt(), items);
    }
}
