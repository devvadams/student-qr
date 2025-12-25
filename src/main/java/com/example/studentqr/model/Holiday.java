package com.example.studentqr.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HolidayType type = HolidayType.PUBLIC_HOLIDAY;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "no_attendance", nullable = false)
    private boolean noAttendance = true;

    @Column(name = "recurring_yearly")
    private boolean recurringYearly = false;

    @Column(name = "auto_mark_attendance")
    private boolean autoMarkAttendance = false;

    @Column(name = "attendance_status")
    private String attendanceStatus = "ABSENT";

    @Column(name = "affects_resumption")
    private boolean affectsResumption = false;

    @Column(name = "resumption_date")
    private LocalDate resumptionDate;

    @Column(name = "is_school_activity")
    private boolean schoolActivity = false;

    @Column(name = "activity_description", length = 1000)
    private String activityDescription;

    // Constructors
    public Holiday() {}

    public Holiday(String name, String description, LocalDate holidayDate, String type) {
        this.name = name;
        this.description = description;
        this.holidayDate = holidayDate;
        this.type = HolidayType.valueOf(type);
    }

    public Holiday(String name, String description, LocalDate startDate, LocalDate endDate, String type) {
        this.name = name;
        this.description = description;
        this.holidayDate = startDate;
        this.endDate = endDate;
        this.type = HolidayType.valueOf(type);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public HolidayType getType() { return type; }
    public void setType(HolidayType type) { this.type = type; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isNoAttendance() { return noAttendance; }
    public void setNoAttendance(boolean noAttendance) { this.noAttendance = noAttendance; }

    public boolean isRecurringYearly() { return recurringYearly; }
    public void setRecurringYearly(boolean recurringYearly) { this.recurringYearly = recurringYearly; }

    public boolean isAutoMarkAttendance() { return autoMarkAttendance; }
    public void setAutoMarkAttendance(boolean autoMarkAttendance) { this.autoMarkAttendance = autoMarkAttendance; }

    public String getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public boolean isAffectsResumption() { return affectsResumption; }
    public void setAffectsResumption(boolean affectsResumption) { this.affectsResumption = affectsResumption; }

    public LocalDate getResumptionDate() { return resumptionDate; }
    public void setResumptionDate(LocalDate resumptionDate) { this.resumptionDate = resumptionDate; }

    public boolean isSchoolActivity() { return schoolActivity; }
    public void setSchoolActivity(boolean schoolActivity) { this.schoolActivity = schoolActivity; }

    public String getActivityDescription() { return activityDescription; }
    public void setActivityDescription(String activityDescription) { this.activityDescription = activityDescription; }

    // Helper methods
    public boolean isDateInRange(LocalDate date) {
        if (endDate == null) {
            return date.equals(holidayDate);
        }
        return !date.isBefore(holidayDate) && !date.isAfter(endDate);
    }

    public String getFormattedDateRange() {
        if (endDate == null || endDate.equals(holidayDate)) {
            return holidayDate.toString();
        }
        return holidayDate.toString() + " to " + endDate.toString();
    }

    public int getNumberOfDays() {
        if (endDate == null) {
            return 1;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(holidayDate, endDate) + 1;
    }

    public boolean isMultiDay() {
        return endDate != null && !endDate.equals(holidayDate);
    }

    public String getTypeDisplayName() {
        return type.getDisplayName();
    }

    public String getTypeBadgeClass() {
        switch (type) {
            case PUBLIC_HOLIDAY: return "bg-danger";
            case SCHOOL_HOLIDAY: return "bg-warning";
            case VACATION: return "bg-info";
            case SPECIAL_EVENT: return "bg-success";
            case SCHOOL_ACTIVITY: return "bg-primary";
            case EXAMINATION: return "bg-secondary";
            case BREAK: return "bg-dark";
            default: return "bg-secondary";
        }
    }

    // Enum
    public enum HolidayType {
        PUBLIC_HOLIDAY("Public Holiday"),
        SCHOOL_HOLIDAY("School Holiday"),
        VACATION("Vacation"),
        SPECIAL_EVENT("Special Event"),
        SCHOOL_ACTIVITY("School Activity"),
        EXAMINATION("Examination"),
        BREAK("Break"),
        CUSTOM("Custom");

        private final String displayName;

        HolidayType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}