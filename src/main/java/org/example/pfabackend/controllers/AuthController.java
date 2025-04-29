package org.example.pfabackend.controllers;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RestTemplate restTemplate;

    public AuthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        // Extract username and password from request body
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Prepare the request body for Keycloak
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "password");
        requestBody.add("client_id", "pfa-client-frontend");
        requestBody.add("username", username);
        requestBody.add("password", password);
        requestBody.add("client_secret", "aq2uNxZCnVfyrz0gP4nxcWk1HWnOLog4");

        // Set headers for x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Create the HTTP entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send the request to Keycloak
        String keycloakTokenUrl = "http://localhost:8080/realms/PFARealm/protocol/openid-connect/token";
        ResponseEntity<Map> response = restTemplate.exchange(
                keycloakTokenUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // Return the response from Keycloak
        return ResponseEntity.ok(response.getBody());
    }
}