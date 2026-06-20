package com.uade.exammanager.service;

import com.uade.exammanager.entity.ExamConfig;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface ExamConfigService {
    ExamConfig getConfig();
    ExamConfig saveConfig(String subjectName, String teachers, LocalDate examDate, Integer pageCount, MultipartFile headerImage);
    void updateTopicSettings(int topicsPerGroup, boolean allowRepetition);
}
