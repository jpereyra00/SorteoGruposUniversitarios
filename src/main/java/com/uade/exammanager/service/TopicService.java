package com.uade.exammanager.service;

import com.uade.exammanager.entity.Topic;

import java.util.List;

public interface TopicService {
    List<Topic> findAll();
    Topic addTopic(String name);
    void deleteTopic(Long id);
    void assignTopicsToGroups(int topicsPerGroup, boolean allowRepetition);
}
