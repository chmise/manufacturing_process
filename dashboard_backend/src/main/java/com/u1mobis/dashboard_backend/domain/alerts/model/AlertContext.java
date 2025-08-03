package com.u1mobis.dashboard_backend.domain.alerts.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDateTime;
import java.util.*;

public class AlertContext extends ValueObject {
    private final Map<String, Object> metadata;
    private final List<AlertNote> notes;
    private String resolution;
    private String resolutionBy;
    private LocalDateTime resolutionAt;
    
    public AlertContext() {
        this.metadata = new HashMap<>();
        this.notes = new ArrayList<>();
    }
    
    public void addMetadata(String key, Object value) {
        if (key != null && !key.trim().isEmpty() && value != null) {
            metadata.put(key.trim(), value);
        }
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public Map<String, Object> getAllMetadata() {
        return new HashMap<>(metadata);
    }
    
    public void addNote(String note, String userId) {
        if (note != null && !note.trim().isEmpty()) {
            notes.add(new AlertNote(note.trim(), userId, LocalDateTime.now()));
        }
    }
    
    public List<AlertNote> getNotes() {
        return new ArrayList<>(notes);
    }
    
    public void setResolution(String resolution, String userId) {
        this.resolution = resolution;
        this.resolutionBy = userId;
        this.resolutionAt = LocalDateTime.now();
    }
    
    public String getResolution() {
        return resolution;
    }
    
    public String getResolutionBy() {
        return resolutionBy;
    }
    
    public LocalDateTime getResolutionAt() {
        return resolutionAt;
    }
    
    public boolean hasResolution() {
        return resolution != null && !resolution.trim().isEmpty();
    }
    
    public int getNoteCount() {
        return notes.size();
    }
    
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AlertContext that = (AlertContext) obj;
        return Objects.equals(metadata, that.metadata) &&
               Objects.equals(notes, that.notes) &&
               Objects.equals(resolution, that.resolution) &&
               Objects.equals(resolutionBy, that.resolutionBy) &&
               Objects.equals(resolutionAt, that.resolutionAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(metadata, notes, resolution, resolutionBy, resolutionAt);
    }
    
    public static class AlertNote extends ValueObject {
        private final String note;
        private final String userId;
        private final LocalDateTime timestamp;
        
        public AlertNote(String note, String userId, LocalDateTime timestamp) {
            this.note = note;
            this.userId = userId;
            this.timestamp = timestamp;
        }
        
        public String getNote() {
            return note;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AlertNote alertNote = (AlertNote) obj;
            return Objects.equals(note, alertNote.note) &&
                   Objects.equals(userId, alertNote.userId) &&
                   Objects.equals(timestamp, alertNote.timestamp);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(note, userId, timestamp);
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s: %s", timestamp, userId, note);
        }
    }
}