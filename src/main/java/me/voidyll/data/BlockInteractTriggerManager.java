package me.voidyll.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class BlockInteractTriggerManager {
    private final Path dataFilePath;
    private final Gson gson;
    private final Set<String> triggeredBlockTypes = new HashSet<>();

    public BlockInteractTriggerManager(Path pluginDataDirectory) {
        this.dataFilePath = pluginDataDirectory.resolve("block_interact_triggers.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    public boolean isTriggered(String blockTypeId) {
        return triggeredBlockTypes.contains(blockTypeId);
    }

    public void markTriggered(String blockTypeId) {
        if (triggeredBlockTypes.add(blockTypeId)) {
            save();
        }
    }

    public void clearAll() {
        triggeredBlockTypes.clear();
        save();
    }

    private void load() {
        if (!Files.exists(dataFilePath)) {
            return;
        }

        try {
            String json = Files.readString(dataFilePath);
            Set<String> loaded = gson.fromJson(json, new TypeToken<Set<String>>() {}.getType());
            if (loaded != null) {
                triggeredBlockTypes.addAll(loaded);
            }
        } catch (IOException e) {
            System.err.println("Failed to load block interact triggers: " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(dataFilePath.getParent());
            String json = gson.toJson(triggeredBlockTypes);
            Files.writeString(dataFilePath, json);
        } catch (IOException e) {
            System.err.println("Failed to save block interact triggers: " + e.getMessage());
        }
    }
}
