package com.example.studentqr.repository;

import com.example.studentqr.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByActiveTrue();

    @Query("SELECT h FROM Holiday h WHERE YEAR(h.holidayDate) = :year")
    List<Holiday> findByYear(@Param("year") int year);

    List<Holiday> findByHolidayDateBetween(LocalDate start, LocalDate end);

    Optional<Holiday> findByHolidayDate(LocalDate date);

    @Query("SELECT DISTINCT YEAR(h.holidayDate) FROM Holiday h ORDER BY YEAR(h.holidayDate) DESC")
    List<Integer> findDistinctYears();

    @Query("SELECT h FROM Holiday h WHERE h.active = true AND " +
            "(:date BETWEEN h.holidayDate AND COALESCE(h.endDate, h.holidayDate))")
    List<Holiday> findActiveHolidaysForDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE h.active = true AND h.noAttendance = true AND " +
            "(:date BETWEEN h.holidayDate AND COALESCE(h.endDate, h.holidayDate))")
    List<Holiday> findNoAttendanceHolidaysForDate(@Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
            "FROM Holiday h WHERE h.active = true AND h.noAttendance = true AND " +
            "(:date BETWEEN h.holidayDate AND COALESCE(h.endDate, h.holidayDate))")
    boolean isNoAttendanceDay(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE h.active = true AND h.autoMarkAttendance = true AND " +
            "(:date BETWEEN h.holidayDate AND COALESCE(h.endDate, h.holidayDate))")
    List<Holiday> findAutoMarkHolidaysForDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE h.active = true AND h.schoolActivity = true AND " +
            "(:date BETWEEN h.holidayDate AND COALESCE(h.endDate, h.holidayDate))")
    List<Holiday> findSchoolActivitiesForDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE h.active = true AND h.recurringYearly = true")
    List<Holiday> findRecurringHolidays();
}