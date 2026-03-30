package com.example.nordea.repository;

import com.example.nordea.entity.TaxRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaxRuleRepository extends JpaRepository<TaxRuleEntity, UUID> {
}
