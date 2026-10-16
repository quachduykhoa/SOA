package org.example.th3.controller;

import org.example.th3.dto.ProductRequest;
import org.example.th3.entity.Product;
import org.example.th3.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. GET /products - lay danh sach
    @GetMapping
    public List<Product> getAll() {
        return productService.getAll();
    }

    // 2. GET /products/{id} - chi tiet
    @GetMapping("/{id}")
    public Product getById(@PathVariable Integer id) {
        return productService.getById(id);
    }

    // 3. POST /products - them moi
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody ProductRequest request) {
        Product created = productService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    // 4. PUT /products/{id} - cap nhat
    @PutMapping("/{id}")
    public Product update(@PathVariable Integer id,
                          @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    // 5. DELETE /products/{id} - xoa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();   // 204
    }
}
