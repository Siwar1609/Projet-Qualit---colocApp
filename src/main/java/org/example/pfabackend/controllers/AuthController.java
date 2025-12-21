// ==============================
// AuthController.java
// ==============================

package org.example.pfabackend.controllers;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RestTemplate restTemplate;

    // ==============================
    // Secrets et identifiants Keycloak
    // Utiliser les variables d'environnement pour plus de sécurité
    // ==============================
    private static final String CLIENT_ID = System.getenv("KEYCLOAK_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("KEYCLOAK_CLIENT_SECRET");
    private static final String REALM = System.getenv("KEYCLOAK_REALM");

    // Pattern pour valider l'UUID de userId (éviter CWE-22 / Path Traversal)
    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");

    // Définition des rôles autorisés côté serveur
    static final Map<String, Map<String, Object>> ROLE_MAP = Map.of(
            "admin", Map.of(
                    "id", "c7623844-272a-4f27-b450-3ce013c1e7b2",
                    "name", "admin",
                    "description", "",
                    "composite", true,
                    "clientRole", true,
                    "containerId", CLIENT_ID
            ),
            "user", Map.of(
                    "id", "016941fc-422b-4ba1-b346-df667efb0673",
                    "name", "user",
                    "description", "",
                    "composite", true,
                    "clientRole", true,
                    "containerId", CLIENT_ID
            ),
            "colocataire", Map.of(
                    "id", "6bea7bec-4eb3-4967-b827-8abbc4c49264",
                    "name", "colocataire",
                    "description", "",
                    "composite", true,
                    "clientRole", true,
                    "containerId", CLIENT_ID
            )
    );

    public AuthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        // Optionnel : ajouter un timeout pour éviter DoS
        // ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(5000);
        // ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(5000);
    }

    private boolean isValidUUID(String userId) {
        return userId != null && UUID_PATTERN.matcher(userId).matches();
    }

    // ========================
    // Login Keycloak
    // ========================
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "password");
        requestBody.add("client_id", CLIENT_ID); // depuis variable d'environnement
        requestBody.add("username", username);
        requestBody.add("password", password);
        requestBody.add("client_secret", CLIENT_SECRET);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        String keycloakTokenUrl = "http://localhost:8080/realms/" + REALM + "/protocol/openid-connect/token";

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    keycloakTokenUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed");
            errorResponse.put("message", ex.getResponseBodyAsString());
            errorResponse.put("status", ex.getStatusCode().value());
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        } catch (Exception ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================
    // Récupérer les rôles du client
    // ========================
    @GetMapping("/roles")
    public ResponseEntity<?> getClientRoles(@RequestHeader("Authorization") String authorizationHeader) {
        String keycloakRolesUrl = "http://localhost:8080/admin/realms/" + REALM + "/clients/" + CLIENT_ID + "/roles";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    keycloakRolesUrl,
                    HttpMethod.GET,
                    requestEntity,
                    Map[].class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch roles", "message", e.getMessage()));
        }
    }

    // ========================
    // Assigner des rôles à un utilisateur
    // Correction CWE-22 + validation des rôles
    // ========================
    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRolesToUser(
            @PathVariable String userId,
            @RequestBody List<String> roles,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isValidUUID(userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid userId format"));
        }

        List<Map<String, Object>> rolesToAssign = new ArrayList<>();
        for (String roleName : roles) {
            Map<String, Object> roleData = ROLE_MAP.get(roleName.toLowerCase());
            if (roleData != null) {
                rolesToAssign.add(roleData);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Role not found: " + roleName));
            }
        }

        if (rolesToAssign.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No valid roles provided"));
        }

        try {
            String safeUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8);
            String url = String.format(
                    "http://localhost:8080/admin/realms/%s/users/%s/role-mappings/clients/%s",
                    REALM, safeUserId, CLIENT_ID);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authorizationHeader);

            HttpEntity<List<Map<String, Object>>> requestEntity = new HttpEntity<>(rolesToAssign, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(response.getStatusCode()).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to assign roles", "details", e.getMessage()));
        }
    }

    // ========================
    // Récupérer les rôles d'un utilisateur
    // Correction CWE-22 + validation UUID
    // ========================
    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<?> getUserRoles(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (!isValidUUID(userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid userId format"));
        }

        try {
            String safeUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8);
            String url = String.format(
                    "http://localhost:8080/admin/realms/%s/users/%s/role-mappings/clients/%s",
                    REALM, safeUserId, CLIENT_ID);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    List.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch roles", "details", e.getMessage()));
        }
    }
}
