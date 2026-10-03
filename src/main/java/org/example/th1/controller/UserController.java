package org.example.th1.controller;

import org.example.th1.entity.User;
import org.example.th1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // GET /api/users -> lay danh sach nguoi dung
    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    // POST /api/users -> dang ky tai khoan moi
    @PostMapping
    public User create(@RequestBody User user) {
        user.setToken(null);
        return userService.save(user);
    }
}