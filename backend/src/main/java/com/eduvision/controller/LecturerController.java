                                                                                                 package com.eduvision.controller;

import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.model.Course;
import com.eduvision.model.CourseLecturer;
import com.eduvision.model.Lecturer;
import com.eduvision.model.User;
import com.eduvision.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lecturers")
public class LecturerController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.eduvision.repository.UserRepository userRepository;

    @Autowired
    private com.eduvision.repository.CourseLecturerRepository courseLecturerRepository;

    @GetMapping
    public List<User> getAllLecturers() {
        return userService.findAll().stream()
            .filter(user -> user.getLecturer() != null)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public User getLecturer(@PathVariable String id) {
        return userService.findById(id).orElse(null);
    }
    

    @PostMapping
    public User createLecturer(@RequestBody User user) {
        // Assume lecturer data is in user
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public User updateLecturer(@PathVariable String id, @RequestBody User user) {
        user.setId(id);
        return userService.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteLecturer(@PathVariable String id) {
        userService.deleteById(id);
    }
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User lecturer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Map<String, Object> profile = new java.util.LinkedHashMap<>();
        profile.put("id",        lecturer.getId());
        profile.put("email",     lecturer.getEmail());
        profile.put("firstName", lecturer.getFirstName());
        profile.put("lastName",  lecturer.getLastName());
        profile.put("fullName",  lecturer.getFirstName() + " " + lecturer.getLastName());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/courses")
   public ResponseEntity<List<Map<String, String>>> getMyCourses() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User lecturer = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    List<CourseLecturer> assignments = courseLecturerRepository.findByLecturerId(lecturer.getId());
    
    List<Map<String, String>> courses = assignments.stream().map(cl -> {
        Course c = cl.getCourse();
        return Map.of(
            "courseId", c.getId(),
            "code", c.getCode(),
            "title", c.getTitle(),
            "department", c.getDepartment()
        );
    }).collect(Collectors.toList());
    
    return ResponseEntity.ok(courses);
}
}