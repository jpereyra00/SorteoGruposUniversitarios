package com.uade.exammanager.service.impl;

import com.uade.exammanager.entity.ExamConfig;
import com.uade.exammanager.exception.BusinessException;
import com.uade.exammanager.repository.ExamConfigRepository;
import com.uade.exammanager.service.ExamConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class ExamConfigServiceImpl implements ExamConfigService {

    private static final Long SINGLETON_ID = 1L;

    private final ExamConfigRepository examConfigRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ExamConfigServiceImpl(ExamConfigRepository examConfigRepository) {
        this.examConfigRepository = examConfigRepository;
    }

    @Override
    public ExamConfig getConfig() {
        return examConfigRepository.findById(SINGLETON_ID).orElseGet(this::createDefault);
    }

    @Override
    public ExamConfig saveConfig(String subjectName,
                                 String teachers,
                                 LocalDate examDate,
                                 Integer pageCount,
                                 MultipartFile headerImage) {
        if (subjectName == null || subjectName.trim().isEmpty()) {
            throw new BusinessException("El nombre de la materia es obligatorio.");
        }
        if (teachers == null || teachers.trim().isEmpty()) {
            throw new BusinessException("Debe indicar al menos un docente.");
        }
        if (pageCount == null || pageCount <= 0) {
            throw new BusinessException("La cantidad de hojas debe ser mayor a 0.");
        }

        ExamConfig config = getConfig();
        config.setSubjectName(subjectName.trim());
        config.setTeachers(teachers.trim());
        config.setExamDate(examDate);
        config.setPageCount(pageCount);

        if (headerImage != null && !headerImage.isEmpty()) {
            config.setHeaderImagePath(saveHeaderImage(headerImage));
        }

        return examConfigRepository.save(config);
    }

    @Override
    public void updateTopicSettings(int topicsPerGroup, boolean allowRepetition) {
        if (topicsPerGroup <= 0) {
            throw new BusinessException("La cantidad de temas por grupo debe ser mayor a 0.");
        }
        ExamConfig config = getConfig();
        config.setTopicsPerGroup(topicsPerGroup);
        config.setAllowTopicRepetition(allowRepetition);
        examConfigRepository.save(config);
    }

    private ExamConfig createDefault() {
        ExamConfig config = new ExamConfig();
        config.setId(SINGLETON_ID);
        config.setSubjectName("Materia");
        config.setTeachers("Docentes");
        config.setPageCount(1);
        config.setTopicsPerGroup(1);
        config.setAllowTopicRepetition(false);
        return examConfigRepository.save(config);
    }

    private String saveHeaderImage(MultipartFile image) {
        String filename = StringUtils.cleanPath(image.getOriginalFilename());
        String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')) : ".png";
        String storedName = "header-" + UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path target = uploadPath.resolve(storedName);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar la imagen de cabecera.");
        }
    }
}
