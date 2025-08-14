package com.teamviewer.orderenricher.client;

import com.teamviewer.orderenricher.api.model.Product;
import com.teamviewer.orderenricher.exception.ResourceNotFoundException;
import com.teamviewer.orderenricher.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductServiceClient {
    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate,
                                @Value("${clients.product-service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public Product getProductById(String productId) {
        String url = productServiceUrl + "/products/{id}";
        try {
            return restTemplate.getForObject(url, Product.class, productId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Product with ID '" + productId + "' not found.");
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Product service is currently unavailable.", ex);
        }
    }
}