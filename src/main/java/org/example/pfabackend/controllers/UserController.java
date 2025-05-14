package org.example.pfabackend.controllers;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
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
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @RequestBody Map<String, Object> userDetails,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Map<String, Object> response = new HashMap<>();

        // Vérification du token d'autorisation
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.put("status", "error");
            response.put("message", "Authorization header is missing or invalid. Please provide a valid Bearer token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String accessToken = authorizationHeader.replace("Bearer ", "");

        // Étape 1 : Vérification de l'identité de l'utilisateur
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> userInfoRequest = new HttpEntity<>(headers);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "http://localhost:8080/realms/PFARealm/protocol/openid-connect/userinfo",
                    HttpMethod.GET,
                    userInfoRequest,
                    Map.class
            );

            String authenticatedUserId = (String) userInfoResponse.getBody().get("sub");

            if (!authenticatedUserId.equals(userId)) {
                response.put("status", "error");
                response.put("message", "Access denied. You are not authorized to update this user's profile.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

        } catch (HttpStatusCodeException ex) {
            response.put("status", "error");
            response.put("message", "Failed to validate user identity with Keycloak: " + ex.getStatusText());
            return ResponseEntity.status(ex.getStatusCode()).body(response);
        } catch (Exception ex) {
            response.put("status", "error");
            response.put("message", "Unexpected error during identity verification: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        // Étape 2 : Mise à jour de l'utilisateur dans Keycloak
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Set<String> simpleFields = Set.of("username", "firstName", "lastName", "enabled");
            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> attributes = new HashMap<>();

            for (Map.Entry<String, Object> entry : userDetails.entrySet()) {
                if (simpleFields.contains(entry.getKey())) {
                    payload.put(entry.getKey(), entry.getValue());
                } else {
                    attributes.put(entry.getKey(), entry.getValue());
                }
            }

            payload.put("attributes", attributes);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

            String keycloakUpdateUserUrl = "http://localhost:8080/admin/realms/PFARealm/users/" + userId;
            restTemplate.exchange(keycloakUpdateUserUrl, HttpMethod.PUT, requestEntity, Void.class);

            response.put("status", "success");
            response.put("message", "User profile updated successfully.");
            return ResponseEntity.ok(response);

        } catch (HttpStatusCodeException ex) {
            response.put("status", "error");
            response.put("message", "Failed to update user in Keycloak: " +
                    (ex.getResponseBodyAsString().isEmpty() ? ex.getStatusText() : ex.getResponseBodyAsString()));
            return ResponseEntity.status(ex.getStatusCode()).body(response);
        } catch (Exception ex) {
            response.put("status", "error");
            response.put("message", "Unexpected error while updating user: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    /**
     * Endpoint to retrieve user details by ID from Keycloak.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Map<String, Object> response = new HashMap<>();

        // Vérification du token d'autorisation
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.put("status", HttpStatus.UNAUTHORIZED.value());
            response.put("error", "Unauthorized");
            response.put("message", "Authorization header is missing or invalid. Please provide a valid Bearer token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String accessToken = authorizationHeader.replace("Bearer ", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String keycloakGetUserUrl = "http://localhost:8080/admin/realms/PFARealm/users/" + userId;

        try {
            // Envoi de la requête à Keycloak pour récupérer les informations de l'utilisateur
            ResponseEntity<Map> keycloakResponse = restTemplate.exchange(
                    keycloakGetUserUrl,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            // Si la réponse est réussie, renvoi des données de l'utilisateur sous forme aplatie
            Map<String, Object> user = keycloakResponse.getBody();


            return ResponseEntity.ok(user);

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Gestion des erreurs liées à Keycloak (Client/Server Error)
            response.put("status", ex.getStatusCode().value());
            response.put("error", "Keycloak Error");
            response.put("message", ex.getResponseBodyAsString().isEmpty()
                    ? ex.getStatusText()
                    : ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode()).body(response);

        } catch (HttpStatusCodeException ex) {
            // Cas où le code d'état HTTP est spécifié mais différent des erreurs spécifiques
            response.put("status", ex.getStatusCode().value());
            response.put("error", ex.getStatusText());
            response.put("message", "Error occurred while contacting Keycloak. Response: " + ex.getResponseBodyAsString());
            return ResponseEntity.status(ex.getStatusCode()).body(response);

        } catch (Exception ex) {
            // Gestion des autres exceptions inattendues
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Unexpected error");
            response.put("message", "An unexpected error occurred: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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