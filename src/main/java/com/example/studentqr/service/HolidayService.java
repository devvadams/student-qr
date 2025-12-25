package com.example.studentqr.service;

import com.example.studentqr.model.Holiday;
import com.example.studentqr.model.Student;
import com.example.studentqr.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private StudentService studentService;

    @Lazy  // Add @Lazy annotation to break circular dependency
    @Autowired
    private AttendanceService attendanceService;

    public void initializePredefinedHolidays() {
        int currentYear = Year.now().getValue();

        List<Holiday> existingHolidays = holidayRepository.findByYear(currentYear);
        if (!existingHolidays.isEmpty()) {
            return;
        }

        List<Holiday> predefinedHolidays = List.of(
                // Public Holidays
                new Holiday("New Year's Day", "New Year Celebration",
                        LocalDate.of(currentYear, 1, 1), "PUBLIC_HOLIDAY"),

                new Holiday("Republic Day", "Indian Republic Day",
                        LocalDate.of(currentYear, 1, 26), "PUBLIC_HOLIDAY"),

                new Holiday("Independence Day", "Indian Independence Day",
                        LocalDate.of(currentYear, 8, 15), "PUBLIC_HOLIDAY"),

                new Holiday("Gandhi Jayanti", "Mahatma Gandhi's Birthday",
                        LocalDate.of(currentYear, 10, 2), "PUBLIC_HOLIDAY"),

                new Holiday("Christmas Day", "Christmas Celebration",
                        LocalDate.of(currentYear, 12, 25), "PUBLIC_HOLIDAY"),

                // School Holidays/Events
                new Holiday("Teachers' Day", "Teachers' Day Celebration",
                        LocalDate.of(currentYear, 9, 5), "SPECIAL_EVENT"),

                new Holiday("Children's Day", "Children's Day Celebration",
                        LocalDate.of(currentYear, 11, 14), "SPECIAL_EVENT"),

                // Breaks with date ranges
                new Holiday("Summer Vacation", "Summer Break",
                        LocalDate.of(currentYear, 5, 15),
                        LocalDate.of(currentYear, 6, 30), "VACATION"),

                new Holiday("Winter Vacation", "Winter Break",
                        LocalDate.of(currentYear, 12, 20),
                        LocalDate.of(currentYear, 12, 31), "VACATION"),

                new Holiday("Diwali Vacation", "Diwali Festival Break",
                        LocalDate.of(currentYear, 11, 1),
                        LocalDate.of(currentYear, 11, 5), "VACATION"),

                new Holiday("Mid-term Break", "Mid-term Examination Break",
                        LocalDate.of(currentYear, 3, 15),
                        LocalDate.of(currentYear, 3, 20), "BREAK")
        );

        // Set special properties for vacations
        predefinedHolidays.forEach(holiday -> {
            if (holiday.getType() == Holiday.HolidayType.VACATION) {
                holiday.setAutoMarkAttendance(true);
                holiday.setAttendanceStatus("ABSENT");
                holiday.setAffectsResumption(true);
                holiday.setResumptionDate(holiday.getEndDate() != null ?
                        holiday.getEndDate().plusDays(1) : holiday.getHolidayDate().plusDays(1));
            }
        });

        holidayRepository.saveAll(predefinedHolidays);
    }

    // ADD THIS METHOD: Initialize Custom Date Ranges for Activities/Breaks
    public void initializeCustomDateRanges() {
        int currentYear = Year.now().getValue();

        System.out.println("Initializing custom date ranges for year: " + currentYear);

        // ===== SCHOOL ACTIVITIES (Attendance ENABLED) =====
        // These allow attendance marking

        // February: Sports Activities
        addDateRangeHoliday(
                "Annual Sports Week",
                "Inter-house sports competition and activities",
                LocalDate.of(currentYear, 2, 10),
                LocalDate.of(currentYear, 2, 15),
                Holiday.HolidayType.SCHOOL_ACTIVITY
        );

        // August: Cultural Activities
        addDateRangeHoliday(
                "Cultural Festival",
                "Annual cultural fest with performances",
                LocalDate.of(currentYear, 8, 20),
                LocalDate.of(currentYear, 8, 25),
                Holiday.HolidayType.SCHOOL_ACTIVITY
        );

        // November: Science Fair
        addDateRangeHoliday(
                "Science Exhibition Week",
                "Student science projects exhibition",
                LocalDate.of(currentYear, 11, 5),
                LocalDate.of(currentYear, 11, 9),
                Holiday.HolidayType.SCHOOL_ACTIVITY
        );

        // September: Field Trips
        addDateRangeHoliday(
                "Educational Field Trips",
                "Multiple grade-level field trips",
                LocalDate.of(currentYear, 9, 15),
                LocalDate.of(currentYear, 9, 19),
                Holiday.HolidayType.SCHOOL_ACTIVITY
        );

        // ===== CUSTOM BREAK PERIODS (Attendance DISABLED) =====
        // These don't allow attendance marking

        // March: Exam Preparation Break
        addDateRangeHoliday(
                "Pre-Exam Study Break",
                "Break for final exam preparation",
                LocalDate.of(currentYear, 3, 1),
                LocalDate.of(currentYear, 3, 5),
                Holiday.HolidayType.BREAK
        );

        // July: Mid-Semester Break
        addDateRangeHoliday(
                "Mid-Semester Break",
                "Short break between semesters",
                LocalDate.of(currentYear, 7, 10),
                LocalDate.of(currentYear, 7, 14),
                Holiday.HolidayType.BREAK
        );

        // April: Teacher Training Days
        addDateRangeHoliday(
                "Teacher Development Days",
                "Professional development for teachers",
                LocalDate.of(currentYear, 4, 22),
                LocalDate.of(currentYear, 4, 23),
                Holiday.HolidayType.BREAK
        );

        // January: Infrastructure Maintenance
        addDateRangeHoliday(
                "School Maintenance Days",
                "Building maintenance and repairs",
                LocalDate.of(currentYear, 1, 8),
                LocalDate.of(currentYear, 1, 10),
                Holiday.HolidayType.BREAK
        );

        // ===== SPECIAL EVENTS (Mixed Attendance Rules) =====

        // October: Parent-Teacher Meetings
        addDateRangeHoliday(
                "Parent-Teacher Conference",
                "Meetings with parents - half days",
                LocalDate.of(currentYear, 10, 28),
                LocalDate.of(currentYear, 10, 29),
                Holiday.HolidayType.SPECIAL_EVENT
        );

        // March-April: Examination Days
        addDateRangeHoliday(
                "Final Examinations",
                "Annual final examinations",
                LocalDate.of(currentYear, 3, 25),
                LocalDate.of(currentYear, 4, 5),
                Holiday.HolidayType.EXAMINATION
        );

        System.out.println("Custom date ranges initialization completed!");
    }

    // ADD THIS HELPER METHOD: Print all configured date ranges
    public void printAllDateRanges() {
        List<Holiday> allHolidays = getAllHolidays();

        System.out.println("\n=== CONFIGURED DATE RANGES ===");
        int rangeCount = 0;
        for (Holiday holiday : allHolidays) {
            if (holiday.getEndDate() != null) {
                rangeCount++;
                System.out.println("Range #" + rangeCount);
                System.out.println("  Name: " + holiday.getName());
                System.out.println("  Type: " + holiday.getType());
                System.out.println("  Dates: " + holiday.getHolidayDate() + " to " + holiday.getEndDate());
                System.out.println("  Attendance: " + (holiday.isNoAttendance() ? "❌ DISABLED" : "✅ ENABLED"));
                System.out.println("  Auto-mark: " + (holiday.isAutoMarkAttendance() ? "✅ YES" : "❌ NO"));
                if (holiday.getAttendanceStatus() != null) {
                    System.out.println("  Auto-mark Status: " + holiday.getAttendanceStatus());
                }
                System.out.println("---");
            }
        }

        if (rangeCount == 0) {
            System.out.println("No date ranges configured yet.");
        } else {
            System.out.println("Total date ranges: " + rangeCount);
        }
    }

    // ADD THIS TEST METHOD: Check specific dates
    public void testDateRangeConfiguration() {
        int currentYear = Year.now().getValue();

        System.out.println("\n=== TESTING DATE RANGE CONFIGURATION ===");

        // Test dates for different scenarios
        List<LocalDate> testDates = List.of(
                LocalDate.of(currentYear, 2, 12),  // During Sports Week - ACTIVITY (enabled)
                LocalDate.of(currentYear, 3, 2),   // During Pre-Exam Break - BREAK (disabled)
                LocalDate.of(currentYear, 8, 22),  // During Cultural Fest - ACTIVITY (enabled)
                LocalDate.of(currentYear, 7, 12),  // During Mid-Semester Break - BREAK (disabled)
                LocalDate.of(currentYear, 10, 29), // During Parent-Teacher - SPECIAL EVENT
                LocalDate.of(currentYear, 4, 2),   // During Final Exams - EXAMINATION
                LocalDate.of(currentYear, 6, 1),   // Regular school day (no holidays)
                LocalDate.of(currentYear, 5, 20),  // During Summer Vacation - VACATION (disabled)
                LocalDate.of(currentYear, 12, 25)  // Christmas - PUBLIC HOLIDAY (disabled)
        );

        for (LocalDate date : testDates) {
            boolean shouldMark = shouldMarkAttendance(date);
            List<Holiday> holidays = getHolidaysForDate(date);

            System.out.println("\n📅 Date: " + date + " (" + date.getDayOfWeek() + ")");
            System.out.println("   Attendance Marking: " + (shouldMark ? "✅ ALLOWED" : "❌ BLOCKED"));

            if (!holidays.isEmpty()) {
                System.out.println("   Holidays/Events on this date:");
                for (Holiday holiday : holidays) {
                    String typeIcon = getHolidayTypeIcon(holiday.getType());
                    System.out.println("     " + typeIcon + " " + holiday.getName() +
                            " [" + holiday.getType() + "]");
                }
            } else {
                System.out.println("   No holidays/events - Regular school day");
            }
        }
    }

    private String getHolidayTypeIcon(Holiday.HolidayType type) {
        return switch (type) {
            case SCHOOL_ACTIVITY -> "🏫";
            case VACATION -> "🏖️";
            case BREAK -> "⏸️";
            case SPECIAL_EVENT -> "🎉";
            case EXAMINATION -> "📝";
            case PUBLIC_HOLIDAY -> "🎊";
            default -> "📅";
        };
    }

    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    public List<Holiday> getActiveHolidays() {
        return holidayRepository.findByActiveTrue();
    }

    public List<Holiday> getHolidaysByYear(int year) {
        return holidayRepository.findByYear(year);
    }

    public List<Holiday> getUpcomingHolidays() {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusMonths(3);
        return holidayRepository.findByHolidayDateBetween(today, futureDate);
    }

    public Optional<Holiday> getHolidayById(Long id) {
        return holidayRepository.findById(id);
    }

    public Holiday saveHoliday(Holiday holiday) {
        return holidayRepository.save(holiday);
    }

    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
    }

    public boolean isNoAttendanceDay(LocalDate date) {
        return holidayRepository.isNoAttendanceDay(date);
    }

    public List<Holiday> getHolidaysForDate(LocalDate date) {
        return holidayRepository.findActiveHolidaysForDate(date);
    }

    public List<Holiday> getNoAttendanceHolidaysForDate(LocalDate date) {
        return holidayRepository.findNoAttendanceHolidaysForDate(date);
    }

    public Map<String, Object> getHolidaySummary() {
        Map<String, Object> summary = new HashMap<>();
        LocalDate today = LocalDate.now();

        List<Holiday> upcomingHolidays = getUpcomingHolidays();
        List<Holiday> todaysHolidays = getHolidaysForDate(today);

        summary.put("upcomingCount", upcomingHolidays.size());
        summary.put("todayHolidays", todaysHolidays);
        summary.put("isNoAttendanceDay", isNoAttendanceDay(today));

        Map<String, Long> countByType = new HashMap<>();
        getActiveHolidays().forEach(holiday -> {
            String type = holiday.getType().name();
            countByType.merge(type, 1L, Long::sum);
        });
        summary.put("countByType", countByType);

        return summary;
    }

    public List<Integer> getAvailableYears() {
        return holidayRepository.findDistinctYears();
    }

    public boolean shouldMarkAttendance(LocalDate date) {
        List<Holiday> holidays = getHolidaysForDate(date);
        if (holidays.isEmpty()) {
            return true;
        }
        for (Holiday holiday : holidays) {
            if (!holiday.isNoAttendance()) {
                return true;
            }
        }
        return false;
    }

    // Add back the autoMarkAttendanceForHoliday method
    @Transactional
    public void autoMarkAttendanceForHoliday(Holiday holiday) {
        if (!holiday.isAutoMarkAttendance()) {
            throw new IllegalStateException("Auto-mark not enabled for this holiday");
        }

        List<Student> allStudents = studentService.getAllStudents();
        LocalDate currentDate = holiday.getHolidayDate();
        LocalDate endDate = holiday.getEndDate() != null ?
                holiday.getEndDate() : holiday.getHolidayDate();

        while (!currentDate.isAfter(endDate)) {
            for (Student student : allStudents) {
                try {
                    attendanceService.markAttendance(
                            student.getId(),
                            holiday.getAttendanceStatus(),
                            "Auto-marked: " + holiday.getName()
                    );
                } catch (Exception e) {
                    // Log error but continue with other students
                    System.err.println("Error auto-marking for student " + student.getId() + ": " + e.getMessage());
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    public void addDateRangeHoliday(String name, String description,
                                    LocalDate startDate, LocalDate endDate,
                                    Holiday.HolidayType type) {
        Holiday holiday = new Holiday();
        holiday.setName(name);
        holiday.setDescription(description);
        holiday.setHolidayDate(startDate);
        holiday.setEndDate(endDate);
        holiday.setType(type);

        // Set defaults based on type
        switch (type) {
            case VACATION:
                holiday.setNoAttendance(true);
                holiday.setAutoMarkAttendance(true);
                holiday.setAttendanceStatus("ABSENT");
                holiday.setAffectsResumption(true);
                holiday.setResumptionDate(endDate.plusDays(1));
                break;
            case SCHOOL_ACTIVITY:
                holiday.setNoAttendance(false);
                holiday.setAutoMarkAttendance(true);
                holiday.setAttendanceStatus("PRESENT");
                holiday.setSchoolActivity(true);
                break;
            case SPECIAL_EVENT:
                holiday.setNoAttendance(false);
                holiday.setAutoMarkAttendance(false);
                break;
            case EXAMINATION:
                holiday.setNoAttendance(false); // Allow attendance for exams
                holiday.setAutoMarkAttendance(false);
                break;
            default:
                holiday.setNoAttendance(true);
                holiday.setAutoMarkAttendance(false);
        }

        saveHoliday(holiday);
    }

    public void toggleHolidayStatus(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
        holiday.setActive(!holiday.isActive());
        saveHoliday(holiday);
    }
}