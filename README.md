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
2.Build the project:
   ```bash
   ./gradlew build
3.Run the application:
   ```bash
   ./gradlew bootRun
4. Open browser: http://localhost:8080
   ```bash
Open browser: http://localhost:8080

Default Credentials
Admin: admin / admin123

Teacher: teacher / teacher123

User: user / user123

📁 Project Structure
text
student-qr-attendance-system/
├── src/main/java/com/example/studentqr/
│   ├── controller/     # MVC Controllers
│   ├── model/          # Entity classes
│   ├── repository/     # Data repositories
│   ├── service/        # Business logic
│   └── config/         # Configuration classes
├── src/main/resources/
│   ├── templates/      # Thymeleaf templates
│   ├── static/         # CSS, JS, images
│   └── application.properties
└── build.gradle        # Gradle configuration
🎯 Key Features in Detail
Holiday System
Date Ranges: Support for multi-day holidays

Activity Days: Special school days with attendance

Auto-marking: Automatic attendance marking for holidays

Calendar View: Visual holiday calendar

Attendance Features
Manual Entry: Enter student ID/roll number

QR Scanning: Scan student QR codes

Bulk Actions: Mark attendance for entire class

Reports: Daily, weekly, monthly reports

🔧 Configuration
Database
The application uses H2 in-memory database by default. To use MySQL:

Update application.properties:

properties
spring.datasource.url=jdbc:mysql://localhost:3306/studentqr
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
Email Configuration (Optional)
For email notifications, configure in application.properties:

properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
📊 API Documentation
The application provides REST APIs for:

Student management

Attendance marking

Holiday management

Report generation

🧪 Testing
Run tests:

bash
./gradlew test
🤝 Contributing
Fork the repository

Create a feature branch

Commit your changes

Push to the branch

Create a Pull Request

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

👏 Acknowledgments
Spring Boot Team

Bootstrap Team

All contributors and testers

text

## **4. Additional Files to Create:**

### **.gitignore (already added by GitHub)**
Make sure it includes:
Java
*.class
*.jar
*.war
*.ear
*.nar
*.zip
*.tar.gz
*.rar

Gradle
.gradle/
build/
out/

IDE
.idea/
*.iml
*.iws
*.ipr
.vscode/
.settings/
.classpath
.project
*.swp
*.swo

Logs
*.log
logs/

Database
*.db
*.sql

OS
.DS_Store
Thumbs.db

text

### **LICENSE (MIT)**
```text
MIT License

Copyright (c) 2024 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
5. Push Your Existing Code:
bash
# Initialize git (if not already)
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit: Student QR Attendance System"

# Add remote repository
git remote add origin https://github.com/yourusername/student-qr-attendance-system.git

# Push to GitHub
git branch -M main
git push -u origin main
6. Repository Badges (Add to README):
Add these badges to your README for a professional look:

markdown
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen)
![License](https://img.shields.io/badge/License-MIT-green)
![GitHub last commit](https://img.shields.io/github/last-commit/yourusername/student-qr-attendance-system)
7. Final Repository Structure Should Look Like:
text
student-qr-attendance-system/
├── README.md
├── LICENSE
├── .gitignore
├── build.gradle
├── gradle/
├── gradlew
├── gradlew.bat
└── src/
    └── main/
        ├── java/com/example/studentqr/
        └── resources/
