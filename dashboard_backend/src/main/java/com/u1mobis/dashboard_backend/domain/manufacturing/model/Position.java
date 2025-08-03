package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class Position extends ValueObject {
    private final double x;
    private final double y;
    
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public static Position initial() {
        return new Position(0.0, 0.0);
    }
    
    public static Position at(double x, double y) {
        return new Position(x, y);
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double distanceTo(Position other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public Position moveTo(double newX, double newY) {
        return new Position(newX, newY);
    }
    
    public Position moveBy(double deltaX, double deltaY) {
        return new Position(this.x + deltaX, this.y + deltaY);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return Double.compare(position.x, x) == 0 && Double.compare(position.y, y) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    @Override
    public String toString() {
        return "Position{x=" + x + ", y=" + y + "}";
    }
}