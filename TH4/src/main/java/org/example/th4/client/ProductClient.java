package org.example.th4.client;

import org.example.th4.dto.ProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${product.service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ProductDto getProduct(Integer productId, String token) {
        try {
            return restClient.get()
                    .uri("/products/{id}", productId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(ProductDto.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "San pham id=" + productId + " khong ton tai");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Dich vu san pham tra loi " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Khong ket noi duoc dich vu san pham (TH3) o cong 8081");
        }
    }
}