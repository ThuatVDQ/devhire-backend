package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.entities.CV;

public interface ICVService {
    CV createCV(CVDTO cvDTO);
    CV findByUserId(Long userId);
}
