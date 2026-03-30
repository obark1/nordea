package com.example.nordea.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "investments")
@Data
@Setter
public class InvestmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore
    private AccountEntity account;

    @Column(nullable = false, length = 6)
    private String fundCode;

    @Embedded
    private Money amount;

    @CreationTimestamp
    private Instant allocatedAt;
}
