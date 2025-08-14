package com.teamviewer.orderenricher.mapper;

import com.teamviewer.orderenricher.api.model.Customer;
import com.teamviewer.orderenricher.api.model.EnrichedOrderResponse;
import com.teamviewer.orderenricher.api.model.OrderRequest;
import com.teamviewer.orderenricher.api.model.Product;
import com.teamviewer.orderenricher.domain.CustomerInfo;
import com.teamviewer.orderenricher.domain.EnrichedOrder;
import com.teamviewer.orderenricher.domain.ProductInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public EnrichedOrder toEntity(OrderRequest request, Customer customer, List<Product> products) {
        EnrichedOrder entity = new EnrichedOrder();
        entity.setOrderId(request.getOrderId());
        entity.setTimestamp(request.getTimestamp());
        entity.setCustomer(toCustomerInfo(customer));
        entity.setProducts(products.stream().map(this::toProductInfo).collect(Collectors.toList()));

        BigDecimal totalPrice = products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        entity.setTotalPrice(totalPrice);

        return entity;
    }

    public EnrichedOrderResponse toApi(EnrichedOrder entity) {
        EnrichedOrderResponse api = new EnrichedOrderResponse();
        api.setOrderId(entity.getOrderId());
        api.setTimestamp(entity.getTimestamp());
        api.setCustomer(toApiCustomer(entity.getCustomer()));
        api.setProducts(entity.getProducts().stream().map(this::toApiProduct).collect(Collectors.toList()));
        api.setTotalPrice(entity.getTotalPrice());
        return api;
    }

    private CustomerInfo toCustomerInfo(Customer customer) {
        CustomerInfo info = new CustomerInfo();
        info.setCustomerId(customer.getId());
        info.setName(customer.getName());
        info.setStreet(customer.getStreet());
        info.setZip(customer.getZip());
        info.setCountry(customer.getCountry());
        return info;
    }

    private ProductInfo toProductInfo(Product product) {
        ProductInfo info = new ProductInfo();
        info.setProductId(product.getId());
        info.setName(product.getName());
        info.setPrice(product.getPrice());
        info.setCategory(product.getCategory());
        info.setTags(product.getTags());
        return info;
    }

    private Customer toApiCustomer(CustomerInfo info) {
        Customer api = new Customer();
        api.setId(info.getCustomerId());
        api.setName(info.getName());
        api.setStreet(info.getStreet());
        api.setZip(info.getZip());
        api.setCountry(info.getCountry());
        return api;
    }

    private Product toApiProduct(ProductInfo info) {
        Product api = new Product();
        api.setId(info.getProductId());
        api.setName(info.getName());
        api.setPrice(info.getPrice());
        api.setCategory(info.getCategory());
        api.setTags(info.getTags());
        return api;
    }
}