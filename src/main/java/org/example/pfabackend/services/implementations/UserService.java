package org.example.pfabackend.services.implementations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.token.url}")
    private String tokenUrl;

    @Value("${keycloak.users.url}")
    private String usersUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public UserService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Map<String, Object>> populateUsersById(List<String> userIds) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("Unable to retrieve access token from Keycloak.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                usersUrl,
                HttpMethod.GET,
                entity,
                List.class
        );

        List<Map<String, Object>> allUsers = response.getBody();

        return allUsers.stream()
                .filter(user -> userIds.contains(user.get("id")))
                .collect(Collectors.toList());
    }

    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    JsonNode.class
            );
            return response.getBody().get("access_token").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
