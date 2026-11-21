package org.example.th5.controller;

import org.example.th5.dto.request.OrderReportRequest;
import org.example.th5.dto.request.ProductReportRequest;
import org.example.th5.entity.OrderReport;
import org.example.th5.entity.ProductReport;
import org.example.th5.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    // Inject ReportService qua constructor
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // GET /reports/orders
    @GetMapping("/orders")
    public List<OrderReport> getAllOrderReports() {
        return reportService.getAllOrderReport();
    }

    // GET /reports/orders/{id}
    @GetMapping("/orders/{id}")
    public OrderReport getOrderReportById(@PathVariable Integer id) {
        return reportService.getOrderReportById(id);
    }

    // POST /reports/orders
    @PostMapping("/orders")
    public ResponseEntity<OrderReport> createOrderReport(
            @RequestBody OrderReportRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        OrderReport created = reportService.createOrderReport(request.getOrderId(), token);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // DELETE /reports/orders/{id}
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrderReport(@PathVariable Integer id) {
        reportService.deleteOrderReport(id);
        return ResponseEntity.noContent().build();
    }

    // GET /reports/products
    @GetMapping("/products")
    public List<ProductReport> getAllProductReports() {
        return reportService.getAllProductReports();
    }

    // GET /reports/products/{id}
    @GetMapping("/products/{id}")
    public ProductReport getProductReportById(@PathVariable Integer id) {
        return reportService.getProductReportById(id);
    }

    // POST /reports/products
    @PostMapping("/products")
    public ResponseEntity<ProductReport> createProductReport(@RequestBody ProductReportRequest request) {
        ProductReport created = reportService.createProductReport(
                request.getOrderReportId(),
                request.getProductId(),
                request.getTotalSold(),
                request.getRevenue(),
                request.getCost()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // DELETE /reports/products/{id}
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProductReport(@PathVariable Integer id) {
        reportService.deleteProductReport(id);
        return ResponseEntity.noContent().build();
    }
}