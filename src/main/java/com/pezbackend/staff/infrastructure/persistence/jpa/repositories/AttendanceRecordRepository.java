package com.pezbackend.staff.infrastructure.persistence.jpa.repositories;

import com.pezbackend.staff.domain.model.entities.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para gestionar la persistencia de los registros de asistencia.
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findAllByStaffProfileId(Long staffProfileId);
    Optional<AttendanceRecord> findFirstByStaffProfileIdAndCheckOutAtIsNullOrderByCheckInAtDesc(Long staffProfileId);
}
