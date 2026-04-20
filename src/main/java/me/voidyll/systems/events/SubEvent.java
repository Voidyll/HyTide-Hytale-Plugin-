package me.voidyll.systems.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a sub-event within a main event.
 */
public class SubEvent {
    private String id;
    private SubEventTrigger trigger;
    private List<SubEventAction> actions;
    
    public SubEvent() {
        this.actions = new ArrayList<>();
    }
    
    public SubEvent(String id, SubEventTrigger trigger, List<SubEventAction> actions) {
        this.id = id;
        this.trigger = trigger;
        this.actions = actions != null ? actions : new ArrayList<>();
    }
    
    /**
     * Parse nested enums after loading from JSON.
     */
    public void parseTypes() {
        if (trigger != null) {
            trigger.parseTriggerType();
        }
        if (actions != null) {
            for (SubEventAction action : actions) {
                action.parseActionType();
            }
        }
    }
    
    public String getId() {
        return id;
    }
    
    public SubEventTrigger getTrigger() {
        return trigger;
    }
    
    public List<SubEventAction> getActions() {
        return actions != null ? new ArrayList<>(actions) : new ArrayList<>();
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setTrigger(SubEventTrigger trigger) {
        this.trigger = trigger;
    }
    
    public void setActions(List<SubEventAction> actions) {
        this.actions = actions;
    }
}
