package org.example.th5.client;

import org.example.th5.dto.OrderDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${order.service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public OrderDto getOrder(Integer orderId, String token) {
        try {
            return restClient.get()
                    .uri("/orders/{id}", orderId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(OrderDto.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Don hang id=" + orderId + " khong ton tai");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Dich vu don hang tra loi " + e.getStatusCode());
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Khong ket noi duoc dich vu don hang");
        }
    }
}