package com.uade.exammanager.service.impl;

import com.uade.exammanager.entity.ExamConfig;
import com.uade.exammanager.entity.ExamGroup;
import com.uade.exammanager.entity.GroupTopicAssignment;
import com.uade.exammanager.entity.Topic;
import com.uade.exammanager.exception.BusinessException;
import com.uade.exammanager.repository.ExamGroupRepository;
import com.uade.exammanager.repository.GroupTopicAssignmentRepository;
import com.uade.exammanager.repository.TopicRepository;
import com.uade.exammanager.service.ExamConfigService;
import com.uade.exammanager.service.TopicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final ExamGroupRepository examGroupRepository;
    private final GroupTopicAssignmentRepository assignmentRepository;
    private final ExamConfigService examConfigService;

    public TopicServiceImpl(TopicRepository topicRepository,
                            ExamGroupRepository examGroupRepository,
                            GroupTopicAssignmentRepository assignmentRepository,
                            ExamConfigService examConfigService) {
        this.topicRepository = topicRepository;
        this.examGroupRepository = examGroupRepository;
        this.assignmentRepository = assignmentRepository;
        this.examConfigService = examConfigService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> findAll() {
        return topicRepository.findAll();
    }

    @Override
    public Topic addTopic(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("El tema no puede estar vacío.");
        }
        Topic topic = new Topic();
        topic.setName(name.trim());
        return topicRepository.save(topic);
    }

    @Override
    public void deleteTopic(Long id) {
        topicRepository.deleteById(id);
    }

    @Override
    public void assignTopicsToGroups(int topicsPerGroup, boolean allowRepetition) {
        List<ExamGroup> groups = examGroupRepository.findAll();
        List<Topic> topics = topicRepository.findAll();

        if (groups.isEmpty()) {
            throw new BusinessException("Debe generar grupos antes de asignar temas.");
        }
        if (topics.isEmpty()) {
            throw new BusinessException("Debe cargar temas antes de asignarlos.");
        }
        if (!allowRepetition && topics.size() < groups.size() * topicsPerGroup) {
            throw new BusinessException("No hay suficientes temas para asignar sin repetición.");
        }

        for (ExamGroup group : groups) {
            assignmentRepository.deleteByExamGroupId(group.getId());
        }

        List<Topic> globalPool = new ArrayList<>(topics);
        Collections.shuffle(globalPool, new Random());

        int globalPointer = 0;
        for (ExamGroup group : groups) {
            List<Topic> groupPool = new ArrayList<>(topics);
            Collections.shuffle(groupPool, new Random());

            for (int i = 0; i < topicsPerGroup; i++) {
                Topic selected;
                if (allowRepetition) {
                    selected = groupPool.get(i % groupPool.size());
                } else {
                    selected = globalPool.get(globalPointer++);
                }

                GroupTopicAssignment assignment = new GroupTopicAssignment();
                assignment.setExamGroup(group);
                assignment.setTopic(selected);
                assignmentRepository.save(assignment);
            }
        }

        ExamConfig config = examConfigService.getConfig();
        config.setTopicsPerGroup(topicsPerGroup);
        config.setAllowTopicRepetition(allowRepetition);
    }
}
