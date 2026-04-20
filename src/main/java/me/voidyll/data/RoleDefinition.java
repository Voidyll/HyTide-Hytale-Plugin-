package me.voidyll.data;

/**
 * Represents a single entity role with its spawn cap and selection weight.
 */
public class RoleDefinition {
    private String entityName;
    private int cap;
    private int weight;

    public RoleDefinition() {
    }

    public RoleDefinition(String entityName, int cap, int weight) {
        this.entityName = entityName;
        this.cap = cap;
        this.weight = weight;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public int getCap() {
        return cap;
    }

    public void setCap(int cap) {
        this.cap = cap;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
