package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.CompanyImage;
import com.hcmute.devhire.repositories.CompanyImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyImageService implements ICompanyImageService {
    private final CompanyImageRepository companyImageRepository;
    @Override
    public List<String> getImageUrlsByCompanyId(Long companyId) {
        List<CompanyImage> companyImages = companyImageRepository.findByCompanyId(companyId);
        if (companyImages != null) {
            return companyImages.stream().map(CompanyImage::getImageUrl).toList();
        }
        return null;
    }
}
