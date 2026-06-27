package com.uade.exammanager.domain.port.out;

import com.uade.exammanager.domain.model.Topic;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de temas.
 */
public interface TopicRepositoryPort {

    List<Topic> findAll();

    Optional<Topic> findById(Long id);

    Topic save(Topic topic);

    void deleteById(Long id);
}
