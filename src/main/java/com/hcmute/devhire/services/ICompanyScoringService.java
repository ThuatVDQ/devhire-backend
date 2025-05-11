package com.hcmute.devhire.services;

import com.hcmute.devhire.exceptions.DataNotFoundException;
import com.hcmute.devhire.responses.CompanyScoreResponse;

public interface ICompanyScoringService {
    CompanyScoreResponse calculateCompanyScore(Long companyId) throws DataNotFoundException;
    CompanyScoreResponse calculateCompanyScore(String username) throws DataNotFoundException;
}
