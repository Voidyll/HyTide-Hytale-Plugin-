package me.voidyll.systems.events;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for an event loaded from JSON.
 */
public class EventConfig {
    private String eventId;
    private int hordeTimerMinMs;
    private int hordeTimerMaxMs;
    private int hordeWaveIntervalMs;
    private int hordeWaveCount;
    private int specialTimerMs;
    private List<int[]> triggerBlockCoordinates;
    private String endConditionType;
    private JsonObject endConditionData;
    private boolean repeatable = false;  // Whether this event can be triggered multiple times
    private List<SubEvent> subEvents;     // Sub-events within this event
    
    // Parsed end condition data
    private transient EventEndConditionType parsedEndConditionType;
    
    public EventConfig() {
    }
    
    /**
     * Parse the end condition type string to enum after loading from JSON.
     */
    public void parseEndConditionType() {
        try {
            this.parsedEndConditionType = EventEndConditionType.valueOf(endConditionType.toUpperCase());
        } catch (Exception e) {
            System.err.println("Invalid end condition type: " + endConditionType);
            this.parsedEndConditionType = EventEndConditionType.TIMER;
        }
    }
    
    // ==================== Getters ====================
    
    public String getEventId() {
        return eventId;
    }
    
    public int getHordeTimerMinMs() {
        return hordeTimerMinMs;
    }
    
    public int getHordeTimerMaxMs() {
        return hordeTimerMaxMs;
    }
    
    public int getHordeWaveIntervalMs() {
        return hordeWaveIntervalMs;
    }
    
    public int getHordeWaveCount() {
        return hordeWaveCount;
    }
    
    public int getSpecialTimerMs() {
        return specialTimerMs;
    }
    
    public EventEndConditionType getEndConditionType() {
        if (parsedEndConditionType == null) {
            parseEndConditionType();
        }
        return parsedEndConditionType;
    }
    
    public List<int[]> getTriggerBlockCoordinates() {
        return triggerBlockCoordinates != null ? new ArrayList<>(triggerBlockCoordinates) : new ArrayList<>();
    }
    
    // ==================== End Condition Data Getters ====================
    
    public long getTimerDurationMs() {
        if (endConditionData != null && endConditionData.has("durationMs")) {
            return endConditionData.get("durationMs").getAsLong();
        }
        return 0;
    }
    
    public String getTriggerZoneName() {
        if (endConditionData != null && endConditionData.has("zoneName")) {
            return endConditionData.get("zoneName").getAsString();
        }
        return "";
    }
    
    public String getBlockBreakType() {
        if (endConditionData != null && endConditionData.has("blockType")) {
            return endConditionData.get("blockType").getAsString();
        }
        return "";
    }
    
    public int getBlockBreakCount() {
        if (endConditionData != null && endConditionData.has("count")) {
            return endConditionData.get("count").getAsInt();
        }
        return 0;
    }
    
    public String getBlockInteractionType() {
        if (endConditionData != null && endConditionData.has("blockType")) {
            return endConditionData.get("blockType").getAsString();
        }
        return "";
    }
    
    /**
     * Get entity kill requirements as a JsonObject.
     * Expected format: { "HyTide_Shadow_Knight": 2, "HyTide_Rex_Cave": 1 }
     */
    public JsonObject getEntityKillRequirements() {
        if (endConditionData != null && endConditionData.has("entities")) {
            return endConditionData.getAsJsonObject("entities");
        }
        return null;
    }
    
    // ==================== Setters ====================
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public void setHordeTimerMinMs(int hordeTimerMinMs) {
        this.hordeTimerMinMs = hordeTimerMinMs;
    }
    
    public void setHordeTimerMaxMs(int hordeTimerMaxMs) {
        this.hordeTimerMaxMs = hordeTimerMaxMs;
    }
    
    public void setHordeWaveIntervalMs(int hordeWaveIntervalMs) {
        this.hordeWaveIntervalMs = hordeWaveIntervalMs;
    }
    
    public void setHordeWaveCount(int hordeWaveCount) {
        this.hordeWaveCount = hordeWaveCount;
    }
    
    public void setSpecialTimerMs(int specialTimerMs) {
        this.specialTimerMs = specialTimerMs;
    }
    
    public void setEndConditionType(String endConditionType) {
        this.endConditionType = endConditionType;
    }
    
    public void setEndConditionData(JsonObject endConditionData) {
        this.endConditionData = endConditionData;
    }
    
    public void setTriggerBlockCoordinates(List<int[]> triggerBlockCoordinates) {
        this.triggerBlockCoordinates = triggerBlockCoordinates;
    }
    
    public boolean isRepeatable() {
        return repeatable;
    }
    
    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
    
    public List<SubEvent> getSubEvents() {
        return subEvents != null ? new ArrayList<>(subEvents) : new ArrayList<>();
    }
    
    public void setSubEvents(List<SubEvent> subEvents) {
        this.subEvents = subEvents;
    }
}
