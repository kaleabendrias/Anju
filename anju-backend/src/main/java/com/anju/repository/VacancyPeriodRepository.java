package com.anju.repository;

import com.anju.entity.VacancyPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacancyPeriodRepository extends JpaRepository<VacancyPeriod, Long> {

    List<VacancyPeriod> findByPropertyIdAndIsActiveTrue(Long propertyId);
    
    List<VacancyPeriod> findByCreatedBy(Long createdBy);

    @Query("SELECT v FROM VacancyPeriod v WHERE v.property.id = :propertyId AND v.isActive = true " +
           "AND ((v.endDate IS NULL AND v.startDate <= :date) OR " +
           "(v.endDate IS NOT NULL AND v.startDate <= :date AND v.endDate >= :date))")
    List<VacancyPeriod> findActiveVacancyForPropertyOnDate(@Param("propertyId") Long propertyId, 
                                                          @Param("date") LocalDate date);

    @Query("SELECT v FROM VacancyPeriod v WHERE v.property.id = :propertyId AND v.isActive = true " +
           "AND ((v.endDate IS NULL) OR (v.endDate >= :startDate AND v.startDate <= :endDate))")
    List<VacancyPeriod> findOverlappingVacancies(@Param("propertyId") Long propertyId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT v FROM VacancyPeriod v WHERE v.property.id = :propertyId AND v.isActive = true " +
           "AND v.endDate IS NULL")
    List<VacancyPeriod> findOpenEndedVacancies(@Param("propertyId") Long propertyId);

    void deleteByPropertyId(Long propertyId);
}
