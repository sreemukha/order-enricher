package com.teamviewer.orderenricher.client;

import com.teamviewer.orderenricher.api.model.Product;
import com.teamviewer.orderenricher.exception.ResourceNotFoundException;
import com.teamviewer.orderenricher.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductServiceClient client;

    @Test
    void whenProductExists_thenReturnsProduct() {
        ReflectionTestUtils.setField(client, "productServiceUrl", "http://test-url");
        String productId = "PROD-123";
        String url = "http://test-url/products/{id}";
        Product expectedProduct = new Product().id(productId).name("Test Product");

        when(restTemplate.getForObject(url, Product.class, productId)).thenReturn(expectedProduct);

        Product actualProduct = client.getProductById(productId);

        assertThat(actualProduct).isNotNull();
        assertThat(actualProduct.getId()).isEqualTo(productId);
    }

    @Test
    void whenProductNotFound_thenThrowsResourceNotFoundException() {
        ReflectionTestUtils.setField(client, "productServiceUrl", "http://test-url");
        String productId = "PROD-999";
        String url = "http://test-url/products/{id}";

        when(restTemplate.getForObject(url, Product.class, productId))
                .thenThrow(new HttpClientErrorException(NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> {
            client.getProductById(productId);
        });
    }

    @Test
    void whenHttpClientErrorException_thenThrowsServiceUnavailableException() {
        ReflectionTestUtils.setField(client, "productServiceUrl", "http://test-url");
        String productId = "PROD-999";
        String url = "http://test-url/products/{id}";

        when(restTemplate.getForObject(url, Product.class, productId))
                .thenThrow(new HttpClientErrorException(FORBIDDEN));

        assertThrows(ServiceUnavailableException.class, () -> {
            client.getProductById(productId);
        });
    }

    @Test
    void whenServiceIsDown_thenThrowsServiceUnavailableException() {
        ReflectionTestUtils.setField(client, "productServiceUrl", "http://test-url");
        String productId = "PROD-999";
        String url = "http://test-url/products/{id}";

        when(restTemplate.getForObject(url, Product.class, productId))
                .thenThrow(new RestClientException("Service down"));

        assertThrows(ServiceUnavailableException.class, () -> {
            client.getProductById(productId);
        });
    }
}