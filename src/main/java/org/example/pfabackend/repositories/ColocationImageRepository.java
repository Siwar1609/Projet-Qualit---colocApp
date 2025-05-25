package org.example.pfabackend.repositories;

import org.example.pfabackend.entities.ColocationImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColocationImageRepository extends JpaRepository<ColocationImage, Long> {
    List<ColocationImage> findByColocationId(Long colocationId);
}
