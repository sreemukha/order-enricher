package com.teamviewer.orderenricher.client;

import com.teamviewer.orderenricher.api.model.Customer;
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
class CustomerServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CustomerServiceClient client;

    @Test
    void whenCustomerExists_thenReturnsCustomer() {
        ReflectionTestUtils.setField(client, "customerServiceUrl", "http://test-url");
        String customerId = "CUST-123";
        String url = "http://test-url/customers/{id}";
        Customer expectedCustomer = new Customer().id(customerId).name("Test Customer");

        when(restTemplate.getForObject(url, Customer.class, customerId)).thenReturn(expectedCustomer);

        Customer actualCustomer = client.getCustomerById(customerId);

        assertThat(actualCustomer).isNotNull();
        assertThat(actualCustomer.getId()).isEqualTo(customerId);
    }

    @Test
    void whenCustomerNotFound_thenThrowsResourceNotFoundException() {
        ReflectionTestUtils.setField(client, "customerServiceUrl", "http://test-url");
        String customerId = "CUST-999";
        String url = "http://test-url/customers/{id}";

        when(restTemplate.getForObject(url, Customer.class, customerId))
                .thenThrow(new HttpClientErrorException(NOT_FOUND));

        assertThrows(ResourceNotFoundException.class, () -> {
            client.getCustomerById(customerId);
        });
    }

    @Test
    void whenHttpClientErrorException_thenThrowsServiceUnavailableException() {
        ReflectionTestUtils.setField(client, "customerServiceUrl", "http://test-url");
        String customerId = "CUST-999";
        String url = "http://test-url/customers/{id}";

        when(restTemplate.getForObject(url, Customer.class, customerId))
                .thenThrow(new HttpClientErrorException(FORBIDDEN));

        assertThrows(ServiceUnavailableException.class, () -> {
            client.getCustomerById(customerId);
        });
    }

    @Test
    void whenServiceIsDown_thenThrowsServiceUnavailableException() {
        ReflectionTestUtils.setField(client, "customerServiceUrl", "http://test-url");
        String customerId = "CUST-123";
        String url = "http://test-url/customers/{id}";

        when(restTemplate.getForObject(url, Customer.class, customerId))
                .thenThrow(new RestClientException("Service down"));

        assertThrows(ServiceUnavailableException.class, () -> {
            client.getCustomerById(customerId);
        });
    }
}