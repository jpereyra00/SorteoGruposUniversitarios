package com.uade.exammanager.infrastructure.config;

import com.uade.exammanager.domain.service.GradingDomainService;
import com.uade.exammanager.domain.service.GroupingDomainService;
import com.uade.exammanager.domain.service.TopicAssignmentDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los servicios de dominio puros como beans, manteniéndolos libres de
 * anotaciones de Spring.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public GroupingDomainService groupingDomainService() {
        return new GroupingDomainService();
    }

    @Bean
    public TopicAssignmentDomainService topicAssignmentDomainService() {
        return new TopicAssignmentDomainService();
    }

    @Bean
    public GradingDomainService gradingDomainService() {
        return new GradingDomainService();
    }
}
