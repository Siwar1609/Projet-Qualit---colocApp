package org.example.pfabackend.services;


import org.example.pfabackend.dto.ColocationDTO;
import org.example.pfabackend.dto.UpdateColocationDTO;
import org.example.pfabackend.entities.Colocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;


public interface ColocationService {
    //List<ColocationDTO> getAllColocations();
    Page<ColocationDTO> getAllColocations(String search, int page, int size, Jwt jwt);
    Optional<ColocationDTO> getColocationById(Long id, Jwt jwt);
    ColocationDTO saveColocation(ColocationDTO colocationDTO);
    ColocationDTO updateColocation(Long id, UpdateColocationDTO colocationDTO);
    void deleteColocation(Long id);
    Page<ColocationDTO> getNonPublishedColocations(String search,int page, int size);
    /**
     * Update the 'isPublished' status of a colocation
     */
    ColocationDTO updateIsPublished(Long id, boolean isPublished);

    /**
     * Update the 'isArchived' status of a colocation
     */
    ColocationDTO updateIsArchived(Long id, boolean isArchived);

    public Colocation assignUserToColocation(Long colocationId, String userIdToAssign, String currentUserId, boolean isAdmin);

    public Page<Colocation> getOwnColocations(String userId, String keyword, Pageable pageable);

    Optional<Colocation> getColocationEntityById(Long id);

    List<Long> getAssignedColocationIds(String userId);


    Colocation removeAssignedUserFromColocation(Long colocationId, String userIdToRemove, String currentUserId, boolean isAdmin);

    Page<ColocationDTO> getAssignedColocations(String userId, int page, int size);

    Colocation toggleUserAssignment(Long id, String userIdToAssign, String currentUserId, boolean isAdmin);
}