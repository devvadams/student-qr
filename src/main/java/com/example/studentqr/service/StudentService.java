package com.example.studentqr.service;

import com.example.studentqr.model.Student;
import com.example.studentqr.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // Save or update student
    public Student saveStudent(Student student) {
        if (student.getId() == null || student.getId().isEmpty()) {
            student.setId(UUID.randomUUID().toString());
        }
        return studentRepository.save(student);
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get student by ID
    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    // Get student by roll number
    public Optional<Student> getStudentByRollNumber(String rollNumber) {
        return studentRepository.findByRollNumber(rollNumber);
    }

    // Delete student by ID
    public boolean deleteStudent(String id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Search students
    public List<Student> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }

        String searchTerm = keyword.toLowerCase();
        return getAllStudents().stream()
                .filter(student ->
                        student.getName().toLowerCase().contains(searchTerm) ||
                                student.getEmail().toLowerCase().contains(searchTerm) ||
                                student.getCourse().toLowerCase().contains(searchTerm) ||
                                student.getRollNumber().toLowerCase().contains(searchTerm))
                .toList();
    }

    // Get count of students
    public long getStudentCount() {
        return studentRepository.count();
    }
}