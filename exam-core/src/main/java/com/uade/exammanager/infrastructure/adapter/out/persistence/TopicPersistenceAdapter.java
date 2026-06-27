package com.uade.exammanager.infrastructure.adapter.out.persistence;

import com.uade.exammanager.domain.model.Topic;
import com.uade.exammanager.domain.port.out.TopicRepositoryPort;
import com.uade.exammanager.infrastructure.adapter.out.persistence.mapper.TopicMapper;
import com.uade.exammanager.repository.TopicRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link TopicRepositoryPort} sobre Spring Data JPA.
 */
@Component
public class TopicPersistenceAdapter implements TopicRepositoryPort {

    private final TopicRepository topicRepository;

    public TopicPersistenceAdapter(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Override
    public List<Topic> findAll() {
        return topicRepository.findAll().stream()
                .map(TopicMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Topic> findById(Long id) {
        return topicRepository.findById(id).map(TopicMapper::toDomain);
    }

    @Override
    public Topic save(Topic topic) {
        com.uade.exammanager.entity.Topic entity = topic.getId() != null
                ? topicRepository.findById(topic.getId()).orElseGet(com.uade.exammanager.entity.Topic::new)
                : new com.uade.exammanager.entity.Topic();
        entity.setName(topic.getName());
        return TopicMapper.toDomain(topicRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        topicRepository.deleteById(id);
    }
}
