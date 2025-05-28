package org.example.pfabackend.controllers;

import org.example.pfabackend.services.RoleDefinitionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
@Service
public class KeycloakRoleService {

    private final RestTemplate restTemplate;
    private final RoleDefinitionProvider roleProvider;


    private String clientId = "71919931-d9ee-4522-b316-f8152da7785b";


    private String realm= "PFARealm";

    private static final Logger logger = LoggerFactory.getLogger(KeycloakRoleService.class);

    public KeycloakRoleService(RestTemplate restTemplate, RoleDefinitionProvider roleProvider) {
        this.restTemplate = restTemplate;
        this.roleProvider = roleProvider;
    }

    public boolean assignRoleToUser(String userId, String roleName, String authToken) {
        Map<String, Object> role = roleProvider.getRole(roleName);
        if (role == null) return false;

        String url = String.format(
                "http://localhost:8080/admin/realms/%s/users/%s/role-mappings/clients/%s",
                realm, userId, clientId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authToken);

        HttpEntity<List<Map<String, Object>>> requestEntity = new HttpEntity<>(List.of(role), headers);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
            return response.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (Exception ex) {
            logger.error("Erreur d’assignation du rôle {} à l'utilisateur {} : {}", roleName, userId, ex.getMessage());
            return false;
        }
    }
}
