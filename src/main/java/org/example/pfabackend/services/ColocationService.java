package org.example.pfabackend.services;


import org.example.pfabackend.dto.ColocationDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;


public interface ColocationService {
    //List<ColocationDTO> getAllColocations();
    Page<ColocationDTO> getAllColocations(String search,int page, int size);
    Optional<ColocationDTO> getColocationById(Long id);
    ColocationDTO saveColocation(ColocationDTO colocationDTO);
    ColocationDTO updateColocation(Long id, ColocationDTO colocationDTO);
    void deleteColocation(Long id);
}