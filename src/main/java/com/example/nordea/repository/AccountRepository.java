package com.example.nordea.repository;

import com.example.nordea.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    @Query("SELECT a FROM AccountEntity a LEFT JOIN FETCH a.investmentEntities WHERE a.id = :id")
    Optional<AccountEntity> findByIdWithInvestments(@Param("id") UUID id);
}
