package com.pezbackend.cashregister.infrastructure.persistence.jpa.repositories;

import com.pezbackend.cashregister.domain.model.aggregates.CashRegister;
import com.pezbackend.cashregister.domain.model.valueobjects.CashRegisterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {

    // Busca el turno abierto actual
    Optional<CashRegister> findByStatus(CashRegisterStatus status);

    List<CashRegister> findByCreatedAtBetween(Date startDate, Date endDate);
}