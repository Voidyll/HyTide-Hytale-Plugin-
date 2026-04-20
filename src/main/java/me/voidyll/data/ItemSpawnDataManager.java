package me.voidyll.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages item spawn marker data persistence.
 */
public class ItemSpawnDataManager {
    private final Path dataFilePath;
    private final Gson gson;
    private ItemSpawnData data;

    public ItemSpawnDataManager(Path pluginDataDirectory) {
        this.dataFilePath = pluginDataDirectory.resolve("item_spawns.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadOrCreateData();
    }

    private void loadOrCreateData() {
        File file = dataFilePath.toFile();
        
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                data = gson.fromJson(reader, ItemSpawnData.class);
                if (data == null) {
                    data = new ItemSpawnData();
                }
            } catch (IOException e) {
                System.err.println("Failed to load item spawn data: " + e.getMessage());
                data = new ItemSpawnData();
            }
        } else {
            data = new ItemSpawnData();
            saveData();
        }
    }

    private void saveData() {
        try (FileWriter writer = new FileWriter(dataFilePath.toFile())) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Failed to save item spawn data: " + e.getMessage());
        }
    }

    /**
     * Add a new item spawn marker.
     */
    public void addMarker(ItemSpawnMarkerData marker) {
        loadOrCreateData(); // Reload from disk to respect manual edits
        data.getMarkers().add(marker);
        saveData();
    }

    /**
     * Get all item spawn markers.
     */
    public List<ItemSpawnMarkerData> getMarkers() {
        loadOrCreateData(); // Reload from disk to get latest state
        return new ArrayList<>(data.getMarkers());
    }

    /**
     * Clear all markers.
     */
    public void clearMarkers() {
        loadOrCreateData(); // Reload from disk to respect manual edits
        data.getMarkers().clear();
        saveData();
    }

    /**
     * Container class for JSON serialization.
     */
    private static class ItemSpawnData {
        private List<ItemSpawnMarkerData> markers;

        public ItemSpawnData() {
            this.markers = new ArrayList<>();
        }

        public List<ItemSpawnMarkerData> getMarkers() {
            return markers;
        }

        public void setMarkers(List<ItemSpawnMarkerData> markers) {
            this.markers = markers;
        }
    }

    public Path getDataFilePath() {
        return dataFilePath;
    }

    public boolean hasMarkerId(String id) {
        List<ItemSpawnMarkerData> markers = getMarkers();
        return markers.stream().anyMatch(m -> id.equals(m.getId()));
    }

    public boolean removeMarkerById(String id) {
        loadOrCreateData();
        boolean removed = data.getMarkers().removeIf(m -> id.equals(m.getId()));
        if (removed) {
            saveData();
        }
        return removed;
    }
}
