package org.example.pfabackend.service;

import org.example.pfabackend.model.Colocation;
import org.example.pfabackend.model.ColocationDTO;
import org.example.pfabackend.repository.ColocationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ColocationServiceImpl implements ColocationService {
    private final ColocationRepository colocationRepository;

    public ColocationServiceImpl(ColocationRepository colocationRepository) {
        this.colocationRepository = colocationRepository;
    }

    @Override
    public List<ColocationDTO> getAllColocations() {
        return colocationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ColocationDTO> getColocationById(Long id) {
        return colocationRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    public ColocationDTO saveColocation(ColocationDTO colocationDTO) {
        Colocation colocation = convertToEntity(colocationDTO);
        Colocation savedColocation = colocationRepository.save(colocation);
        return convertToDTO(savedColocation);
    }

    @Override
    public ColocationDTO updateColocation(Long id, ColocationDTO colocationDTO) {
        Colocation colocation = colocationRepository.findById(id).orElseThrow();
        colocation.setName(colocationDTO.name());
        colocation.setDescription(colocationDTO.description());
        colocation.setAddress(colocationDTO.address());
        colocation.setPrice(colocationDTO.price());
        Colocation updatedColocation = colocationRepository.save(colocation);
        return convertToDTO(updatedColocation);
    }

    @Override
    public void deleteColocation(Long id) {
        colocationRepository.deleteById(id);
    }

    // Convert Colocation Entity to ColocationDTO
    private ColocationDTO convertToDTO(Colocation colocation) {
        return new ColocationDTO(colocation.getId(), colocation.getName(), colocation.getAddress(), colocation.getDescription(), colocation.getPrice());
    }

    // Convert ColocationDTO to Colocation Entity
    private Colocation convertToEntity(ColocationDTO colocationDTO) {
        Colocation colocation = new Colocation();
        colocation.setName(colocationDTO.name());
        colocation.setDescription(colocationDTO.description());
        colocation.setAddress(colocationDTO.address());
        colocation.setPrice(colocationDTO.price());
        return colocation;
    }
}
