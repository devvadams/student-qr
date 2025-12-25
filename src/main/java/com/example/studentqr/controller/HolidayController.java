package com.example.studentqr.controller;

import com.example.studentqr.model.Holiday;
import com.example.studentqr.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/holidays")
@PreAuthorize("hasRole('ADMIN')")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @GetMapping
    public String viewHolidays(Model model,
                               @RequestParam(required = false) Integer year,
                               @RequestParam(required = false) String type) {

        int selectedYear = (year != null) ? year : LocalDate.now().getYear();
        String selectedType = type;

        List<Holiday> holidays;
        if (selectedType != null && !selectedType.isEmpty()) {
            holidays = holidayService.getAllHolidays().stream()
                    .filter(h -> h.getType().name().equals(selectedType))
                    .filter(h -> h.getHolidayDate().getYear() == selectedYear)
                    .toList();
        } else {
            holidays = holidayService.getHolidaysByYear(selectedYear);
        }

        model.addAttribute("holidays", holidays);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("availableYears", holidayService.getAvailableYears());
        model.addAttribute("holidaySummary", holidayService.getHolidaySummary());

        return "holidays";
    }

    @GetMapping("/add")
    public String showAddHolidayForm(Model model) {
        model.addAttribute("holiday", new Holiday());
        return "add-holiday";
    }

    @GetMapping("/create")
    public String showCreateHolidayForm(Model model) {
        model.addAttribute("holiday", new Holiday());
        model.addAttribute("holidayTypes", Holiday.HolidayType.values());
        return "add-holiday";
    }

    @PostMapping("/add")
    public String addHoliday(@ModelAttribute Holiday holiday,
                             RedirectAttributes redirectAttributes) {
        try {
            // Set defaults if not set
            if (holiday.getEndDate() == null) {
                holiday.setEndDate(holiday.getHolidayDate());
            }

            if (holiday.getType() == Holiday.HolidayType.VACATION) {
                holiday.setAutoMarkAttendance(true);
                holiday.setAttendanceStatus("ABSENT");
                holiday.setAffectsResumption(true);
                if (holiday.getResumptionDate() == null && holiday.getEndDate() != null) {
                    holiday.setResumptionDate(holiday.getEndDate().plusDays(1));
                }
            }

            holidayService.saveHoliday(holiday);

            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message", "Holiday added successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/holidays";
    }

    @GetMapping("/edit/{id}")
    public String showEditHolidayForm(@PathVariable Long id, Model model) {
        Holiday holiday = holidayService.getHolidayById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
        model.addAttribute("holiday", holiday);
        return "add-holiday";
    }

    @PostMapping("/update/{id}")
    public String updateHoliday(@PathVariable Long id,
                                @ModelAttribute Holiday holiday,
                                RedirectAttributes redirectAttributes) {
        try {
            holiday.setId(id);
            holidayService.saveHoliday(holiday);

            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message", "Holiday updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/holidays";
    }

    @GetMapping("/delete/{id}")
    public String deleteHoliday(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            holidayService.deleteHoliday(id);
            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message", "Holiday deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/holidays";
    }

    @GetMapping("/toggle/{id}")
    public String toggleHolidayStatus(@PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {
        try {
            holidayService.toggleHolidayStatus(id);
            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message", "Holiday status toggled successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/holidays";
    }

    @GetMapping("/auto-mark/{id}")
    public String autoMarkAttendanceForHoliday(@PathVariable Long id,
                                               RedirectAttributes redirectAttributes) {
        try {
            Holiday holiday = holidayService.getHolidayById(id)
                    .orElseThrow(() -> new RuntimeException("Holiday not found"));

            holidayService.autoMarkAttendanceForHoliday(holiday);

            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message",
                    "Attendance auto-marked for " + holiday.getName() + " holiday!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/holidays";
    }

    @GetMapping("/initialize")
    public String initializeHolidays(RedirectAttributes redirectAttributes) {
        try {
            holidayService.initializePredefinedHolidays();
            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message",
                    "Predefined holidays initialized successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/holidays";
    }


    @GetMapping("/calendar")
    public String viewCalendar(Model model,
                               @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        model.addAttribute("selectedYear", targetYear);
        model.addAttribute("holidays", holidayService.getHolidaysByYear(targetYear));
        model.addAttribute("currentYear", LocalDate.now().getYear());
        return "calendar";
    }

    @PostMapping("/bulk-add")
    public String addBulkHolidays(@RequestParam String name,
                                  @RequestParam String description,
                                  @RequestParam LocalDate startDate,
                                  @RequestParam LocalDate endDate,
                                  @RequestParam Holiday.HolidayType type,
                                  RedirectAttributes redirectAttributes) {
        try {
            holidayService.addDateRangeHoliday(name, description, startDate, endDate, type);
            redirectAttributes.addAttribute("success", true);
            redirectAttributes.addAttribute("message", "Holiday range added successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/holidays";
    }
}