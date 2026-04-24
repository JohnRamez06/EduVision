package com.eduvision.controller;

import com.eduvision.model.User;
import com.eduvision.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    @Autowired
    private UserService userService;

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