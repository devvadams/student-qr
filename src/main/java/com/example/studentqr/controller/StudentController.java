package com.example.studentqr.controller;

import com.example.studentqr.model.Student;
import com.example.studentqr.service.StudentService;
import com.example.studentqr.util.QRCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private QRCodeUtil qrCodeUtil;

    @Autowired
    private StudentService studentService;

    // ==== SHOW ALL STUDENTS ====
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER', 'TEACHER', 'ADMIN')")
    public String listStudents(Model model,
                               @RequestParam(required = false) String search) {
        List<Student> students;

        if (search != null && !search.trim().isEmpty()) {
            students = studentService.searchStudents(search);
            model.addAttribute("searchTerm", search);
        } else {
            students = studentService.getAllStudents();
        }

        // Create maps for QR codes and photo thumbnails
        Map<String, String> qrCodeMap = new HashMap<>();
        Map<String, String> photoThumbnailMap = new HashMap<>();

        for (Student student : students) {
            try {
                // Generate QR code data
                String studentData = String.format(
                        "ID: %s\nName: %s\nEmail: %s\nCourse: %s\nRoll: %s",
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getCourse(),
                        student.getRollNumber()
                );

                // Generate small QR code for list view
                String smallQR = qrCodeUtil.generateQRCodeBase64(studentData, 100, 100);
                qrCodeMap.put(student.getId(), smallQR);

                // Generate photo thumbnail if exists
                if (student.hasPhoto()) {
                    String thumbnail = qrCodeUtil.generatePhotoThumbnail(student.getPhotoBase64(), 100);
                    if (thumbnail != null) {
                        photoThumbnailMap.put(student.getId(), thumbnail);
                    }
                }

            } catch (Exception e) {
                qrCodeMap.put(student.getId(), "error");
                System.err.println("Error processing student " + student.getId() + ": " + e.getMessage());
            }
        }

        model.addAttribute("students", students);
        model.addAttribute("qrCodeMap", qrCodeMap);
        model.addAttribute("photoThumbnailMap", photoThumbnailMap);
        model.addAttribute("studentCount", students.size());
        model.addAttribute("totalCount", studentService.getStudentCount());

        return "student-list";
    }

    // ==== SHOW STUDENT FORM WITH WEBCAM ====
    @GetMapping("/form")
    @PreAuthorize("hasRole('ADMIN')")
    public String showStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "student-form-webcam";
    }

    // ==== GENERATE QR CODE WITH PHOTO ====
    @PostMapping("/generate-qr")
    @PreAuthorize("hasRole('ADMIN')")
    public String generateQRCode(@ModelAttribute Student student,
                                 @RequestParam(required = false) String capturedPhoto,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        try {
            // Process photo
            String processedPhoto = null;
            if (capturedPhoto != null && !capturedPhoto.isEmpty()) {
                if (capturedPhoto.contains(",")) {
                    processedPhoto = capturedPhoto.split(",")[1];
                } else {
                    processedPhoto = capturedPhoto;
                }
            }
            student.setPhotoBase64(processedPhoto);

            // Save student
            Student savedStudent = studentService.saveStudent(student);

            // Generate QR code
            String filePath = qrCodeUtil.generateStudentQRCode(savedStudent);
            savedStudent.setQrCodePath(filePath);
            studentService.saveStudent(savedStudent);

            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", "Student added with photo and QR code generated!");
            return "redirect:/student/list";

        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("student", student);
            return "student-form-webcam";
        }
    }

    // ==== VIEW SINGLE STUDENT WITH PHOTO ====
    @GetMapping("/view/{id}")
    public String viewStudent(@PathVariable String id, Model model) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            model.addAttribute("student", student);
            model.addAttribute("hasPhoto", student.hasPhoto());

            try {
                String studentData = String.format(
                        "ID: %s\nName: %s\nEmail: %s\nCourse: %s\nRoll: %s",
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getCourse(),
                        student.getRollNumber()
                );

                String qrCodeBase64 = qrCodeUtil.generateQRCodeBase64(studentData, 300, 300);
                model.addAttribute("qrCodeBase64", qrCodeBase64);

                if (student.hasPhoto()) {
                    String photoThumbnail = qrCodeUtil.generatePhotoThumbnail(student.getPhotoBase64(), 300);
                    model.addAttribute("photoThumbnail", photoThumbnail);
                }

            } catch (Exception e) {
                model.addAttribute("error", "Could not generate QR code: " + e.getMessage());
            }
            return "student-view";
        }
        return "redirect:/student/list";
    }

    // ==== UPDATE STUDENT PHOTO ====
    @GetMapping("/update-photo/{id}")
    public String showUpdatePhotoForm(@PathVariable String id, Model model) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isPresent()) {
            model.addAttribute("student", studentOpt.get());
            return "update-photo";
        }
        return "redirect:/student/list";
    }

    @PostMapping("/update-photo/{id}")
    public String updatePhoto(@PathVariable String id,
                              @RequestParam String capturedPhoto,
                              RedirectAttributes redirectAttributes) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            try {
                if (capturedPhoto != null && !capturedPhoto.isEmpty()) {
                    // Process the photo
                    String processedPhoto = capturedPhoto;
                    if (processedPhoto.contains(",")) {
                        processedPhoto = processedPhoto.split(",")[1];
                    }

                    student.setPhotoBase64(processedPhoto);
                    studentService.saveStudent(student);

                    redirectAttributes.addFlashAttribute("success", true);
                    redirectAttributes.addFlashAttribute("message", "Photo updated successfully!");
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Error updating photo: " + e.getMessage());
            }
            return "redirect:/student/view/" + id;
        }
        return "redirect:/student/list";
    }

    // ==== DELETE STUDENT ====
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteStudent(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            try {
                // Delete QR code file if exists
                if (student.getQrCodePath() != null) {
                    qrCodeUtil.deleteQRCodeFile(student.getQrCodePath());
                }

                // Delete student from storage
                studentService.deleteStudent(id);

                redirectAttributes.addFlashAttribute("success", true);
                redirectAttributes.addFlashAttribute("message",
                        "Student '" + student.getName() + "' deleted successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error",
                        "Error deleting student: " + e.getMessage());
            }
        }
        return "redirect:/student/list";
    }

    // ==== DOWNLOAD QR CODE ====
    @GetMapping("/download/{id}")
    public String downloadQRCode(@PathVariable String id, Model model) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            model.addAttribute("student", student);

            try {
                if (student.getQrCodePath() == null) {
                    String filePath = qrCodeUtil.generateStudentQRCode(student);
                    student.setQrCodePath(filePath);
                    studentService.saveStudent(student);
                    model.addAttribute("qrCodePath", filePath);
                } else {
                    model.addAttribute("qrCodePath", student.getQrCodePath());
                }

                String studentData = String.format(
                        "ID: %s\nName: %s\nEmail: %s\nCourse: %s\nRoll: %s",
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getCourse(),
                        student.getRollNumber()
                );
                String qrCodeBase64 = qrCodeUtil.generateQRCodeBase64(studentData, 300, 300);
                model.addAttribute("qrCodeBase64", qrCodeBase64);

            } catch (Exception e) {
                model.addAttribute("error", "Error: " + e.getMessage());
            }

            return "download-qr";
        }
        return "redirect:/student/list";
    }

    // ==== REGENERATE QR CODE ====
    @GetMapping("/regenerate/{id}")
    public String regenerateQRCode(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            try {
                if (student.getQrCodePath() != null) {
                    qrCodeUtil.deleteQRCodeFile(student.getQrCodePath());
                }

                String newFilePath = qrCodeUtil.generateStudentQRCode(student);
                student.setQrCodePath(newFilePath);
                studentService.saveStudent(student);

                redirectAttributes.addFlashAttribute("success", true);
                redirectAttributes.addFlashAttribute("message", "QR Code regenerated successfully!");

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error",
                        "Error regenerating QR code: " + e.getMessage());
            }
            return "redirect:/student/view/" + id;
        }
        return "redirect:/student/list";
    }

    // ==== REST API ENDPOINTS ====
    @PostMapping("/api/generate")
    @ResponseBody
    public Map<String, Object> generateQRCodeAPI(@RequestBody Student student) {
        Map<String, Object> response = new HashMap<>();

        try {
            Student savedStudent = studentService.saveStudent(student);
            String filePath = qrCodeUtil.generateStudentQRCode(savedStudent);
            savedStudent.setQrCodePath(filePath);
            studentService.saveStudent(savedStudent);

            String qrCodeBase64 = qrCodeUtil.generateQRCodeBase64(savedStudent.toString(), 300, 300);

            response.put("status", "success");
            response.put("message", "QR Code generated successfully");
            response.put("filePath", filePath);
            response.put("qrCodeBase64", qrCodeBase64);
            response.put("student", savedStudent);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }

    @GetMapping("/api/students")
    @ResponseBody
    public List<Student> getAllStudentsAPI() {
        return studentService.getAllStudents();
    }
}