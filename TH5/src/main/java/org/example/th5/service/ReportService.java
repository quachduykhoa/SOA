package org.example.th5.service;

import jakarta.transaction.Transactional;
import org.example.th5.client.OrderClient;
import org.example.th5.client.ProductClient;
import org.example.th5.dto.OrderDto;
import org.example.th5.dto.OrderItemDto;
import org.example.th5.dto.ProductDto;
import org.example.th5.entity.OrderReport;
import org.example.th5.entity.ProductReport;
import org.example.th5.repository.OrderReportRepository;
import org.example.th5.repository.ProductReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final OrderReportRepository orderReportRepo;
    private final ProductReportRepository productReportRepo;
    private final OrderClient orderClient;
    private final ProductClient productClient;

    public ReportService(OrderReportRepository orderReportRepo,
                         ProductReportRepository productReportRepo,
                         OrderClient orderClient,
                         ProductClient productClient) {
        this.orderReportRepo = orderReportRepo;
        this.productReportRepo = productReportRepo;
        this.orderClient = orderClient;
        this.productClient = productClient;
    }

    // GET /reports/orders
    public List<OrderReport> getAllOrderReport(){
        return orderReportRepo.findAll();
    }

    // GET /reports/orders/{id}
    public OrderReport getOrderReportById(Integer id){
        return orderReportRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Khong tim thay bao cao don hang id=" + id
                        )
                );
    }

    // GET /reports/products
    public List<ProductReport> getAllProductReports() {
        return productReportRepo.findAll();
    }
    // GET /reports/products/{id}
    public ProductReport getProductReportById(Integer id) {
        return productReportRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Khong tim thay bao cao san pham id=" + id));
    }

    // DELETE /reports/orders/{id}
    public void deleteOrderReport(Integer id) {
        getOrderReportById(id);  // kiểm tra tồn tại
        orderReportRepo.deleteById(id);
    }
    // DELETE /reports/products/{id}
    public void deleteProductReport(Integer id) {
        getProductReportById(id);
        productReportRepo.deleteById(id);
    }

    // POST /reports/orders
    @Transactional
    public OrderReport createOrderReport(Integer orderId, String token) {

        // ① Gọi sang TH4 lấy thông tin đơn hàng
        OrderDto order = orderClient.getOrder(orderId, token);

        // ② Tạo OrderReport
        OrderReport report = new OrderReport();
        report.setOrderId(orderId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<ProductReport> productReports = new ArrayList<>();

        // ③ Duyệt từng item trong đơn hàng
        for (OrderItemDto item : order.getItems()) {

            // Gọi sang TH3 lấy giá gốc (cost) của sản phẩm
            // Ở đây dùng price từ Product làm cost (giá nhập)
            // còn unitPrice trong OrderItem là giá bán
            ProductDto product = productClient.getProduct(item.getProductId(), token);

            ProductReport pr = new ProductReport();
            pr.setProductId(item.getProductId());
            pr.setTotalSold(item.getQuantity());

            // revenue = unitPrice * quantity (giá bán × số lượng)
            BigDecimal revenue = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            pr.setRevenue(revenue);

            // cost = giá nhập × số lượng
            // (tuỳ nghiệp vụ, có thể dùng product.getPrice() làm giá nhập)
            BigDecimal cost = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            pr.setCost(cost);

            // profit tự tính qua @PrePersist
            pr.setOrderReport(report);  // liên kết FK
            productReports.add(pr);

            totalRevenue = totalRevenue.add(revenue);
            totalCost = totalCost.add(cost);
        }

        // ④ Set tổng vào OrderReport
        report.setTotalRevenue(totalRevenue);
        report.setTotalCost(totalCost);
        // totalProfit tự tính qua @PrePersist

        report.setProductReports(productReports);

        // ⑤ Lưu (cascade sẽ lưu luôn ProductReport)
        return orderReportRepo.save(report);
    }

    // POST /reports/products
    @Transactional
    public ProductReport createProductReport(Integer orderReportId,
                                             Integer productId,
                                             Integer totalSold,
                                             BigDecimal revenue,
                                             BigDecimal cost) {
        OrderReport orderReport = getOrderReportById(orderReportId);

        ProductReport pr = new ProductReport();
        pr.setOrderReport(orderReport);
        pr.setProductId(productId);
        pr.setTotalSold(totalSold);
        pr.setRevenue(revenue);
        pr.setCost(cost);
        // profit tự tính qua @PrePersist

        return productReportRepo.save(pr);
    }


}
