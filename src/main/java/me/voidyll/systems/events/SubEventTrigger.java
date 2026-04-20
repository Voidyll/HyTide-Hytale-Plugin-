package me.voidyll.systems.events;

import com.google.gson.JsonObject;

/**
 * Configuration for a sub-event trigger.
 */
public class SubEventTrigger {
    private String triggerType;
    private JsonObject triggerData;
    
    private transient SubEventTriggerType parsedTriggerType;
    
    public SubEventTrigger() {
    }
    
    public SubEventTrigger(String triggerType, JsonObject triggerData) {
        this.triggerType = triggerType;
        this.triggerData = triggerData;
    }
    
    /**
     * Parse the trigger type string to enum after loading from JSON.
     */
    public void parseTriggerType() {
        try {
            this.parsedTriggerType = SubEventTriggerType.valueOf(triggerType.toUpperCase());
        } catch (Exception e) {
            System.err.println("Invalid sub-event trigger type: " + triggerType);
            this.parsedTriggerType = SubEventTriggerType.TIMER;
        }
    }
    
    public SubEventTriggerType getTriggerType() {
        if (parsedTriggerType == null) {
            parseTriggerType();
        }
        return parsedTriggerType;
    }
    
    public JsonObject getTriggerData() {
        // Return defensive copy to prevent external modification
        if (triggerData == null) {
            return null;
        }
        return triggerData.deepCopy();
    }
    
    public long getTimerDelayMs() {
        if (triggerData != null && triggerData.has("delayMs")) {
            return triggerData.get("delayMs").getAsLong();
        }
        return 0;
    }
    
    public java.util.List<String> getBlockTypes() {
        java.util.List<String> blockTypes = new java.util.ArrayList<>();
        if (triggerData != null && triggerData.has("blockTypes")) {
            com.google.gson.JsonArray blockArray = triggerData.getAsJsonArray("blockTypes");
            for (com.google.gson.JsonElement element : blockArray) {
                blockTypes.add(element.getAsString());
            }
        }
        return blockTypes;
    }
    
    public int getEntityKillThreshold() {
        if (triggerData != null && triggerData.has("count")) {
            return triggerData.get("count").getAsInt();
        }
        return 0;
    }
    
    public String getParentSubEventId() {
        if (triggerData != null && triggerData.has("subEventId")) {
            return triggerData.get("subEventId").getAsString();
        }
        return "";
    }
    
    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }
    
    public void setTriggerData(JsonObject triggerData) {
        this.triggerData = triggerData;
    }
}
