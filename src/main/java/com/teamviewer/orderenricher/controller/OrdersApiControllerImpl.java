package com.teamviewer.orderenricher.controller;

import com.teamviewer.orderenricher.api.OrdersApi;
import com.teamviewer.orderenricher.api.model.EnrichedOrderResponse;
import com.teamviewer.orderenricher.api.model.OrderRequest;
import com.teamviewer.orderenricher.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("${openapi.orderEnricherAPI.base-path:/v1}")
public class OrdersApiControllerImpl implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<EnrichedOrderResponse> createOrder(OrderRequest orderRequest) {
        return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<EnrichedOrderResponse> getOrderById(String orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<List<EnrichedOrderResponse>> getOrders(String customerId, String productId) {
        return ResponseEntity.ok(orderService.getOrders(customerId, productId));
    }
}
