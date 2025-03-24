package org.example.pfabackend.services.implementations;

import org.example.pfabackend.entities.Colocation;
import org.example.pfabackend.dto.ColocationDTO;
import org.example.pfabackend.exceptions.ResourceNotFoundException;
import org.example.pfabackend.repositories.ColocationRepository;
import org.example.pfabackend.services.ColocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ColocationServiceImpl implements ColocationService {
    private final ColocationRepository colocationRepository;

    public ColocationServiceImpl(ColocationRepository colocationRepository) {
        this.colocationRepository = colocationRepository;
    }

    @Override
    public Page<ColocationDTO> getAllColocations(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Colocation> result = (search == null || search.trim().isEmpty())
                ? colocationRepository.findAll(pageable)
                : colocationRepository.search(search, pageable);

        return result.map(this::convertToDTO);
    }

    @Override
    public Optional<ColocationDTO> getColocationById(Long id) {
        return colocationRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public ColocationDTO saveColocation(ColocationDTO colocationDTO) {
        Colocation colocation = convertToEntity(colocationDTO);
        return convertToDTO(colocationRepository.save(colocation));
    }

    @Override
    public ColocationDTO updateColocation(Long id, ColocationDTO colocationDTO) {
        Colocation colocation = colocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colocation with ID " + id + " not found"));

        colocation.setName(colocationDTO.name());
        colocation.setNameOfPublisher(colocationDTO.nameOfPublisher());
        colocation.setIdOfPublisher(colocationDTO.idOfPublisher());
        colocation.setDescription(colocationDTO.description());
        colocation.setAddress(colocationDTO.address());
        colocation.setPrice(colocationDTO.price());

        return convertToDTO(colocationRepository.save(colocation));
    }

    @Override
    public void deleteColocation(Long id) {
        if (!colocationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Colocation with ID " + id + " not found");
        }
        colocationRepository.deleteById(id);
    }

    // Convert Colocation Entity to ColocationDTO
    private ColocationDTO convertToDTO(Colocation colocation) {
        return new ColocationDTO(
                colocation.getId(),
                colocation.getName(),
                colocation.getIdOfPublisher(),
                colocation.getNameOfPublisher(),
                colocation.getAddress(),
                colocation.getDescription(),
                colocation.getPrice()
        );
    }

    // Convert ColocationDTO to Colocation Entity
    private Colocation convertToEntity(ColocationDTO colocationDTO) {
        Colocation colocation = new Colocation();
        colocation.setId(colocationDTO.id());
        colocation.setName(colocationDTO.name());
        colocation.setAddress(colocationDTO.address());
        colocation.setDescription(colocationDTO.description());
        colocation.setIdOfPublisher(colocationDTO.idOfPublisher());
        colocation.setNameOfPublisher(colocationDTO.nameOfPublisher());
        colocation.setPrice(colocationDTO.price());
        return colocation;
    }

}
