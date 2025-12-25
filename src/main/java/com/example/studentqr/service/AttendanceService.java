package com.example.studentqr.service;

import com.example.studentqr.model.Attendance;
import com.example.studentqr.model.Holiday;
import com.example.studentqr.model.Student;
import com.example.studentqr.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentService studentService;

    @Autowired
    private HolidayService holidayService;

    public Attendance markAttendance(String studentIdOrRollNumber, String status, String remarks) {
        LocalDate today = LocalDate.now();

        // Check if today is a no-attendance day
        if (holidayService.isNoAttendanceDay(today)) {
            List<Holiday> holidays = holidayService.getNoAttendanceHolidaysForDate(today);
            String holidayNames = holidays.stream()
                    .map(Holiday::getName)
                    .collect(Collectors.joining(", "));

            throw new RuntimeException("Cannot mark attendance on " + today +
                    ". It's a holiday: " + holidayNames);
        }

        Optional<Student> studentOpt = findStudent(studentIdOrRollNumber);

        if (studentOpt.isEmpty()) {
            throw new RuntimeException("Student not found: " + studentIdOrRollNumber);
        }

        Student student = studentOpt.get();

        // Check if attendance already marked for today
        Optional<Attendance> existing = attendanceRepository.findByStudentAndAttendanceDate(student, today);

        Attendance attendance;
        if (existing.isPresent()) {
            attendance = existing.get();
            attendance.setStatus(status);
            attendance.setRemarks(remarks);
            attendance.setMarkedAt(LocalDateTime.now());
        } else {
            attendance = new Attendance(student, status);
            attendance.setRemarks(remarks);
        }

        attendance.setMarkedBy(getCurrentUsername());

        return attendanceRepository.save(attendance);
    }

    public Attendance markAttendanceByQR(String qrData, String status, String remarks) {
        // Extract student ID or roll number from QR data
        String studentIdentifier = extractStudentIdentifier(qrData);
        return markAttendance(studentIdentifier, status, remarks);
    }

    public List<Attendance> getTodaysAttendance() {
        return attendanceRepository.findByAttendanceDate(LocalDate.now());
    }

    public List<Attendance> getStudentAttendance(String studentIdOrRollNumber) {
        Optional<Student> studentOpt = findStudent(studentIdOrRollNumber);
        return studentOpt.map(attendanceRepository::findByStudent)
                .orElse(List.of());
    }

    public Map<String, Object> getAttendanceSummary(LocalDate date) {
        Map<String, Object> summary = new HashMap<>();

        List<Attendance> todaysAttendance = attendanceRepository.findByAttendanceDate(date);

        long presentCount = todaysAttendance.stream()
                .filter(Attendance::isPresent)
                .count();

        long absentCount = todaysAttendance.stream()
                .filter(Attendance::isAbsent)
                .count();

        long totalStudents = studentService.getStudentCount();
        long markedCount = todaysAttendance.size();
        long unmarkedCount = totalStudents - markedCount;

        summary.put("date", date);
        summary.put("presentCount", presentCount);
        summary.put("absentCount", absentCount);
        summary.put("totalStudents", totalStudents);
        summary.put("markedCount", markedCount);
        summary.put("unmarkedCount", unmarkedCount);
        summary.put("attendancePercentage",
                totalStudents > 0 ? (presentCount * 100.0 / totalStudents) : 0.0);

        return summary;
    }

    public Map<String, Object> getCourseWiseSummary(String course, LocalDate date) {
        Map<String, Object> summary = new HashMap<>();

        Long presentCount = attendanceRepository.countPresentByCourseAndDate(course, date);
        Long totalCount = attendanceRepository.countTotalByCourseAndDate(course, date);

        summary.put("course", course);
        summary.put("date", date);
        summary.put("presentCount", presentCount != null ? presentCount : 0);
        summary.put("totalCount", totalCount != null ? totalCount : 0);
        summary.put("attendancePercentage",
                totalCount != null && totalCount > 0 ?
                        (presentCount != null ? presentCount * 100.0 / totalCount : 0.0) : 0.0);

        return summary;
    }

    public Map<String, Object> getDateAttendanceStatus(LocalDate date) {
        Map<String, Object> status = new HashMap<>();

        status.put("date", date);
        status.put("isHoliday", holidayService.isNoAttendanceDay(date));

        if (holidayService.isNoAttendanceDay(date)) {
            List<Holiday> holidays = holidayService.getNoAttendanceHolidaysForDate(date);
            status.put("holidayNames", holidays.stream()
                    .map(Holiday::getName)
                    .collect(Collectors.joining(", ")));
            status.put("holidayDetails", holidays);
            status.put("canMarkAttendance", false);
        } else {
            status.put("canMarkAttendance", true);
            status.put("todaysAttendance", attendanceRepository.findByAttendanceDate(date));
        }

        return status;
    }

    public boolean canMarkAttendance(LocalDate date) {
        return !holidayService.isNoAttendanceDay(date);
    }

    public List<Attendance> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByAttendanceDateBetween(startDate, endDate);
    }

    public Map<String, Object> getAttendanceStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        List<Attendance> attendances = getAttendanceByDateRange(startDate, endDate);
        List<Student> allStudents = studentService.getAllStudents();

        long totalAttendanceRecords = attendances.size();
        long totalPresent = attendances.stream()
                .filter(Attendance::isPresent)
                .count();

        long uniqueStudents = attendances.stream()
                .map(a -> a.getStudent().getId())
                .distinct()
                .count();

        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("totalAttendanceRecords", totalAttendanceRecords);
        stats.put("totalPresent", totalPresent);
        stats.put("totalAbsent", totalAttendanceRecords - totalPresent);
        stats.put("uniqueStudents", uniqueStudents);
        stats.put("totalStudents", allStudents.size());
        stats.put("attendanceRate", totalAttendanceRecords > 0 ?
                (totalPresent * 100.0 / totalAttendanceRecords) : 0.0);

        return stats;
    }

    private Optional<Student> findStudent(String identifier) {
        // Try by ID first
        Optional<Student> byId = studentService.getStudentById(identifier);
        if (byId.isPresent()) {
            return byId;
        }

        // Try by roll number
        return studentService.getStudentByRollNumber(identifier);
    }

    private String extractStudentIdentifier(String qrData) {
        // Simple extraction - in real app, parse QR data properly
        if (qrData.contains("ID:")) {
            String[] lines = qrData.split("\n");
            for (String line : lines) {
                if (line.startsWith("ID:")) {
                    return line.substring(3).trim();
                }
                if (line.startsWith("Roll:")) {
                    return line.substring(5).trim();
                }
            }
        }
        return qrData;
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}