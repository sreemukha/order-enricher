package com.teamviewer.orderenricher.client;

import com.teamviewer.orderenricher.api.model.Customer;
import com.teamviewer.orderenricher.exception.ResourceNotFoundException;
import com.teamviewer.orderenricher.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerServiceClient {
    private final RestTemplate restTemplate;
    private final String customerServiceUrl;

    public CustomerServiceClient(RestTemplate restTemplate,
                                 @Value("${clients.customer-service.url}") String customerServiceUrl) {
        this.restTemplate = restTemplate;
        this.customerServiceUrl = customerServiceUrl;
    }

    public Customer getCustomerById(String customerId) {
        String url = customerServiceUrl + "/customers/{id}";
        try {
            return restTemplate.getForObject(url, Customer.class, customerId);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Customer with ID '" + customerId + "' not found.");
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Customer service is currently unavailable.", ex);
        }
    }
}