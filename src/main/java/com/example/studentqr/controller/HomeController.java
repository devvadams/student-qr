package com.example.studentqr.controller;

import com.example.studentqr.model.Holiday;
import com.example.studentqr.service.AttendanceService;
import com.example.studentqr.service.HolidayService;
import com.example.studentqr.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get current user authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_TEACHER"));
        boolean isUser = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"));

        // Add user info to model
        model.addAttribute("username", username);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isTeacher", isTeacher);
        model.addAttribute("isUser", isUser);

        // Add student stats
        long totalStudents = studentService.getStudentCount();
        model.addAttribute("totalStudents", totalStudents);

        // Add holiday information
        LocalDate today = LocalDate.now();
        boolean isHolidayToday = holidayService.isNoAttendanceDay(today);
        model.addAttribute("isHolidayToday", isHolidayToday);

        // Get upcoming holidays (next 30 days)
        List<Holiday> upcomingHolidays = holidayService.getUpcomingHolidays();
        model.addAttribute("upcomingHolidays", upcomingHolidays);

        // Get today's holidays if any
        List<Holiday> todayHolidays = holidayService.getHolidaysForDate(today);
        model.addAttribute("todayHolidays", todayHolidays);

        // Add attendance stats
        if (!isHolidayToday && (isTeacher || isAdmin)) {
            // Get today's attendance summary
            Map<String, Object> attendanceSummary = attendanceService.getAttendanceSummary(today);
            model.addAttribute("attendanceSummary", attendanceSummary);

            // Add individual stats
            model.addAttribute("presentToday", attendanceSummary.get("presentCount"));
            model.addAttribute("absentToday", attendanceSummary.get("absentCount"));
            model.addAttribute("markedToday", attendanceSummary.get("markedCount"));
            model.addAttribute("unmarkedToday", attendanceSummary.get("unmarkedCount"));
            model.addAttribute("attendancePercentage", attendanceSummary.get("attendancePercentage"));

            // Get attendance status for today
            Map<String, Object> dateStatus = attendanceService.getDateAttendanceStatus(today);
            model.addAttribute("canMarkAttendance", dateStatus.get("canMarkAttendance"));

            // Get attendance stats for the week
            LocalDate weekStart = today.minusDays(6); // Last 7 days including today
            Map<String, Object> weeklyStats = attendanceService.getAttendanceStats(weekStart, today);
            model.addAttribute("weeklyStats", weeklyStats);
        }

        // ========== ADD HOLIDAY STATISTICS FOR ADMIN DASHBOARD ==========
        if (isAdmin) {
            // Get total holidays count
            long totalHolidays = holidayService.getAllHolidays().size();
            model.addAttribute("totalHolidays", totalHolidays);

            // Get holiday summary statistics
            Map<String, Object> holidayStats = new HashMap<>();
            holidayStats.put("upcomingCount", upcomingHolidays.size());
            holidayStats.put("todayEvents", todayHolidays.size());

            // Count holidays by type
            List<Holiday> allHolidays = holidayService.getAllHolidays();
            long schoolActivities = allHolidays.stream()
                    .filter(h -> h.getType() == Holiday.HolidayType.SCHOOL_ACTIVITY)
                    .count();
            long vacations = allHolidays.stream()
                    .filter(h -> h.getType() == Holiday.HolidayType.VACATION)
                    .count();
            long specialEvents = allHolidays.stream()
                    .filter(h -> h.getType() == Holiday.HolidayType.SPECIAL_EVENT)
                    .count();

            holidayStats.put("schoolActivities", schoolActivities);
            holidayStats.put("vacations", vacations);
            holidayStats.put("specialEvents", specialEvents);

            model.addAttribute("holidayStats", holidayStats);
        }
        // ========== END HOLIDAY STATISTICS ==========

        // Add today's date in nice format
        model.addAttribute("today", today);
        model.addAttribute("todayFormatted", today.toString());

        return "dashboard";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        model.addAttribute("authorities", auth.getAuthorities());
        return "profile";
    }
}