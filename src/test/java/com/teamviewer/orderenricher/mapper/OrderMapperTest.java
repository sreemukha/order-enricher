package com.teamviewer.orderenricher.mapper;

import com.teamviewer.orderenricher.api.model.Customer;
import com.teamviewer.orderenricher.api.model.EnrichedOrderResponse;
import com.teamviewer.orderenricher.api.model.OrderRequest;
import com.teamviewer.orderenricher.api.model.Product;
import com.teamviewer.orderenricher.domain.EnrichedOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void shouldMapToEntityCorrectly_andBackToApiObject() {
        // Arrange
        OrderRequest request = new OrderRequest()
                .orderId("ORD-1")
                .customerId("CUST-1")
                .timestamp(OffsetDateTime.now());
        Customer customer = new Customer().id("CUST-1").name("Customer Name");
        List<Product> products = List.of(new Product().id("PROD-1").name("Product Name").price(BigDecimal.valueOf(100.0)));

        // Act
        EnrichedOrder entity = mapper.toEntity(request, customer, products);

        // Assert
        assertThat(entity.getOrderId()).isEqualTo(request.getOrderId());
        assertThat(entity.getCustomer().getCustomerId()).isEqualTo(customer.getId());
        assertThat(entity.getProducts().get(0).getProductId()).isEqualTo(products.get(0).getId());
        assertThat(entity.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100.0));


        EnrichedOrderResponse orderResponse = mapper.toApi(entity);
        assertThat(orderResponse.getOrderId()).isEqualTo(entity.getOrderId());
        assertThat(orderResponse.getTotalPrice()).isEqualTo(entity.getTotalPrice());
    }
}