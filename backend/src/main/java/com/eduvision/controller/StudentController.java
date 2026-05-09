package com.eduvision.controller;

import com.eduvision.model.User;
import com.eduvision.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    @Autowired
    private UserService userService;

    @Value("${eduvision.face-enrollment-dir:./face_enrollment}")
    private String enrollmentBaseDir;

    /**
     * Serves the enrollment photo for a given student number.
     * GET /api/v1/students/{studentNumber}/photo
     */
    @GetMapping("/{studentNumber}/photo")
    public ResponseEntity<byte[]> getStudentPhoto(@PathVariable String studentNumber) {
        Path photoPath = Paths.get(enrollmentBaseDir)
                .resolve(studentNumber)
                .resolve("photo_1.jpg");
        if (!Files.exists(photoPath)) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] bytes = Files.readAllBytes(photoPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public List<User> getAllStudents() {
        return userService.findAll().stream()
            .filter(user -> user.getStudent() != null)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public User getStudent(@PathVariable String id) {
        return userService.findById(id).orElse(null);
    }

    @PostMapping
    public User createStudent(@RequestBody User user) {
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public User updateStudent(@PathVariable String id, @RequestBody User user) {
        user.setId(id);
        return userService.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable String id) {
        userService.deleteById(id);
    }
}
