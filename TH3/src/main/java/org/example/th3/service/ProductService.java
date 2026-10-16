package org.example.th3.service;

import org.example.th3.dto.ProductRequest;
import org.example.th3.entity.Product;
import org.example.th3.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // GET /products
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // GET /products/{id}
    public Product getById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Khong tim thay san pham id=" + id));
    }

    // POST /products
    public Product create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        // created_at / updated_at tu @PrePersist set cho
        return productRepository.save(product);
    }

    // PUT /products/{id}
    public Product update(Integer id, ProductRequest request) {
        Product product = getById(id);          // khong co -> 404
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        // updated_at tu @PreUpdate set cho
        return productRepository.save(product);
    }

    // DELETE /products/{id}
    public void delete(Integer id) {
        Product product = getById(id);          // khong co -> 404
        productRepository.delete(product);
    }
}
