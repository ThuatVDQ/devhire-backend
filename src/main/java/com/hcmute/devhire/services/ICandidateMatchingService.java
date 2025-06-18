package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.MatchingCandidateDTO;

import java.io.IOException;
import java.util.List;

public interface ICandidateMatchingService {
    List<MatchingCandidateDTO> findMatchingCandidates(Long jobId, double threshold) throws IOException;
}
