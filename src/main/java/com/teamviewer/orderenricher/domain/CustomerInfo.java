package com.teamviewer.orderenricher.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class CustomerInfo {
    private String customerId;
    private String name;
    private String street;
    private String zip;
    private String country;
}