package me.voidyll.data;

/**
 * Represents a single item spawn marker location.
 */
public class ItemSpawnMarkerData {
    private String id;
    private double x;
    private double y;
    private double z;
    private String worldName;

    public ItemSpawnMarkerData() {
        this.worldName = "Default";
    }

    public ItemSpawnMarkerData(double x, double y, double z, String worldName) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
