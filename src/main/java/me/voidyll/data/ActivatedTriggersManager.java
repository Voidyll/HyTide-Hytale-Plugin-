package me.voidyll.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ActivatedTriggersManager {
    private final Path dataFilePath;
    private final Gson gson;
    private final Set<String> usedTriggers;  // Ambient triggers that have been triggered
    private final Set<String> disabledTriggers;  // Boss/patrol triggers that are disabled

    public ActivatedTriggersManager(Path pluginDataDirectory) {
        this.dataFilePath = pluginDataDirectory.resolve("trigger_states.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        Set<Set<String>> loaded = loadTriggerStates();
        this.usedTriggers = loaded.size() > 0 ? (Set<String>)((Object[])loaded.toArray())[0] : new HashSet<>();
        this.disabledTriggers = loaded.size() > 1 ? (Set<String>)((Object[])loaded.toArray())[1] : new HashSet<>();
    }

    private Set<Set<String>> loadTriggerStates() {
        if (!Files.exists(dataFilePath)) {
            return new HashSet<>();
        }

        try {
            String json = Files.readString(dataFilePath);
            // For simplicity, we'll load as a map-like structure
            // Format: {"used": [...], "disabled": [...]}
            java.util.Map<String, Set<String>> data = gson.fromJson(json, new TypeToken<java.util.Map<String, Set<String>>>(){}.getType());
            Set<Set<String>> result = new HashSet<>();
            if (data != null) {
                result.add(data.getOrDefault("used", new HashSet<>()));
                result.add(data.getOrDefault("disabled", new HashSet<>()));
            }
            return result;
        } catch (IOException e) {
            System.err.println("Failed to load trigger states: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private void saveTriggerStates() {
        try {
            Files.createDirectories(dataFilePath.getParent());
            java.util.Map<String, Set<String>> data = new java.util.HashMap<>();
            data.put("used", usedTriggers);
            data.put("disabled", disabledTriggers);
            String json = gson.toJson(data);
            Files.writeString(dataFilePath, json);
        } catch (IOException e) {
            System.err.println("Failed to save trigger states: " + e.getMessage());
        }
    }

    public boolean isUsed(String triggerName) {
        return usedTriggers.contains(triggerName);
    }

    public void setUsed(String triggerName) {
        usedTriggers.add(triggerName);
        saveTriggerStates();
    }

    public boolean isDisabled(String triggerName) {
        return disabledTriggers.contains(triggerName);
    }

    public void setDisabled(String triggerName) {
        disabledTriggers.add(triggerName);
        saveTriggerStates();
    }

    public void setDisabledBatch(java.util.Collection<String> triggerNames) {
        disabledTriggers.addAll(triggerNames);
        saveTriggerStates();
    }

    public void clearAllStates() {
        usedTriggers.clear();
        disabledTriggers.clear();
        saveTriggerStates();
    }
}

