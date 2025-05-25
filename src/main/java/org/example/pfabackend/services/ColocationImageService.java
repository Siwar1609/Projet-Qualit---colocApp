package org.example.pfabackend.services;

import org.example.pfabackend.dto.ColocationImageDTO;

import java.util.List;

public interface ColocationImageService {
    List<ColocationImageDTO> getImagesByColocationId(Long colocationId);
    ColocationImageDTO addImage(Long colocationId, ColocationImageDTO imageDTO);
    ColocationImageDTO updateImage(Long colocationId, Long imageId, ColocationImageDTO imageDTO);
    boolean deleteImage(Long colocationId, Long imageId);

}
