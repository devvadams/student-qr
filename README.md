# Student QR Attendance System

A comprehensive Spring Boot application for managing student attendance using QR codes, with holiday management and role-based access control.

## 🚀 Features

### 📱 Core Features
- **QR Code Attendance** - Generate and scan QR codes for attendance
- **Student Management** - Add, edit, delete students
- **Holiday System** - Manage holidays with custom date ranges
- **Attendance Reports** - Generate detailed attendance reports

### 👥 User Roles
- **Admin** - Full system access (students, attendance, holidays)
- **Teacher** - Mark/view attendance, view students
- **User** - View students and attendance records

### 📅 Holiday Management
- Predefined holidays (public holidays, vacations)
- Custom date range holidays
- School activity days with attendance
- Automatic attendance rules based on holiday types

## 🛠️ Technology Stack

- **Backend:** Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database:** H2 (development), can be configured for MySQL/PostgreSQL
- **Frontend:** Thymeleaf, Bootstrap 5, JavaScript
- **QR Code:** QR Code generation and scanning
- **Build Tool:** Gradle

## 📦 Installation & Setup

### Prerequisites
- Java 17 or higher
- Gradle 7.x or higher
- Git

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/devvadams/student-qr.git
   cd student-qr
