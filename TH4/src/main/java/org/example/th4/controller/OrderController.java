package org.example.th4.controller;

import org.example.th4.dto.request.OrderRequest;
import org.example.th4.dto.reponse.OrderResponse;
import org.example.th4.dto.request.StatusRequest;
import org.example.th4.entity.Order;
import org.example.th4.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @GetMapping
    public List<Order> getAll() { return orderService.getAll(); }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Integer id) { return orderService.getById(id); }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest request,
                                                @RequestHeader("Authorization") String auth) {
        OrderResponse created = orderService.create(request, auth.substring(7));
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public Order updateStatus(@PathVariable Integer id, @RequestBody StatusRequest request) {
        return orderService.updateStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();   // 204
    }
}