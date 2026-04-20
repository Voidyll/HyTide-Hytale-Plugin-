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

public class TriggerZoneManager {
    private final Path dataFilePath;
    private final Gson gson;

    public TriggerZoneManager(Path pluginDataDirectory) {
        this.dataFilePath = pluginDataDirectory.resolve("trigger_zones.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureFileExists();
    }

    private void ensureFileExists() {
        File file = dataFilePath.toFile();
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                saveZones(new ArrayList<>());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<TriggerZoneData> loadZones() {
        try (FileReader reader = new FileReader(dataFilePath.toFile())) {
            Type listType = new TypeToken<ArrayList<TriggerZoneData>>(){}.getType();
            List<TriggerZoneData> zones = gson.fromJson(reader, listType);
            return zones != null ? zones : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveZones(List<TriggerZoneData> zones) {
        try (FileWriter writer = new FileWriter(dataFilePath.toFile())) {
            gson.toJson(zones, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addZone(TriggerZoneData zone) {
        List<TriggerZoneData> zones = loadZones();
        zones.add(zone);
        saveZones(zones);
    }

    public Path getDataFilePath() {
        return dataFilePath;
    }

    public boolean removeZoneByName(String name) {
        List<TriggerZoneData> zones = loadZones();
        boolean removed = zones.removeIf(z -> name.equals(z.getName()));
        if (removed) {
            saveZones(zones);
        }
        return removed;
    }
}
