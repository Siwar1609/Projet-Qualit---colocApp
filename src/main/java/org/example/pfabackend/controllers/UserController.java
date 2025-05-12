package org.example.pfabackend.controllers;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RestTemplate restTemplate;

    public UserController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Endpoint to retrieve user information from Keycloak.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authorizationHeader) {
        // Extract the access token from the Authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String accessToken = authorizationHeader.replace("Bearer ", "");

        // Set headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // Create the HTTP entity
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request to Keycloak
        String keycloakUserInfoUrl = "http://localhost:8080/realms/PFARealm/protocol/openid-connect/userinfo";
        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    keycloakUserInfoUrl,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );
        } catch (HttpClientErrorException e) {
            // Log detailed error for debugging
            throw new RuntimeException("Failed to fetch user info from Keycloak: " + e.getMessage(), e);
        }

        // Return the response from Keycloak
        return ResponseEntity.ok(response.getBody());
    }
    /**
     * Endpoint to update user details in Keycloak.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(
            @PathVariable String userId,
            @RequestBody Map<String, Object> userDetails,
            @RequestHeader("Authorization") String authorizationHeader) {

        // Extract the access token from the Authorization header
        String accessToken = authorizationHeader.replace("Bearer ", "");

        // Set headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the HTTP entity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(userDetails, headers);

        // Send the request to Keycloak
        String keycloakUpdateUserUrl = "http://localhost:8080/admin/realms/PFARealm/users/" + userId;
        restTemplate.exchange(
                keycloakUpdateUserUrl,
                HttpMethod.PUT,
                requestEntity,
                Void.class
        );

        // Return a success response
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to retrieve user details by ID from Keycloak.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authorizationHeader) {

        // Extract the access token from the Authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String accessToken = authorizationHeader.replace("Bearer ", "");

        // Set headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // Create the HTTP entity
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request to Keycloak
        String keycloakGetUserUrl = "http://localhost:8080/admin/realms/PFARealm/users/" + userId;
        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    keycloakGetUserUrl,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );
        } catch (HttpClientErrorException e) {
            // Log detailed error for debugging
            throw new RuntimeException("Failed to fetch user details from Keycloak: " + e.getMessage(), e);
        }

        // Return the response from Keycloak
        return ResponseEntity.ok(response.getBody());
    }

    /**
     * Endpoint to retrieve all users from Keycloak with optional search parameters.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestHeader("Authorization") String authorizationHeader) {

        // Extract the access token from the Authorization header
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String accessToken = authorizationHeader.replace("Bearer ", "");

        // Set headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        // Build the URL with query parameters
        StringBuilder urlBuilder = new StringBuilder("http://localhost:8080/admin/realms/PFARealm/users?");
        if (username != null) {
            urlBuilder.append("username=").append(username).append("&");
        }
        if (email != null) {
            urlBuilder.append("email=").append(email).append("&");
        }
        if (firstName != null) {
            urlBuilder.append("firstName=").append(firstName).append("&");
        }
        if (lastName != null) {
            urlBuilder.append("lastName=").append(lastName).append("&");
        }

        // Remove the trailing '&' if any parameters were added
        String keycloakGetUsersUrl = urlBuilder.toString().replaceAll("&$", "");

        // Create the HTTP entity
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // Send the request to Keycloak
        ResponseEntity<List> response;
        try {
            response = restTemplate.exchange(
                    keycloakGetUsersUrl,
                    HttpMethod.GET,
                    requestEntity,
                    List.class
            );
        } catch (HttpClientErrorException e) {
            // Log detailed error for debugging
            throw new RuntimeException("Failed to fetch users from Keycloak: " + e.getMessage(), e);
        }

        // Return the response from Keycloak
        return ResponseEntity.ok(response.getBody());
    }
    @PostMapping("/add")
    public ResponseEntity<String> addUser(
            @RequestBody Map<String, Object> simpleUserData,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid or missing Authorization header");
        }
        String accessToken = authorizationHeader.replace("Bearer ", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Transformation du JSON simple vers le format attendu par Keycloak
        Map<String, Object> keycloakUser = new HashMap<>();
        keycloakUser.put("enabled", true);
        keycloakUser.put("username", simpleUserData.get("username"));
        keycloakUser.put("email", simpleUserData.get("email"));
        keycloakUser.put("firstName", simpleUserData.get("firstName"));
        keycloakUser.put("lastName", simpleUserData.get("lastName"));

        // Création des credentials
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", simpleUserData.get("password"));
        credentials.put("temporary", false);
        keycloakUser.put("credentials", Collections.singletonList(credentials));

        // Création des attributs
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("locale", Collections.singletonList("en"));
        attributes.put("gender", simpleUserData.get("gender"));
        attributes.put("birth_date", simpleUserData.get("birthDate"));
        attributes.put("phone_number", simpleUserData.get("phoneNumber"));
        keycloakUser.put("attributes", attributes);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(keycloakUser, headers);
        String keycloakCreateUserUrl = "http://localhost:8080/admin/realms/PFARealm/users";

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    keycloakCreateUserUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                return ResponseEntity.status(HttpStatus.CREATED).body("User successfully created.");
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to create user.");
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST &&
                    e.getResponseBodyAsString().contains("User exists with same email")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User already exists with the same email.");
            }
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }


}