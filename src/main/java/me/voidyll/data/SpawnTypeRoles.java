package me.voidyll.data;

import java.util.List;

/**
 * Configuration for all entity roles available for a specific spawn type.
 */
public class SpawnTypeRoles {
    private List<RoleDefinition> horde;
    private List<RoleDefinition> ambient;
    private List<RoleDefinition> patrol;
    private List<RoleDefinition> boss;
    private List<RoleDefinition> special;

    public SpawnTypeRoles() {
    }

    public List<RoleDefinition> getHorde() {
        return horde;
    }

    public void setHorde(List<RoleDefinition> horde) {
        this.horde = horde;
    }

    public List<RoleDefinition> getAmbient() {
        return ambient;
    }

    public void setAmbient(List<RoleDefinition> ambient) {
        this.ambient = ambient;
    }

    public List<RoleDefinition> getPatrol() {
        return patrol;
    }

    public void setPatrol(List<RoleDefinition> patrol) {
        this.patrol = patrol;
    }

    public List<RoleDefinition> getBoss() {
        return boss;
    }

    public void setBoss(List<RoleDefinition> boss) {
        this.boss = boss;
    }

    public List<RoleDefinition> getSpecial() {
        return special;
    }

    public void setSpecial(List<RoleDefinition> special) {
        this.special = special;
    }

    /**
     * Gets the role list for a specific spawn type.
     */
    public List<RoleDefinition> getRolesForType(String spawnType) {
        if (spawnType == null) {
            return null;
        }
        
        switch (spawnType.toLowerCase()) {
            case "horde":
            case "checkpoint": // checkpoint triggers use horde roles
                return horde;
            case "ambient":
                return ambient;
            case "patrol":
                return patrol;
            case "boss":
                return boss;
            case "special":
                return special;
            default:
                return null;
        }
    }
}
