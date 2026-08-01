package com.pezbackend.staff.infrastructure.persistence.jpa.repositories;

import com.pezbackend.staff.domain.model.entities.OvertimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para gestionar la persistencia de los registros de horas extras.
 */
@Repository
public interface OvertimeRecordRepository extends JpaRepository<OvertimeRecord, Long> {
    List<OvertimeRecord> findAllByStaffProfileId(Long staffProfileId);
}
