package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.repositories.CVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CVService implements ICVService{
    private final CVRepository cvRepository;
    @Override
    public CV createCV(CV cv) {
        return cvRepository.save(cv);
    }
}
