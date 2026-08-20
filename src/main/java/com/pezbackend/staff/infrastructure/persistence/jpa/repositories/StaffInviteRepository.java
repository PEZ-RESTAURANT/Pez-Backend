package com.pezbackend.staff.infrastructure.persistence.jpa.repositories;

import com.pezbackend.staff.domain.model.aggregates.StaffInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffInviteRepository extends JpaRepository<StaffInvite, Long> {
    Optional<StaffInvite> findByCode(String code);
    Optional<StaffInvite> findFirstByEmailAndUsedFalseAndRevokedFalse(String email);
}
