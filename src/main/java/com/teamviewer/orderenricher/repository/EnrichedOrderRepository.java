package com.teamviewer.orderenricher.repository;

import com.teamviewer.orderenricher.domain.EnrichedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrichedOrderRepository extends JpaRepository<EnrichedOrder, String> {

    /**
     * Finds orders by customerId and/or productId.
     * If a parameter is null, it is ignored.
     * The DISTINCT keyword prevents duplicate orders when an order matches multiple products.
     */
    @Query("""
            select distinct o
            from EnrichedOrder o
            left join o.products p
            where (:customerId is null or o.customer.customerId = :customerId)
            and (:productId  is null or p.productId = :productId)
           """)
    List<EnrichedOrder> findByCriteria(@Param("customerId") String customerId, @Param("productId") String productId);
}