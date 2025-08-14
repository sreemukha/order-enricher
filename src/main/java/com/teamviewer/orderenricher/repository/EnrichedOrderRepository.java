package com.teamviewer.orderenricher.repository;

import com.teamviewer.orderenricher.domain.EnrichedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrichedOrderRepository extends JpaRepository<EnrichedOrder, String> {
}