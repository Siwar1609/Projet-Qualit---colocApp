package org.example.pfabackend.services.implementations;

import org.example.pfabackend.services.RoleDefinitionProvider;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StaticRoleDefinitionProvider implements RoleDefinitionProvider {
    private static final String CLIENT_ID = "71919931-d9ee-4522-b316-f8152da7785b";

    private static final Map<String, Map<String, Object>> ROLE_MAP = Map.of(
            // initialiser les rôles ici
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

    public Map<String, Object> getRole(String roleName) {
        return ROLE_MAP.get(roleName.toLowerCase());
    }
}

