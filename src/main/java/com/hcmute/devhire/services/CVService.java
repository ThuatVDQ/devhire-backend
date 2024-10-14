package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.repositories.CVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CVService implements ICVService{
    private final CVRepository cvRepository;
    @Override
    public CV createCV(CVDTO cvDTO) {
        CV cv = CV.builder()
                .userId(cvDTO.getUserId())
                .cvUrl(cvDTO.getCvUrl())
                .build();
        return cvRepository.save(cv);
    }

    @Override
    public CV findByUserId(Long userId) {
        return cvRepository.findByUserId(userId);
    }
}
