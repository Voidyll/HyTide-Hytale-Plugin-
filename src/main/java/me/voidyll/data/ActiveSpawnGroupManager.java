package me.voidyll.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class ActiveSpawnGroupManager {
    private final Map<String, Integer> activeGroupByPlayerUuid = new HashMap<>();
    private final Path dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ActiveSpawnGroupManager(Path pluginDataDirectory) {
        this.dataFile = pluginDataDirectory.resolve("active_groups.json");
        loadActiveGroups();
    }

    public void setActiveGroup(String playerUuid, int groupNumber) {
        activeGroupByPlayerUuid.put(playerUuid, groupNumber);
        saveActiveGroups();
    }

    public Integer getActiveGroup(String playerUuid) {
        return activeGroupByPlayerUuid.get(playerUuid);
    }

    public void removePlayer(String playerUuid) {
        activeGroupByPlayerUuid.remove(playerUuid);
        saveActiveGroups();
    }

    public void clear() {
        activeGroupByPlayerUuid.clear();
        saveActiveGroups();
    }

    public java.util.List<Integer> getAllActiveGroups() {
        return activeGroupByPlayerUuid.values().stream()
            .distinct()
            .toList();
    }

    private void loadActiveGroups() {
        try {
            if (Files.exists(dataFile)) {
                String content = Files.readString(dataFile);
                JsonObject json = gson.fromJson(content, JsonObject.class);
                if (json != null) {
                    for (String playerUuid : json.keySet()) {
                        activeGroupByPlayerUuid.put(playerUuid, json.get(playerUuid).getAsInt());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load active groups: " + e.getMessage());
        }
    }

    private void saveActiveGroups() {
        try {
            Files.createDirectories(dataFile.getParent());
            String json = gson.toJson(activeGroupByPlayerUuid);
            Files.writeString(dataFile, json);
        } catch (IOException e) {
            System.err.println("Failed to save active groups: " + e.getMessage());
        }
    }
}
