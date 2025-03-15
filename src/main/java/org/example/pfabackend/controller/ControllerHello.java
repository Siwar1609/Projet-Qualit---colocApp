package org.example.pfabackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ControllerHello {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> sayHello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, String>> sayHelloToAdmin() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello Admin");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, String>> sayHelloToUser() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello User");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/colocataire")
    public ResponseEntity<Map<String, String>> sayHelloToColocaire() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello Colocaire");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-coloc")
    public ResponseEntity<Map<String, String>> sayHelloToUserColoc() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello User or Coloc");
        return ResponseEntity.ok(response);
    }
}