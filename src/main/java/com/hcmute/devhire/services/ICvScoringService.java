package com.hcmute.devhire.services;

import com.hcmute.devhire.responses.CvScoreResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ICvScoringService {
    CvScoreResponse calculateCvSkillMatch(MultipartFile cvFile, Long jobId) throws IOException;
}
