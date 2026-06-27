package com.uade.exammanager.domain.port.in;

import com.uade.exammanager.application.view.TopicView;

import java.util.List;

/**
 * Puerto de entrada para la gestión y asignación de temas.
 */
public interface TopicUseCase {

    List<TopicView> findAll();

    TopicView addTopic(String name);

    void deleteTopic(Long id);

    void assignTopicsToGroups(int topicsPerGroup, boolean allowRepetition);
}
