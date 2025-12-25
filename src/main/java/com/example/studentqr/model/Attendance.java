package com.example.studentqr.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*; // THIS IS THE KEY IMPORT FOR SPRING BOOT 3

@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "attendance_date"}))
@Data
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private String status; // PRESENT, ABSENT, LATE, EXCUSED

    private String remarks;

    @Column(name = "marked_by")
    private String markedBy;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @Column(name = "qr_scanned")
    private boolean qrScanned = false;

    // Custom constructor
    public Attendance(Student student, String status) {
        this.student = student;
        this.status = status;
        this.markedAt = LocalDateTime.now();
        this.attendanceDate = LocalDate.now();
    }

    // Custom methods
    public boolean isPresent() {
        return "PRESENT".equalsIgnoreCase(status);
    }

    public boolean isAbsent() {
        return "ABSENT".equalsIgnoreCase(status);
    }

    public String getFormattedDate() {
        return attendanceDate != null ? attendanceDate.toString() : "";
    }

    public String getFormattedTime() {
        return markedAt != null ? markedAt.toLocalTime().toString() : "";
    }
}