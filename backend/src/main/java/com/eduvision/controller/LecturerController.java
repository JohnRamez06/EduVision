                                                                                                 package com.eduvision.controller;

import com.eduvision.model.Lecturer;
import com.eduvision.model.User;
import com.eduvision.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lecturers")
public class LecturerController {

    @Autowired
    private UserService userService;

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
}