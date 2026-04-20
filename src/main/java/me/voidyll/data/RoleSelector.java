package me.voidyll.data;

import java.util.*;

/**
 * Handles weighted random selection of entity roles with cap tracking.
 * Caps are tracked per spawnForGroups() call and shared across all markers.
 */
public class RoleSelector {
    private final List<RoleDefinition> availableRoles;
    private final Map<String, Integer> spawnedCounts;
    private final Random random;
    private final boolean isPatrol;
    private String selectedPatrolRole; // For patrol type - pick once, use for all

    /**
     * Creates a new role selector for a spawn session.
     * 
     * @param roles List of role definitions to select from
     * @param isPatrol If true, will pick one role and use it for all spawns
     */
    public RoleSelector(List<RoleDefinition> roles, boolean isPatrol) {
        this.availableRoles = new ArrayList<>(roles);
        this.spawnedCounts = new HashMap<>();
        this.random = new Random();
        this.isPatrol = isPatrol;
        this.selectedPatrolRole = null;
        
        // Initialize spawn counts to 0
        for (RoleDefinition role : availableRoles) {
            spawnedCounts.put(role.getEntityName(), 0);
        }
        
        // For patrol type, select the role immediately
        if (isPatrol && !availableRoles.isEmpty()) {
            this.selectedPatrolRole = selectPatrolRole();
        }
    }

    /**
     * Selects a random role based on weights for patrol spawns.
     */
    private String selectPatrolRole() {
        int totalWeight = 0;
        for (RoleDefinition role : availableRoles) {
            totalWeight += role.getWeight();
        }
        
        if (totalWeight == 0) {
            return availableRoles.isEmpty() ? null : availableRoles.get(0).getEntityName();
        }
        
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;
        
        for (RoleDefinition role : availableRoles) {
            currentWeight += role.getWeight();
            if (randomValue < currentWeight) {
                return role.getEntityName();
            }
        }
        
        // Fallback (shouldn't reach here)
        return availableRoles.isEmpty() ? null : availableRoles.get(0).getEntityName();
    }

    /**
     * Selects the next entity role to spawn.
     * For patrol spawns, always returns the pre-selected patrol role.
     * For other types, uses weighted random selection with cap enforcement.
     * 
     * @return The entity name to spawn, or null if all caps are reached
     */
    public String selectNextRole() {
        // Patrol type always uses the same pre-selected role
        if (isPatrol) {
            if (selectedPatrolRole != null) {
                incrementCount(selectedPatrolRole);
            }
            return selectedPatrolRole;
        }
        
        // Regular spawn types: weighted random selection with caps
        List<RoleDefinition> validRoles = new ArrayList<>();
        int totalWeight = 0;
        
        for (RoleDefinition role : availableRoles) {
            int currentCount = spawnedCounts.getOrDefault(role.getEntityName(), 0);
            if (currentCount < role.getCap()) {
                validRoles.add(role);
                totalWeight += role.getWeight();
            }
        }
        
        // No valid roles left
        if (validRoles.isEmpty() || totalWeight == 0) {
            return null;
        }
        
        // Weighted random selection
        int randomValue = random.nextInt(totalWeight);
        int currentWeight = 0;
        
        for (RoleDefinition role : validRoles) {
            currentWeight += role.getWeight();
            if (randomValue < currentWeight) {
                incrementCount(role.getEntityName());
                return role.getEntityName();
            }
        }
        
        // Fallback (shouldn't reach here, but just in case)
        String fallbackRole = validRoles.get(0).getEntityName();
        incrementCount(fallbackRole);
        return fallbackRole;
    }

    /**
     * Increments the spawn count for a role.
     */
    private void incrementCount(String entityName) {
        spawnedCounts.put(entityName, spawnedCounts.getOrDefault(entityName, 0) + 1);
    }

    /**
     * Gets the current spawn count for a role.
     */
    public int getCount(String entityName) {
        return spawnedCounts.getOrDefault(entityName, 0);
    }

    /**
     * Gets all spawn counts.
     */
    public Map<String, Integer> getAllCounts() {
        return new HashMap<>(spawnedCounts);
    }

    /**
     * Returns true if all role caps have been reached.
     */
    public boolean allCapsReached() {
        for (RoleDefinition role : availableRoles) {
            int currentCount = spawnedCounts.getOrDefault(role.getEntityName(), 0);
            if (currentCount < role.getCap()) {
                return false;
            }
        }
        return true;
    }
}
