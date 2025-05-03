package com.hcmute.devhire.services;

import com.hcmute.devhire.entities.Skill;
import org.apache.tika.exception.TikaException;

import java.io.IOException;
import java.util.List;

public interface ICVSkillExtractorService {
    List<Skill> extractSkillsFromCV(Long cvId) throws IOException, TikaException;
}
