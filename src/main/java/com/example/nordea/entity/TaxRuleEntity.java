package com.example.nordea.entity;

import com.example.nordea.model.ProductType;
import com.example.nordea.model.TaxCountry;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tax_rules", uniqueConstraints = @UniqueConstraint(columnNames = {"taxCountry", "productType"}))
public class TaxRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TaxCountry taxCountry;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal annualAllowance;

}
