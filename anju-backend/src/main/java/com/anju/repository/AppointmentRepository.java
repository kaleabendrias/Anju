package com.anju.repository;

import com.anju.entity.Appointment;
import com.anju.entity.Appointment.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByStatus(AppointmentStatus status);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    List<Appointment> findByOperatorId(Long operatorId);

    Page<Appointment> findByOperatorId(Long operatorId, Pageable pageable);

    Optional<Appointment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<Appointment> findByOperatorId(Long operatorId);

    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND a.createdAt < :threshold")
    List<Appointment> findStaleAppointments(
            @Param("status") AppointmentStatus status, 
            @Param("threshold") LocalDateTime threshold);

    @Query("SELECT a FROM Appointment a WHERE a.accompanyingStaffId = :staffId " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED') " +
           "AND ((a.startTime <= :startTime AND a.endTime > :startTime) " +
           "OR (a.startTime < :endTime AND a.endTime >= :endTime) " +
           "OR (a.startTime >= :startTime AND a.endTime <= :endTime))")
    List<Appointment> findConflictingStaffAppointments(
            @Param("staffId") Long staffId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.resourceId = :resourceId " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED') " +
           "AND ((a.startTime <= :startTime AND a.endTime > :startTime) " +
           "OR (a.startTime < :endTime AND a.endTime >= :endTime) " +
           "OR (a.startTime >= :startTime AND a.endTime <= :endTime))")
    List<Appointment> findConflictingResourceAppointments(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED') " +
           "AND ((a.startTime <= :startTime AND a.endTime > :startTime) " +
           "OR (a.startTime < :endTime AND a.endTime >= :endTime) " +
           "OR (a.startTime >= :startTime AND a.endTime <= :endTime))")
    List<Appointment> findConflictingAppointments(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.id != :excludeId " +
           "AND a.accompanyingStaffId = :staffId " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED') " +
           "AND ((a.startTime <= :startTime AND a.endTime > :startTime) " +
           "OR (a.startTime < :endTime AND a.endTime >= :endTime) " +
           "OR (a.startTime >= :startTime AND a.endTime <= :endTime))")
    List<Appointment> findConflictingStaffAppointmentsExcluding(
            @Param("staffId") Long staffId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId);

    @Query("SELECT a FROM Appointment a WHERE a.id != :excludeId " +
           "AND a.resourceId = :resourceId " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED') " +
           "AND ((a.startTime <= :startTime AND a.endTime > :startTime) " +
           "OR (a.startTime < :endTime AND a.endTime >= :endTime) " +
           "OR (a.startTime >= :startTime AND a.endTime <= :endTime))")
    List<Appointment> findConflictingResourceAppointmentsExcluding(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId);
}
