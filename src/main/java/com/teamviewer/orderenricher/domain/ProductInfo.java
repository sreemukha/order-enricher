package com.teamviewer.orderenricher.domain;

import com.teamviewer.orderenricher.domain.converter.StringListConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Embeddable
@Data
@NoArgsConstructor
public class ProductInfo {
    private String productId;
    private String name;
    private BigDecimal price;
    private String category;

    @Convert(converter = StringListConverter.class)
    private List<String> tags;
}