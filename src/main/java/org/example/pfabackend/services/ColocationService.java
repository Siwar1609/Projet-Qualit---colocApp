package org.example.pfabackend.services;


import org.example.pfabackend.dto.ColocationDTO;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;


public interface ColocationService {
    //List<ColocationDTO> getAllColocations();
    Page<ColocationDTO> getAllColocations(String search, int page, int size, Jwt jwt);
    Optional<ColocationDTO> getColocationById(Long id, Jwt jwt);
    ColocationDTO saveColocation(ColocationDTO colocationDTO);
    ColocationDTO updateColocation(Long id, ColocationDTO colocationDTO);
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
}