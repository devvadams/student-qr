package com.example.studentqr.repository;

import com.example.studentqr.model.Attendance;
import com.example.studentqr.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByStudentAndAttendanceDate(Student student, LocalDate date);

    List<Attendance> findByStudent(Student student);

    List<Attendance> findByAttendanceDate(LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.student.rollNumber = :rollNumber AND a.attendanceDate = :date")
    Optional<Attendance> findByRollNumberAndDate(@Param("rollNumber") String rollNumber,
                                                 @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.course = :course AND a.attendanceDate = :date AND a.status = 'PRESENT'")
    Long countPresentByCourseAndDate(@Param("course") String course,
                                     @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.course = :course AND a.attendanceDate = :date")
    Long countTotalByCourseAndDate(@Param("course") String course,
                                   @Param("date") LocalDate date);

    List<Attendance> findByAttendanceDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT a.student) FROM Attendance a WHERE a.attendanceDate = :date AND a.status = 'PRESENT'")
    Long countDistinctPresentStudentsByDate(@Param("date") LocalDate date);

    @Query("SELECT a.student.course, COUNT(a) as total, " +
            "SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present " +
            "FROM Attendance a WHERE a.attendanceDate = :date " +
            "GROUP BY a.student.course")
    List<Object[]> getCourseWiseStatsByDate(@Param("date") LocalDate date);
}