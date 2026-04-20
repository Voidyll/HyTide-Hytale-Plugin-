package me.voidyll.systems.events;

import com.google.gson.JsonObject;

/**
 * Configuration for a sub-event action.
 */
public class SubEventAction {
    private String actionType;
    private JsonObject actionData;
    
    private transient SubEventActionType parsedActionType;
    
    public SubEventAction() {
    }
    
    public SubEventAction(String actionType, JsonObject actionData) {
        this.actionType = actionType;
        this.actionData = actionData;
    }
    
    /**
     * Parse the action type string to enum after loading from JSON.
     */
    public void parseActionType() {
        try {
            this.parsedActionType = SubEventActionType.valueOf(actionType.toUpperCase());
        } catch (Exception e) {
            System.err.println("Invalid sub-event action type: " + actionType);
            this.parsedActionType = SubEventActionType.EXECUTE_COMMAND;
        }
    }
    
    public SubEventActionType getActionType() {
        if (parsedActionType == null) {
            parseActionType();
        }
        return parsedActionType;
    }
    
    public JsonObject getActionData() {
        // Return defensive copy to prevent external modification
        if (actionData == null) {
            return null;
        }
        return actionData.deepCopy();
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public void setActionData(JsonObject actionData) {
        this.actionData = actionData;
    }
}
