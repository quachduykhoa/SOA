package org.example.th1.controller;

import org.example.th1.dto.LoginRequest;
import org.example.th1.dto.LoginResponse;
import org.example.th1.entity.User;
import org.example.th1.service.UserService;
import org.example.th1.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Dang nhap: POST /login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.findByUserName(request.getUserName());

        // Kiem tra tai khoan / mat khau
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Sai tai khoan hoac mat khau"));
        }

        // Dung -> sinh JWT
        String token = jwtUtil.generateToken(user.getUserName());

        // Luu token vao cot Token (bang User trong phu luc)
        user.setToken(token);
        userService.save(user);

        return ResponseEntity.ok(new LoginResponse("Dang nhap thanh cong", token));
    }

    // Xac thuc token: GET /auth
    @GetMapping("/auth")
    public ResponseEntity<?> auth(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;
        try {
            String userName = jwtUtil.validateToken(token);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "userName", userName));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("valid", false, "error", e.getMessage()));
        }
    }
}