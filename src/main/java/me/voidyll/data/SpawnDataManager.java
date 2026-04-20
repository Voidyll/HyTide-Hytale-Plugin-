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

public class SpawnDataManager {
    private static final String DEFAULT_ROLE = "horde";
    private final Path dataFilePath;
    private final Gson gson;

    public SpawnDataManager(Path pluginDataDirectory) {
        this.dataFilePath = pluginDataDirectory.resolve("spawn_markers.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = dataFilePath.toFile();
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                saveSpawnMarkers(new ArrayList<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<SpawnMarkerData> loadSpawnMarkers() {
        try (FileReader reader = new FileReader(dataFilePath.toFile())) {
            Type listType = new TypeToken<ArrayList<SpawnMarkerData>>(){}.getType();
            List<SpawnMarkerData> markers = gson.fromJson(reader, listType);
            return markers != null ? markers : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveSpawnMarkers(List<SpawnMarkerData> markers) {
        try (FileWriter writer = new FileWriter(dataFilePath.toFile())) {
            gson.toJson(markers, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSpawnMarker(SpawnMarkerData marker) {
        List<SpawnMarkerData> markers = loadSpawnMarkers();
        markers.add(marker);
        saveSpawnMarkers(markers);
    }

    public List<SpawnMarkerData> getMarkersByGroupsAndRole(List<Integer> groupNumbers, String role) {
        List<SpawnMarkerData> allMarkers = loadSpawnMarkers();
        List<SpawnMarkerData> filteredMarkers = new ArrayList<>();
        String normalizedRole = normalizeRole(role);
        
        for (SpawnMarkerData marker : allMarkers) {
            String markerRole = normalizeRole(marker.getRole());
            if (groupNumbers.contains(marker.getGroupNumber()) && markerRole.equals(normalizedRole)) {
                filteredMarkers.add(marker);
            }
        }
        
        return filteredMarkers;
    }

    public List<SpawnMarkerData> getMarkersByGroupsRoleAndIdentifier(List<Integer> groupNumbers, String role, String identifier) {
        List<SpawnMarkerData> allMarkers = loadSpawnMarkers();
        List<SpawnMarkerData> filteredMarkers = new ArrayList<>();
        String normalizedRole = normalizeRole(role);
        
        for (SpawnMarkerData marker : allMarkers) {
            String markerRole = normalizeRole(marker.getRole());
            if (groupNumbers.contains(marker.getGroupNumber()) && markerRole.equals(normalizedRole)) {
                // Match identifier - if marker has an identifier, it must match
                if (identifier != null && !identifier.isEmpty()) {
                    if (marker.getIdentifier() != null && marker.getIdentifier().equals(identifier)) {
                        filteredMarkers.add(marker);
                    }
                } else {
                    // If no identifier specified, only include markers without an identifier
                    if (marker.getIdentifier() == null || marker.getIdentifier().isEmpty()) {
                        filteredMarkers.add(marker);
                    }
                }
            }
        }
        
        return filteredMarkers;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return DEFAULT_ROLE;
        }
        return role.trim().toLowerCase();
    }

    public Path getDataFilePath() {
        return dataFilePath;
    }

    public boolean hasMarkerId(String id) {
        List<SpawnMarkerData> markers = loadSpawnMarkers();
        return markers.stream().anyMatch(m -> id.equals(m.getId()));
    }

    public boolean removeMarkerById(String id) {
        List<SpawnMarkerData> markers = loadSpawnMarkers();
        boolean removed = markers.removeIf(m -> id.equals(m.getId()));
        if (removed) {
            saveSpawnMarkers(markers);
        }
        return removed;
    }
}
