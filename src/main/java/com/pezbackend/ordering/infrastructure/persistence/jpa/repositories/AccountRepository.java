package com.pezbackend.ordering.infrastructure.persistence.jpa.repositories;

import com.pezbackend.ordering.domain.model.aggregates.Account;
import com.pezbackend.ordering.domain.model.valueobjects.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByStatus(AccountStatus status);

    List<Account> findByStaffId(Long staffId);

    List<Account> findByCustomerDni(String customerDni);

    List<Account> findByCustomerRuc(String customerRuc);
}