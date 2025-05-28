package org.example.pfabackend.services;

import java.util.Map;

public interface RoleDefinitionProvider {
    Map<String, Object> getRole(String roleName);
}
