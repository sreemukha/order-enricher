package com.teamviewer.orderenricher.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "enriched_orders")
@Data
@NoArgsConstructor
public class EnrichedOrder {

    @Id
    private String orderId;

    private OffsetDateTime timestamp;

    @Embedded
    private CustomerInfo customer;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    private List<ProductInfo> products;

    private BigDecimal totalPrice;
}