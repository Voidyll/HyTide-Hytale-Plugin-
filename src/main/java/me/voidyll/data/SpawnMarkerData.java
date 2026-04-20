package me.voidyll.data;

public class SpawnMarkerData {
    private String id;
    private String entityType;
    private int spawnNumber;
    private int groupNumber;
    private String role;
    private double x;
    private double y;
    private double z;
    private String worldName;
    private String identifier;
    private String pathName;

    public SpawnMarkerData() {
    }

    public SpawnMarkerData(String entityType, int spawnNumber, int groupNumber, String role, double x, double y, double z, String worldName) {
        this.entityType = entityType;
        this.spawnNumber = spawnNumber;
        this.groupNumber = groupNumber;
        this.role = role;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.identifier = null;
    }

    public SpawnMarkerData(String entityType, int spawnNumber, int groupNumber, String role, double x, double y, double z, String worldName, String identifier) {
        this.entityType = entityType;
        this.spawnNumber = spawnNumber;
        this.groupNumber = groupNumber;
        this.role = role;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.identifier = identifier;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public int getSpawnNumber() {
        return spawnNumber;
    }

    public void setSpawnNumber(int spawnNumber) {
        this.spawnNumber = spawnNumber;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }
}
