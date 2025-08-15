package com.teamviewer.orderenricher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamviewer.orderenricher.api.model.EnrichedOrderResponse;
import com.teamviewer.orderenricher.exception.ResourceNotFoundException;
import com.teamviewer.orderenricher.exception.RestExceptionHandler;
import com.teamviewer.orderenricher.exception.ServiceUnavailableException;
import com.teamviewer.orderenricher.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrdersApiControllerImplTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrdersApiControllerImpl ordersApiController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ordersApiController)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    void whenCreateOrder_thenReturnsCreated() throws Exception {
        // Arrange
        when(orderService.createOrder(any())).thenReturn(new EnrichedOrderResponse());
        String orderRequestJson = "{\"orderId\":\"ORD-123\",\"customerId\":\"CUST-456\",\"productIds\":[\"PROD-A1\"]}";

        // Act & Assert
        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson))
                .andExpect(status().isCreated());
    }

    @Test
    void whenGetOrderById_andOrderExists_thenReturnsOk() throws Exception {
        // Arrange
        String orderId = "ORD-123";
        EnrichedOrderResponse response = new EnrichedOrderResponse().orderId(orderId);
        when(orderService.getOrderById(anyString())).thenReturn(Optional.of(response));

        // Act & Assert
        mockMvc.perform(get("/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    void whenGetOrderById_andOrderNotFound_thenReturnsNoContent() throws Exception {
        // Arrange
        String orderId = "ORD-999";
        when(orderService.getOrderById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/v1/orders/{orderId}", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenGetOrders_thenReturnsOk() throws Exception {
        // Arrange
        when(orderService.getOrders(null, null)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/v1/orders"))
                .andExpect(status().isOk());
    }

    @Test
    void whenServiceThrowsResourceNotFound_thenHandlerReturns404() throws Exception {
        // Arrange
        when(orderService.createOrder(any())).thenThrow(new ResourceNotFoundException("Customer not found"));
        String orderRequestJson = "{\"orderId\":\"ORD-123\",\"customerId\":\"CUST-456\",\"productIds\":[\"PROD-A1\"]}";

        // Act & Assert
        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void whenServiceThrowsServiceUnavailable_thenHandlerReturns503() throws Exception {
        // Arrange
        when(orderService.getOrders(any(), any())).thenThrow(new ServiceUnavailableException("External service is down"));

        // Act & Assert
        mockMvc.perform(get("/v1/orders"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("External service is down"));
    }
}