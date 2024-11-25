package com.hcmute.devhire.services;

import java.util.List;

public interface ICompanyImageService {
    List<String> getImageUrlsByCompanyId(Long companyId);
}
