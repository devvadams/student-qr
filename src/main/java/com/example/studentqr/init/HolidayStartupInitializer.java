package com.example.studentqr.init;

import com.example.studentqr.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Year;

@Component
public class HolidayStartupInitializer implements CommandLineRunner {

    @Autowired
    private HolidayService holidayService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🎯 ========== HOLIDAY & ACTIVITY SYSTEM INITIALIZATION ==========");
        System.out.println("Year: " + Year.now().getValue());
        System.out.println("Today: " + LocalDate.now());
        System.out.println("=============================================================\n");

        try {
            // PHASE 1: INITIALIZE ALL HOLIDAYS AND DATE RANGES
            System.out.println("📦 PHASE 1: LOADING HOLIDAYS AND DATE RANGES");
            System.out.println("---------------------------------------------");

            holidayService.initializePredefinedHolidays();
            System.out.println("✅ Predefined holidays loaded");

            holidayService.initializeCustomDateRanges();
            System.out.println("✅ Custom activity/break date ranges loaded");

            // PHASE 2: SHOW CONFIGURATION SUMMARY
            System.out.println("\n📊 PHASE 2: CONFIGURATION SUMMARY");
            System.out.println("----------------------------------");

            holidayService.printAllDateRanges();

            // Count stats
            long totalHolidays = holidayService.getAllHolidays().size();
            long activeHolidays = holidayService.getActiveHolidays().size();
            long dateRanges = holidayService.getAllHolidays().stream()
                    .filter(h -> h.getEndDate() != null)
                    .count();

            System.out.println("\n📈 STATISTICS:");
            System.out.println("   • Total holidays in system: " + totalHolidays);
            System.out.println("   • Active holidays: " + activeHolidays);
            System.out.println("   • Date range events: " + dateRanges);
            System.out.println("   • Single day events: " + (totalHolidays - dateRanges));

            // PHASE 3: TEST CONFIGURATION
            System.out.println("\n🧪 PHASE 3: TESTING CONFIGURATION");
            System.out.println("----------------------------------");

            holidayService.testDateRangeConfiguration();

            // PHASE 4: CHECK TODAY'S STATUS
            System.out.println("\n📅 PHASE 4: TODAY'S STATUS CHECK");
            System.out.println("---------------------------------");

            LocalDate today = LocalDate.now();
            boolean canMarkToday = holidayService.shouldMarkAttendance(today);
            var todayEvents = holidayService.getHolidaysForDate(today);

            System.out.println("Date: " + today + " (" + today.getDayOfWeek() + ")");
            System.out.println("Attendance Marking: " + (canMarkToday ? "✅ ALLOWED" : "❌ BLOCKED"));

            if (!todayEvents.isEmpty()) {
                System.out.println("\nToday's Events:");
                todayEvents.forEach(event -> {
                    String typeIcon = getTypeIcon(event.getType());
                    String attendanceStatus = event.isNoAttendance() ? "❌ No Attendance" : "✅ Attendance Allowed";
                    System.out.println("  " + typeIcon + " " + event.getName() +
                            " [" + event.getType() + "] - " + attendanceStatus);
                    if (event.getEndDate() != null && !event.getHolidayDate().equals(event.getEndDate())) {
                        System.out.println("    Duration: " + event.getHolidayDate() + " to " + event.getEndDate());
                    }
                });
            } else {
                System.out.println("\nNo scheduled events today - Regular school day");
            }

            // PHASE 5: UPCOMING EVENTS (Next 2 weeks)
            System.out.println("\n🔮 PHASE 5: UPCOMING EVENTS (Next 2 weeks)");
            System.out.println("-------------------------------------------");

            LocalDate nextTwoWeeks = today.plusWeeks(2);
            holidayService.getActiveHolidays().stream()  // Changed from getAllHolidays() to getActiveHolidays()
                    .filter(h -> !h.getHolidayDate().isBefore(today) && !h.getHolidayDate().isAfter(nextTwoWeeks))
                    .sorted((h1, h2) -> h1.getHolidayDate().compareTo(h2.getHolidayDate()))
                    .forEach(h -> {
                        String typeIcon = getTypeIcon(h.getType());
                        String daysUntil = "(" + today.until(h.getHolidayDate()).getDays() + " days)";
                        String attendanceStatus = h.isNoAttendance() ? "❌" : "✅";

                        if (h.getEndDate() != null && !h.getHolidayDate().equals(h.getEndDate())) {
                            System.out.println("  " + typeIcon + " " + h.getName() + " " + daysUntil);
                            System.out.println("    📅 " + h.getHolidayDate() + " to " + h.getEndDate());
                            System.out.println("    " + attendanceStatus + " Attendance: " +
                                    (h.isNoAttendance() ? "Disabled" : "Enabled"));
                        } else {
                            System.out.println("  " + typeIcon + " " + h.getName() +
                                    " - " + h.getHolidayDate() + " " + daysUntil);
                            System.out.println("    " + attendanceStatus + " Attendance: " +
                                    (h.isNoAttendance() ? "Disabled" : "Enabled"));
                        }
                        System.out.println();
                    });

            // FINAL STATUS
            System.out.println("\n🎉 ========== INITIALIZATION COMPLETE ==========");
            System.out.println("✅ Holiday & Activity system is fully operational");
            System.out.println("✅ Custom date ranges are active");
            System.out.println("✅ Attendance rules are configured");
            System.out.println("================================================\n");

            // Quick tip
            System.out.println("💡 TIP: To modify date ranges, edit the");
            System.out.println("       initializeCustomDateRanges() method");
            System.out.println("       in HolidayService.java\n");

        } catch (Exception e) {
            System.err.println("\n❌❌❌ ERROR INITIALIZING HOLIDAY SYSTEM ❌❌❌");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("❌❌❌ INITIALIZATION FAILED ❌❌❌\n");
        }
    }

    private String getTypeIcon(com.example.studentqr.model.Holiday.HolidayType type) {
        return switch (type) {
            case SCHOOL_ACTIVITY -> "🏫";
            case VACATION -> "🏖️";
            case BREAK -> "⏸️";
            case SPECIAL_EVENT -> "🎉";
            case EXAMINATION -> "📝";
            case PUBLIC_HOLIDAY -> "🎊";
            case CUSTOM -> "📌";
            default -> "📅";
        };
    }
}