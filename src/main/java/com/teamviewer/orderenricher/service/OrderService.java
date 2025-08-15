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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final CustomerServiceClient customerClient;
    private final ProductServiceClient productClient;
    private final EnrichedOrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    @CacheEvict(value = "orders", allEntries = true) // Evict all entries in the 'orders' cache
    public EnrichedOrderResponse createOrder(OrderRequest orderRequest) {
        log.info("Creating order and clearing cache...");

        log.info("Starting enrichment for orderId: {}", orderRequest.getOrderId());

        log.info("Fetching customer: {}", orderRequest.getCustomerId());
        Customer customer = customerClient.getCustomerById(orderRequest.getCustomerId());

        log.info("Fetching {} products", orderRequest.getProductIds().size());
        List<Product> products = orderRequest.getProductIds().stream()
                .map(productId -> {
                    log.info("Fetching product: {}", productId);
                    return productClient.getProductById(productId);
                })
                .collect(Collectors.toList());

        EnrichedOrder enrichedOrderEntity = orderMapper.toEntity(orderRequest, customer, products);

        orderRepository.save(enrichedOrderEntity);
        log.info("Successfully persisted enriched order: {}", enrichedOrderEntity.getOrderId());

        return orderMapper.toApi(enrichedOrderEntity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#orderId") // Cache result based on orderId
    public Optional<EnrichedOrderResponse> getOrderById(String orderId) {
        log.info("Fetching order by ID from database: {}", orderId);
        return orderRepository.findById(orderId)
                .map(orderMapper::toApi);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "'customerId=' + #customerId + ';productId=' + #productId")
    public List<EnrichedOrderResponse> getOrders(String customerId, String productId) {
        log.info("Fetching orders from the database by customerId: {} and productId: {}", customerId, productId);

        if(customerId == null && productId == null) {
            return orderRepository.findAll()
                    .stream().map(orderMapper::toApi)
                    .collect(Collectors.toList());
        }

        List<EnrichedOrder> enrichedOrders = orderRepository.findByCriteria(customerId, productId);
        if(productId != null) {
            enrichedOrders.forEach(eorder -> {
                List<ProductInfo> filteredProducts = eorder.getProducts().stream()
                        .filter(prod -> prod.getProductId().equals(productId)).toList();
                eorder.setProducts(filteredProducts);
            });
        }

        return  enrichedOrders.stream()
                .map(orderMapper::toApi)
                .collect(Collectors.toList());
    }
}