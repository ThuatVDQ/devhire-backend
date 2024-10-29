package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.entities.CV;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

public interface ICVService {
    CV createCV(CVDTO cvDTO) throws Exception;
    CV findByUserId(Long userId);
    CVDTO findById(Long id);
    CVDTO uploadCV(Long userId, MultipartFile file) throws IOException;
}
