package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.entities.User;
import com.hcmute.devhire.repositories.CVRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CVService implements ICVService{
    private final CVRepository cvRepository;
    private final IUserService userService;
    private final FileUtil fileUtil;
    @Override
    public CV createCV(CVDTO cvDTO) throws Exception {
        User user = userService.findById(cvDTO.getUserId());
        CV cv = CV.builder()
                .user(user)
                .cvUrl(cvDTO.getCvUrl())
                .build();
        return cvRepository.save(cv);
    }

    @Override
    public CV findByUserId(Long userId) {
        return cvRepository.findByUserId(userId);
    }

    @Override
    public Optional<CV> findById(Long id) {
        return cvRepository.findById(id);
    }

    @Override
    public CVDTO uploadCV(Long userId, MultipartFile file) throws IOException {
        // Lưu file và lưu vào database
        String filename = fileUtil.storeFile(file);
        return CVDTO.builder()
                .userId(userId)
                .cvUrl(filename)
                .build();
    }

}
