package org.example.pfabackend.service;


import org.example.pfabackend.model.ColocationDTO;
import java.util.List;
import java.util.Optional;


public interface ColocationService {
    List<ColocationDTO> getAllColocations();
    Optional<ColocationDTO> getColocationById(Long id);
    ColocationDTO saveColocation(ColocationDTO colocationDTO);
    ColocationDTO updateColocation(Long id, ColocationDTO colocationDTO);
    void deleteColocation(Long id);
}