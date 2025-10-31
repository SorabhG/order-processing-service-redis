package com.example.order.controller;

import com.example.order.dto.LoginRequest;
import com.example.order.service.JwtUtils;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/token")
    public Map<String, String> generateToken(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String token = JwtUtils.generateToken(username);
        System.out.println("✅ Token generated for: " + username);
        return Map.of("token", token);
    }
}
