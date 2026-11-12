package org.example.th4.service;

import org.example.th4.client.ProductClient;
import org.example.th4.dto.*;
import org.example.th4.dto.reponse.OrderResponse;
import org.example.th4.dto.request.OrderItemRequest;
import org.example.th4.dto.request.OrderRequest;
import org.example.th4.entity.Order;
import org.example.th4.entity.OrderItem;
import org.example.th4.repository.OrderItemRepository;
import org.example.th4.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class OrderService {

    private static final Set<String> VALID_STATUS = Set.of("pending", "completed", "cancelled");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productClient = productClient;
    }

    // ---------- ORDERS ----------

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public OrderResponse getById(Integer id) {
        Order order = findOrder(id);
        return OrderResponse.of(order, orderItemRepository.findByOrderId(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request, String token) {
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerName khong duoc rong");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Don hang phai co it nhat 1 mat hang");
        }

        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order = orderRepository.save(order);      // cần id trước khi tạo order_items

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.Item line : request.getItems()) {
            int qty = line.getQuantity() == null ? 0 : line.getQuantity();
            if (qty <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity phai > 0");
            }

            // ① GỌI SANG TH3 ĐỂ KIỂM TRA TỒN KHO
            ProductDto product = productClient.getProduct(line.getProductId(), token);

            // ② KHÔNG ĐỦ HÀNG -> CHẶN NGAY
            if (product.getQuantity() == null || product.getQuantity() < qty) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "San pham '" + product.getName() + "' chi con " + product.getQuantity());
            }

            OrderItem item = buildItem(order.getId(), product, qty);
            items.add(orderItemRepository.save(item));
            total = total.add(item.getTotalPrice());
        }

        order.setTotalAmount(total);
        return OrderResponse.of(orderRepository.save(order), items);
    }

    @Transactional
    public Order updateStatus(Integer id, String status) {
        Order order = findOrder(id);
        if (status == null || !VALID_STATUS.contains(status.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "status chi duoc la pending / completed / cancelled");
        }
        order.setStatus(status.toLowerCase());
        return orderRepository.save(order);
    }

    @Transactional
    public void delete(Integer id) {
        findOrder(id);
        orderItemRepository.deleteByOrderId(id);   // xoá con trước
        orderRepository.deleteById(id);            // rồi xoá cha
    }

    // ---------- ORDER ITEMS ----------

    public List<OrderItem> getAllItems() {
        return orderItemRepository.findAll();
    }

    public OrderItem getItem(Integer id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Khong tim thay order_item id=" + id));
    }

    @Transactional
    public OrderItem createItem(OrderItemRequest request, String token) {
        Order order = findOrder(request.getOrderId());

        int qty = request.getQuantity() == null ? 0 : request.getQuantity();
        if (qty <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity phai > 0");
        }

        ProductDto product = productClient.getProduct(request.getProductId(), token);
        if (product.getQuantity() == null || product.getQuantity() < qty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "San pham '" + product.getName() + "' chi con " + product.getQuantity());
        }

        OrderItem item = buildItem(order.getId(), product, qty);
        item = orderItemRepository.save(item);
        recalcTotal(order.getId());
        return item;
    }

    @Transactional
    public OrderItem updateItem(Integer id, OrderItemRequest request, String token) {
        OrderItem item = getItem(id);

        int qty = request.getQuantity() == null ? 0 : request.getQuantity();
        if (qty <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity phai > 0");
        }

        ProductDto product = productClient.getProduct(item.getProductId(), token);
        if (product.getQuantity() == null || product.getQuantity() < qty) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "San pham '" + product.getName() + "' chi con " + product.getQuantity());
        }

        item.setQuantity(qty);
        item.setUnitPrice(product.getPrice());
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        item = orderItemRepository.save(item);

        recalcTotal(item.getOrderId());
        return item;
    }

    @Transactional
    public void deleteItem(Integer id) {
        OrderItem item = getItem(id);
        orderItemRepository.delete(item);
        recalcTotal(item.getOrderId());
    }

    // ---------- HÀM PHỤ ----------

    private Order findOrder(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Khong tim thay don hang id=" + id));
    }

    private OrderItem buildItem(Integer orderId, ProductDto product, int qty) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setQuantity(qty);
        item.setUnitPrice(product.getPrice());
        item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        return item;
    }

    private void recalcTotal(Integer orderId) {
        BigDecimal total = orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItem::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = findOrder(orderId);
        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}