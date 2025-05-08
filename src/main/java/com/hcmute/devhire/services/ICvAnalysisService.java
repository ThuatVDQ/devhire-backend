package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CvAnalysisResult;
import org.springframework.web.multipart.MultipartFile;

public interface ICvAnalysisService {
    CvAnalysisResult analyzeCv(MultipartFile cvFile);
}
