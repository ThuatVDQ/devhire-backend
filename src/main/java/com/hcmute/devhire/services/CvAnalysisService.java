package com.hcmute.devhire.services;

import com.hcmute.devhire.DTOs.CvAnalysisResult;
import com.hcmute.devhire.components.FileTextExtractor;
import com.hcmute.devhire.components.OpenNlpTextProcessor;
import com.hcmute.devhire.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CvAnalysisService implements ICvAnalysisService {
    private final FileTextExtractor fileTextExtractor;

    private final OpenNlpTextProcessor nlpTextProcessor;

    private final SkillRepository skillRepository;
    @Override
    public CvAnalysisResult analyzeCv(MultipartFile cvFile) {
        try {
            // 1. Trích xuất văn bản từ file
            String rawText = fileTextExtractor.extractText(cvFile);

            // 2. Chuẩn hóa văn bản
            String normalizedText = nlpTextProcessor.normalizeText(rawText);

            // 3. Khởi tạo kết quả
            CvAnalysisResult result = new CvAnalysisResult();

            // 4. Trích xuất kỹ năng
            result.setSkills(extractSkills(normalizedText));

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Error analyzing CV", e);
        }
    }

    private Set<String> extractSkills(String text) {
        List<String> allSkills = skillRepository.findAllSkillNames();
        Set<String> found = new HashSet<>();

        for (String skill : allSkills) {
            if (text.toLowerCase().contains(skill.toLowerCase())) {
                found.add(skill);
            }
        }

        // Gộp thêm danh từ từ NLP
        //found.addAll(nlpTextProcessor.extractNouns(text));
        return found;
    }
}
