package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CVDTO;
import com.hcmute.devhire.components.FileUtil;
import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.entities.Skill;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CVService implements ICVService{
    private final CVRepository cvRepository;
    private final IUserService userService;
    private final ICVSkillExtractorService cvSkillExtractorService;
    private final IUserSkillService userSkillService;
    private final FileUtil fileUtil;
    @Override
    public CV createCV(CVDTO cvDTO) throws Exception {
        User user = userService.findById(cvDTO.getUserId());
        CV cv = CV.builder()
                .user(user)
                .cvUrl(cvDTO.getCvUrl())
                .name(cvDTO.getName())
                .build();
        cv = cvRepository.save(cv);
        List<Skill> skills = cvSkillExtractorService.extractSkillsFromCV(cv.getId());
        if (skills != null && !skills.isEmpty()) {
            skills.forEach(skill -> {
                userSkillService.add(user, skill);
            });
        }
        return cv;
    }

    @Override
    public List<CV> findByUserId(Long userId) {
        List<CV> cvs = cvRepository.findByUserId(userId);
        cvs.sort(Comparator.comparing(CV::getUpdatedAt).reversed());
        return cvs;
    }

    @Override
    public CV findById(Long id) {
        return cvRepository.findById(id).orElse(null);
    }

    @Override
    public CVDTO getById(Long id) {
        CV cv = cvRepository.findById(id).orElse(null);
        if (cv == null) {
            return null;
        }
        return CVDTO.builder()
                .id(cv.getId())
                .cvUrl(cv.getCvUrl())
                .name(cv.getName())
                .userId(cv.getUser().getId())
                .build();
    }

    @Override
    public CVDTO uploadCV(Long userId, String name, MultipartFile file) throws IOException {
        // Lưu file và lưu vào database
        String filename = fileUtil.storeFile(file);
        return CVDTO.builder()
                .userId(userId)
                .name(name)
                .cvUrl(filename)
                .build();
    }

}
