package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.CV;
import com.hcmute.devhire.entities.Skill;
import com.hcmute.devhire.repositories.CVRepository;
import com.hcmute.devhire.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CVSkillExtractorService implements ICVSkillExtractorService {
    private final SkillRepository skillRepository;
    private final CVRepository cvRepository;
    @Override
    public List<Skill> extractSkillsFromCV(Long cvId) throws IOException, TikaException {
        CV cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV not found"));
        String baseDir = System.getProperty("user.dir");
        String filePath = cv.getCvUrl();
        File file = Paths.get(baseDir, "/uploads/" + filePath).toFile();
        if (!file.exists()) {
            throw new FileNotFoundException("CV file not found at " + filePath);
        }

        // Trích xuất nội dung văn bản từ file
        Tika tika = new Tika();
        String content = tika.parseToString(file).toLowerCase();

        // Tìm kỹ năng có trong nội dung
        List<Skill> allSkills = skillRepository.findAll();
        return allSkills.stream()
                .filter(skill -> content.contains(skill.getName().toLowerCase()))
                .collect(Collectors.toList());
    }
}
