package com.example.studentqr.controller;

import com.example.studentqr.model.Holiday;
import com.example.studentqr.model.Student;
import com.example.studentqr.service.AttendanceService;
import com.example.studentqr.service.HolidayService;
import com.example.studentqr.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/attendance")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private HolidayService holidayService;

    // ==== MARK ATTENDANCE PAGE ====
    @GetMapping("/mark")
    public String markAttendancePage(Model model) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        String formattedDate = today.format(formatter);

        // Check holiday status using both methods for compatibility
        boolean shouldMarkAttendance = false;
        boolean isHoliday = false;
        List<Holiday> todayHolidays = new ArrayList<>();

        try {
            // Using your new method
            shouldMarkAttendance = holidayService.shouldMarkAttendance(today);
            isHoliday = !shouldMarkAttendance; // This is for backward compatibility
            todayHolidays = holidayService.getHolidaysForDate(today);

            // Also check with old method for additional safety
            boolean isNoAttendanceDay = holidayService.isNoAttendanceDay(today);

            // If either method says it's a no-attendance day, treat it as holiday
            if (isNoAttendanceDay && shouldMarkAttendance) {
                shouldMarkAttendance = false;
                isHoliday = true;
            }

        } catch (Exception e) {
            // Log error but continue
            System.err.println("Error checking holidays: " + e.getMessage());
            shouldMarkAttendance = true; // Default to allowing attendance
            isHoliday = false;
        }

        model.addAttribute("today", today);
        model.addAttribute("formattedDate", formattedDate);
        model.addAttribute("isHoliday", isHoliday);
        model.addAttribute("shouldMarkAttendance", shouldMarkAttendance);
        model.addAttribute("holidays", todayHolidays);
        model.addAttribute("todaysAttendance", attendanceService.getTodaysAttendance());
        model.addAttribute("attendanceSummary", attendanceService.getAttendanceSummary(today));

        return "attendance-mark";
    }

    // ==== MARK ATTENDANCE BY ID/ROLL ====
    @PostMapping("/mark")
    public String markAttendance(@RequestParam String studentIdentifier,
                                 @RequestParam String status,
                                 @RequestParam(required = false) String remarks,
                                 RedirectAttributes redirectAttributes) {

        LocalDate today = LocalDate.now();

        // Check if today is a holiday using both methods
        boolean isAttendanceDay = false;
        try {
            // Primary check using new method
            isAttendanceDay = holidayService.shouldMarkAttendance(today);

            // Secondary check using old method
            boolean isNoAttendanceDay = holidayService.isNoAttendanceDay(today);

            // If either says no attendance, block it
            if (!isAttendanceDay || isNoAttendanceDay) {
                redirectAttributes.addAttribute("error", true);
                redirectAttributes.addAttribute("message",
                        "Cannot mark attendance today - it's a holiday/special day!");
                return "redirect:/attendance/mark";
            }
        } catch (Exception e) {
            // Continue even if holiday check fails
            System.err.println("Holiday check failed: " + e.getMessage());
            isAttendanceDay = true; // Default to allowing
        }

        try {
            attendanceService.markAttendance(studentIdentifier, status, remarks);
            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message",
                    "Attendance marked successfully for student: " + studentIdentifier);
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message",
                    "Error: " + e.getMessage());
        }
        return "redirect:/attendance/mark";
    }

    // ==== MARK ATTENDANCE BY QR ====
    @PostMapping("/mark-by-qr")
    public String markAttendanceByQR(@RequestParam String qrData,
                                     @RequestParam String status,
                                     @RequestParam(required = false) String remarks,
                                     RedirectAttributes redirectAttributes) {

        LocalDate today = LocalDate.now();

        // Check if today is a holiday using both methods
        boolean isAttendanceDay = false;
        try {
            // Primary check using new method
            isAttendanceDay = holidayService.shouldMarkAttendance(today);

            // Secondary check using old method
            boolean isNoAttendanceDay = holidayService.isNoAttendanceDay(today);

            // If either says no attendance, block it
            if (!isAttendanceDay || isNoAttendanceDay) {
                redirectAttributes.addAttribute("error", true);
                redirectAttributes.addAttribute("message",
                        "Cannot mark attendance today - it's a holiday/special day!");
                return "redirect:/attendance/mark";
            }
        } catch (Exception e) {
            // Continue even if holiday check fails
            System.err.println("Holiday check failed: " + e.getMessage());
            isAttendanceDay = true; // Default to allowing
        }

        try {
            attendanceService.markAttendanceByQR(qrData, status, remarks);
            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message",
                    "Attendance marked successfully via QR code!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message",
                    "Error: " + e.getMessage());
        }
        return "redirect:/attendance/mark";
    }

    // ==== VIEW ATTENDANCE RECORDS ====
    @GetMapping("/records")
    public String viewRecords(Model model,
                              @RequestParam(required = false) String studentId,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        model.addAttribute("selectedDate", targetDate);
        model.addAttribute("attendanceRecords",
                attendanceService.getTodaysAttendance());
        model.addAttribute("attendanceSummary",
                attendanceService.getAttendanceSummary(targetDate));

        // Get all courses for filter
        List<String> courses = studentService.getAllStudents().stream()
                .map(Student::getCourse)
                .distinct()
                .toList();
        model.addAttribute("courses", courses);

        return "attendance-records";
    }

    // ==== VIEW STUDENT ATTENDANCE ====
    @GetMapping("/student/{id}")
    public String viewStudentAttendance(@PathVariable String id, Model model) {
        List<com.example.studentqr.model.Attendance> records =
                attendanceService.getStudentAttendance(id);

        model.addAttribute("attendanceRecords", records);

        // Get student info
        studentService.getStudentById(id).ifPresent(student -> {
            model.addAttribute("student", student);

            long presentCount = records.stream()
                    .filter(com.example.studentqr.model.Attendance::isPresent)
                    .count();
            long totalCount = records.size();

            model.addAttribute("presentCount", presentCount);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("attendancePercentage",
                    totalCount > 0 ? (presentCount * 100.0 / totalCount) : 0.0);
        });

        return "attendance-student";
    }

    // ==== ATTENDANCE REPORT ====
    @GetMapping("/report")
    public String attendanceReport(Model model,
                                   @RequestParam(required = false) String course,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        String targetCourse = (course != null && !course.isEmpty()) ? course : null;

        if (targetCourse != null) {
            model.addAttribute("courseSummary",
                    attendanceService.getCourseWiseSummary(targetCourse, targetDate));
        }

        model.addAttribute("selectedDate", targetDate);
        model.addAttribute("selectedCourse", targetCourse);
        model.addAttribute("overallSummary",
                attendanceService.getAttendanceSummary(targetDate));

        // Get all courses for filter
        List<String> courses = studentService.getAllStudents().stream()
                .map(Student::getCourse)
                .distinct()
                .toList();
        model.addAttribute("courses", courses);

        return "attendance-report";
    }
}