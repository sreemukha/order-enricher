package com.teamviewer.orderenricher.service;

import com.teamviewer.orderenricher.api.model.Customer;
import com.teamviewer.orderenricher.api.model.EnrichedOrderResponse;
import com.teamviewer.orderenricher.api.model.OrderRequest;
import com.teamviewer.orderenricher.api.model.Product;
import com.teamviewer.orderenricher.client.CustomerServiceClient;
import com.teamviewer.orderenricher.client.ProductServiceClient;
import com.teamviewer.orderenricher.domain.EnrichedOrder;
import com.teamviewer.orderenricher.domain.ProductInfo;
import com.teamviewer.orderenricher.mapper.OrderMapper;
import com.teamviewer.orderenricher.repository.EnrichedOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CustomerServiceClient customerClient;
    @Mock
    private ProductServiceClient productClient;
    @Mock
    private EnrichedOrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;
    private Customer customer;
    private Product product;
    private EnrichedOrder enrichedOrder;
    private EnrichedOrderResponse enrichedOrderResponse;

    @BeforeEach
    void setUp() {
        // Common test data setup
        orderRequest = new OrderRequest()
                .orderId("ORD-123")
                .customerId("CUST-456")
                .addProductIdsItem("PROD-A1")
                .timestamp(OffsetDateTime.now());

        customer = new Customer().id("CUST-456").name("Test Customer");
        product = new Product().id("PROD-A1").name("Test Product");
        enrichedOrder = new EnrichedOrder(); // Assume this is populated
        enrichedOrderResponse = new EnrichedOrderResponse().orderId("ORD-123");
    }

    @Test
    void whenCreateOrder_thenSavesAndReturnsEnrichedOrder() {
        when(customerClient.getCustomerById("CUST-456")).thenReturn(customer);
        when(productClient.getProductById("PROD-A1")).thenReturn(product);
        when(orderMapper.toEntity(any(), any(), any())).thenReturn(enrichedOrder);
        when(orderRepository.save(enrichedOrder)).thenReturn(enrichedOrder);
        when(orderMapper.toApi(enrichedOrder)).thenReturn(enrichedOrderResponse);

        EnrichedOrderResponse response = orderService.createOrder(orderRequest);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo("ORD-123");

        verify(orderRepository, times(1)).save(enrichedOrder);
    }

    @Test
    void whenGetOrderById_andOrderExists_thenReturnsOptionalOfOrder() {
        when(orderRepository.findById("ORD-123")).thenReturn(Optional.of(enrichedOrder));
        when(orderMapper.toApi(enrichedOrder)).thenReturn(enrichedOrderResponse);

        Optional<EnrichedOrderResponse> response = orderService.getOrderById("ORD-123");

        assertThat(response).isPresent();
        assertThat(response.get()).isEqualTo(enrichedOrderResponse);
    }

    @Test
    void whenGetOrderById_andOrderDoesNotExist_thenReturnsEmptyOptional() {
        when(orderRepository.findById("ORD-UNKNOWN")).thenReturn(Optional.empty());
        Optional<EnrichedOrderResponse> response = orderService.getOrderById("ORD-UNKNOWN");
        assertThat(response).isNotPresent();
    }

    @Test
    void whenGetOrders_withNoFilters_thenReturnsAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(enrichedOrder));
        when(orderMapper.toApi(enrichedOrder)).thenReturn(enrichedOrderResponse);

        List<EnrichedOrderResponse> result = orderService.getOrders(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo("ORD-123");
        verify(orderRepository, times(1)).findAll();
        verify(orderRepository, never()).findByCriteria(any(), any());
    }

    @Test
    void whenGetOrders_withCustomerIdFilter_thenReturnsFilteredOrders() {
        when(orderRepository.findByCriteria("CUST-456", null)).thenReturn(List.of(enrichedOrder));
        when(orderMapper.toApi(enrichedOrder)).thenReturn(enrichedOrderResponse);

        List<EnrichedOrderResponse> result = orderService.getOrders("CUST-456", null);

        assertThat(result).hasSize(1);
        verify(orderRepository, never()).findAll();
        verify(orderRepository, times(1)).findByCriteria("CUST-456", null);
    }

    @Test
    void whenGetOrders_withProductIdFilter_thenReturnsFilteredProductsInOrder() {
        ProductInfo productInfoA1 = new ProductInfo();
        productInfoA1.setProductId("PROD-A1");
        ProductInfo productInfoB2 = new ProductInfo();
        productInfoB2.setProductId("PROD-B2");

        EnrichedOrder orderWithTwoProducts = new EnrichedOrder();
        orderWithTwoProducts.setProducts(List.of(productInfoA1, productInfoB2));

        EnrichedOrderResponse responseWithOneProduct = new EnrichedOrderResponse();
        responseWithOneProduct.setProducts(List.of(new Product().id("PROD-A1")));


        when(orderRepository.findByCriteria(null, "PROD-A1")).thenReturn(List.of(orderWithTwoProducts));

        when(orderMapper.toApi(any(EnrichedOrder.class))).thenAnswer(invocation -> {
            EnrichedOrder argument = invocation.getArgument(0);
            EnrichedOrderResponse response = new EnrichedOrderResponse();
            if (argument.getProducts().size() == 1 && argument.getProducts().get(0).getProductId().equals("PROD-A1")) {
                response.setProducts(List.of(new Product().id("PROD-A1")));
            }
            return response;
        });

        List<EnrichedOrderResponse> result = orderService.getOrders(null, "PROD-A1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProducts()).hasSize(1);
        assertThat(result.get(0).getProducts().get(0).getId()).isEqualTo("PROD-A1");
        verify(orderRepository, times(1)).findByCriteria(null, "PROD-A1");
    }
}
