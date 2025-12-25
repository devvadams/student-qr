package com.example.studentqr.util;

import com.example.studentqr.model.Student;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class QRCodeUtil {

    @Value("${qr.directory:qr-codes}")
    private String qrDirectory;

    @PostConstruct
    public void init() {
        try {
            Path dirPath = Paths.get(qrDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                System.out.println("Created QR directory: " + dirPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error creating QR directory: " + e.getMessage());
        }
    }

    // Generate QR code and save to file
    public String generateQRCodeImage(String text, int width, int height, String fileName) throws Exception {
        try {
            Path dirPath = Paths.get(qrDirectory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String cleanFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            Path path = Paths.get(qrDirectory + "/" + cleanFileName);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            System.out.println("QR code saved to: " + path.toAbsolutePath());
            return path.toAbsolutePath().toString();

        } catch (Exception e) {
            throw new Exception("Failed to generate QR code image: " + e.getMessage(), e);
        }
    }

    // Generate QR code and return as Base64 string
    public String generateQRCodeBase64(String text, int width, int height) throws Exception {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);

            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return base64;

        } catch (Exception e) {
            throw new Exception("Failed to generate QR code Base64: " + e.getMessage(), e);
        }
    }

    // Generate QR code for student with photo info
    public String generateStudentQRCode(Student student) throws Exception {
        StringBuilder studentData = new StringBuilder();

        studentData.append("=== STUDENT INFORMATION ===\n");
        studentData.append("ID: ").append(student.getId()).append("\n");
        studentData.append("Name: ").append(student.getName()).append("\n");
        studentData.append("Email: ").append(student.getEmail()).append("\n");
        studentData.append("Course: ").append(student.getCourse()).append("\n");
        studentData.append("Roll Number: ").append(student.getRollNumber()).append("\n");
        studentData.append("Generated: ").append(java.time.LocalDateTime.now()).append("\n");

        if (student.hasPhoto()) {
            studentData.append("Photo: Available (Base64 length: ").append(student.getPhotoBase64().length()).append(")\n");
        } else {
            studentData.append("Photo: Not available\n");
        }

        studentData.append("===========================");

        String fileName = "student_" + student.getRollNumber() + "_" + System.currentTimeMillis() + ".png";
        return generateQRCodeImage(studentData.toString(), 350, 350, fileName);
    }

    // Read QR code file as Base64
    public String getQRCodeAsBase64(String filePath) throws Exception {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new RuntimeException("QR Code file not found: " + filePath);
        }

        byte[] fileContent = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(fileContent);
    }

    // Delete QR code file
    public boolean deleteQRCodeFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (Exception e) {
            return false;
        }
    }

    // Generate photo thumbnail (for list view)
    public String generatePhotoThumbnail(String photoBase64, int maxWidth) throws Exception {
        if (photoBase64 == null || photoBase64.isEmpty()) {
            return null;
        }

        try {
            // Decode Base64 to image
            byte[] imageBytes = Base64.getDecoder().decode(photoBase64);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bais);

            if (originalImage == null) {
                return null;
            }

            // Calculate new dimensions
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int newWidth = maxWidth;
            int newHeight = (int) ((double) originalHeight / originalWidth * maxWidth);

            // Create thumbnail
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // Convert back to Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "JPEG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            System.err.println("Error generating thumbnail: " + e.getMessage());
            return null;
        }
    }
}