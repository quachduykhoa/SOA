package org.example.th4.controller;

import org.example.th4.dto.request.OrderItemRequest;
import org.example.th4.entity.OrderItem;
import org.example.th4.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order_items")
public class OrderItemController {

    private final OrderService orderService;

    public OrderItemController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping
    public List<OrderItem> getAll() { return orderService.getAllItems(); }

    @GetMapping("/{id}")
    public OrderItem getById(@PathVariable Integer id) { return orderService.getItem(id); }

    @PostMapping
    public ResponseEntity<OrderItem> create(@RequestBody OrderItemRequest request,
                                            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.status(201).body(orderService.createItem(request, auth.substring(7)));
    }

    @PutMapping("/{id}")
    public OrderItem update(@PathVariable Integer id,
                            @RequestBody OrderItemRequest request,
                            @RequestHeader("Authorization") String auth) {
        return orderService.updateItem(id, request, auth.substring(7));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        orderService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
