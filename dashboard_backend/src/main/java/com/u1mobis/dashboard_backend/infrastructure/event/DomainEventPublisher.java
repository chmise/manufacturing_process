package com.u1mobis.dashboard_backend.infrastructure.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public void publish(DomainEvent event) {
        try {
            log.debug("도메인 이벤트 발행: {} - {}", event.getClass().getSimpleName(), event.aggregateId());
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("도메인 이벤트 발행 실패: {}", event.getClass().getSimpleName(), e);
        }
    }
    
    public void publishAll(Iterable<DomainEvent> events) {
        events.forEach(this::publish);
    }
}