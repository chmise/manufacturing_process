package com.u1mobis.dashboard_backend.shared.kernel;

public abstract class ValueObject {
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract int hashCode();
}