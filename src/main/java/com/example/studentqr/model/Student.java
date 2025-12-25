package com.example.studentqr.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String course;

    @Column(name = "roll_number", nullable = false, unique = true)
    private String rollNumber;

    @Column(name = "photo_base64", length = 1000000) // For large base64 strings
    private String photoBase64;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "qr_code_path")
    private String qrCodePath;

    // Custom constructor
    public Student(String id, String name, String email, String course, String rollNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.course = course;
        this.rollNumber = rollNumber;
        this.createdAt = LocalDateTime.now();
    }

    // Custom methods
    public String getFormattedDate() {
        return createdAt != null ? createdAt.toString().replace("T", " ") : "";
    }

    public boolean hasPhoto() {
        return photoBase64 != null && !photoBase64.isEmpty() && photoBase64.length() > 100;
    }
}