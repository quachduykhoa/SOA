package org.example.th4.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderItemRequest {
    private Integer orderId;
    private Integer productId;
    private Integer quantity;
}