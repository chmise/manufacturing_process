package com.u1mobis.dashboard_backend.shared.kernel;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredOn();
    String aggregateId();
}