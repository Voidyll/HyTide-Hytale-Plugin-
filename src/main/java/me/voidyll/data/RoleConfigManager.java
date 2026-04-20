package me.voidyll.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages loading and accessing entity role configurations from JSON.
 */
public class RoleConfigManager {
    private final Path configFilePath;
    private final Gson gson;
    private SpawnTypeRoles rolesConfig;

    public RoleConfigManager(Path pluginDataDirectory) {
        this.configFilePath = pluginDataDirectory.resolve("entity_roles.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadOrCreateConfig();
    }

    private void loadOrCreateConfig() {
        File file = configFilePath.toFile();
        if (!file.exists()) {
            createDefaultConfig();
        } else {
            loadConfig();
        }
    }

    private void createDefaultConfig() {
        rolesConfig = createDefaultRolesConfig();
        saveConfig();
    }

    private SpawnTypeRoles createDefaultRolesConfig() {
        SpawnTypeRoles config = new SpawnTypeRoles();
        
        // Horde roles
        List<RoleDefinition> hordeRoles = new ArrayList<>();
        hordeRoles.add(new RoleDefinition("HyTide_Aggressive_Rat", 999, 150));
        hordeRoles.add(new RoleDefinition("HyTide_Goblin_Scrapper", 999, 40));
        hordeRoles.add(new RoleDefinition("HyTide_Goblin_Scavenger", 999, 20));
        hordeRoles.add(new RoleDefinition("HyTide_Spawn_Void", 3, 2));
        hordeRoles.add(new RoleDefinition("HyTide_Trork_Warrior", 3, 2));
        hordeRoles.add(new RoleDefinition("HyTide_Bear_Polar", 1, 1));
        hordeRoles.add(new RoleDefinition("HyTide_Skeleton_Burnt_Knight", 3, 1));
        hordeRoles.add(new RoleDefinition("HyTide_Skeleton_Frost_Knight", 3, 1));
        hordeRoles.add(new RoleDefinition("HyTide_Wraith", 3, 2));
        config.setHorde(hordeRoles);
        
        // Ambient roles
        List<RoleDefinition> ambientRoles = new ArrayList<>();
        ambientRoles.add(new RoleDefinition("HyTide_Aggressive_Rat_Amb", 999, 100));
        ambientRoles.add(new RoleDefinition("HyTide_Zombie_Amb", 999, 40));
        ambientRoles.add(new RoleDefinition("HyTide_Zombie-Burnt_Amb", 999, 30));
        ambientRoles.add(new RoleDefinition("HyTide_Spawn_Void_Amb", 2, 10));
        ambientRoles.add(new RoleDefinition("HyTide_Trork_Warrior_Amb", 2, 10));
        ambientRoles.add(new RoleDefinition("HyTide_Bear_Polar_Amb", 2, 10));
        ambientRoles.add(new RoleDefinition("HyTide_Skeleton_Burnt_Knight_Amb", 2, 10));
        ambientRoles.add(new RoleDefinition("HyTide_Skeleton_Frost_Knight_Amb", 2, 10));
        ambientRoles.add(new RoleDefinition("HyTide_Wraith_Amb", 2, 10));
        config.setAmbient(ambientRoles);
        
        // Patrol roles
        List<RoleDefinition> patrolRoles = new ArrayList<>();
        patrolRoles.add(new RoleDefinition("HyTide_Spawn_Void_Pat", 999, 1));
        patrolRoles.add(new RoleDefinition("HyTide_Trork_Warrior_Pat", 999, 1));
        patrolRoles.add(new RoleDefinition("HyTide_Bear_Polar_Pat", 999, 1));
        patrolRoles.add(new RoleDefinition("HyTide_Skeleton_Burnt_Knight_Pat", 999, 1));
        patrolRoles.add(new RoleDefinition("HyTide_Skeleton_Frost_Knight_Pat", 999, 1));
        patrolRoles.add(new RoleDefinition("HyTide_Wraith_Pat", 999, 1));
        config.setPatrol(patrolRoles);
        
        // Boss roles
        List<RoleDefinition> bossRoles = new ArrayList<>();
        bossRoles.add(new RoleDefinition("HyTide_Shadow_Knight", 1, 5));
        bossRoles.add(new RoleDefinition("HyTide_Rex_Cave", 1, 5));
        bossRoles.add(new RoleDefinition("HyTide_Yeti", 1, 5));
        bossRoles.add(new RoleDefinition("HyTide_Hedera", 1, 5));
        bossRoles.add(new RoleDefinition("HyTide_Scarak_Broodmother", 1, 5));
        bossRoles.add(new RoleDefinition("HyTide_Golem_Crystal_Earth", 1, 1));
        bossRoles.add(new RoleDefinition("HyTide_Golem_Crystal_Flame", 1, 1));
        bossRoles.add(new RoleDefinition("HyTide_Golem_Crystal_Frost", 1, 1));
        bossRoles.add(new RoleDefinition("HyTide_Golem_Crystal_Sand", 1, 1));
        bossRoles.add(new RoleDefinition("HyTide_Golem_Crystal_Thunder", 1, 1));
        config.setBoss(bossRoles);
        
        // Special roles
        List<RoleDefinition> specialRoles = new ArrayList<>();
        specialRoles.add(new RoleDefinition("HyTide_Eye_Void", 2, 1));
        specialRoles.add(new RoleDefinition("HyTide_Goblin_Lobber", 2, 1));
        specialRoles.add(new RoleDefinition("HyTide_Toad_Rhino_Magma", 2, 1));
        specialRoles.add(new RoleDefinition("HyTide_Skeleton_Archer", 2, 1));
        specialRoles.add(new RoleDefinition("HyTide_Skeleton_Archmage", 2, 1));
        config.setSpecial(specialRoles);
        
        return config;
    }

    private void loadConfig() {
        try (FileReader reader = new FileReader(configFilePath.toFile())) {
            rolesConfig = gson.fromJson(reader, SpawnTypeRoles.class);
            if (rolesConfig == null) {
                rolesConfig = createDefaultRolesConfig();
            }
        } catch (IOException e) {
            System.err.println("Error loading entity_roles.json: " + e.getMessage());
            rolesConfig = createDefaultRolesConfig();
        }
    }

    private void saveConfig() {
        try {
            File file = configFilePath.toFile();
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(rolesConfig, writer);
            }
        } catch (IOException e) {
            System.err.println("Error saving entity_roles.json: " + e.getMessage());
        }
    }

    /**
     * Reloads the configuration from disk.
     * Call this if the file is edited while the server is running.
     */
    public void reloadConfig() {
        loadConfig();
    }

    /**
     * Gets the role configuration for all spawn types.
     */
    public SpawnTypeRoles getRolesConfig() {
        return rolesConfig;
    }

    /**
     * Gets the roles for a specific spawn type.
     */
    public List<RoleDefinition> getRolesForType(String spawnType) {
        if (rolesConfig == null) {
            return new ArrayList<>();
        }
        List<RoleDefinition> roles = rolesConfig.getRolesForType(spawnType);
        return roles != null ? roles : new ArrayList<>();
    }

    public Path getConfigFilePath() {
        return configFilePath;
    }
}
