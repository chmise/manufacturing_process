package com.u1mobis.dashboard_backend.shared.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot<ID> {
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    protected void raiseEvent(DomainEvent event) {
        domainEvents.add(event);
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
    
    public abstract ID getId();
}