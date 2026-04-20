package me.voidyll.data;

public class TriggerZoneData {
    private String name;
    private int groupNumber;
    private double radius;
    private double x;
    private double y;
    private double z;
    private String worldName;
    private String type;
    private String identifier;

    public TriggerZoneData() {
    }

    public TriggerZoneData(String name, int groupNumber, double radius, double x, double y, double z, String worldName, String type) {
        this.name = name;
        this.groupNumber = groupNumber;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.type = type;
        this.identifier = null;
    }

    public TriggerZoneData(String name, int groupNumber, double radius, double x, double y, double z, String worldName, String type, String identifier) {
        this.name = name;
        this.groupNumber = groupNumber;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
        this.type = type;
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public double getRadius() {
        return radius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
