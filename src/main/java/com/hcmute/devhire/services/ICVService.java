package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.entities.CV;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ICVService {
    CV createCV(CVDTO cvDTO) throws Exception;
    List<CV> findByUserId(Long userId);
    CV findById(Long id);
    CVDTO getById(Long id);
    CVDTO uploadCV(Long userId, String name, MultipartFile file) throws IOException;
}
