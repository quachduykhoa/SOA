package org.example.th4.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class OrderRequest {
    private String customerName;
    private String customerEmail;
    private List<Item> items;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Item {
        private Integer productId;
        private Integer quantity;
    }
}
