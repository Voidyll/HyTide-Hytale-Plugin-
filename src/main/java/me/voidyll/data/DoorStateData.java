package me.voidyll.data;

/**
 * Data class representing a door's position and state.
 * Used to track door state changes during events for automatic reset functionality.
 */
public class DoorStateData {
    private int x;
    private int y;
    private int z;
    private String state;
    
    /**
     * No-arg constructor for GSON deserialization.
     */
    public DoorStateData() {
    }
    
    /**
     * Create a new door state data entry.
     * 
     * @param x X coordinate of the door
     * @param y Y coordinate of the door
     * @param z Z coordinate of the door
     * @param state The current state of the door (e.g., "OpenDoorIn", "CloseDoorIn")
     */
    public DoorStateData(int x, int y, int z, String state) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.state = state;
    }
    
    /**
     * Get the X coordinate of the door.
     * 
     * @return X coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Get the Y coordinate of the door.
     * 
     * @return Y coordinate
     */
    public int getY() {
        return y;
    }
    
    /**
     * Get the Z coordinate of the door.
     * 
     * @return Z coordinate
     */
    public int getZ() {
        return z;
    }
    
    /**
     * Get the current state of the door.
     * 
     * @return The door state (e.g., "OpenDoorIn", "CloseDoorIn")
     */
    public String getState() {
        return state;
    }
    
    /**
     * Get the opposite state for door reset.
     * Maps: OpenDoorIn to CloseDoorIn and vice versa, OpenDoorOut to CloseDoorOut and vice versa.
     * DoorBlocked has no opposite and returns itself.
     * 
     * @return The opposite door state for reset purposes
     */
    public String getOppositeState() {
        switch (state) {
            case "OpenDoorIn":
                return "CloseDoorIn";
            case "CloseDoorIn":
                return "OpenDoorIn";
            case "OpenDoorOut":
                return "CloseDoorOut";
            case "CloseDoorOut":
                return "OpenDoorOut";
            case "DoorBlocked":
                return "DoorBlocked";
            default:
                // For unknown states, return the original state
                System.err.println("[DoorStateData] Unknown door state: " + state + ", cannot determine opposite");
                return state;
        }
    }
    
    /**
     * Get a unique key for this door position.
     * 
     * @return A string key in format "x,y,z"
     */
    public String getPositionKey() {
        return x + "," + y + "," + z;
    }
    
    @Override
    public String toString() {
        return "Door at (" + x + "," + y + "," + z + ") state: " + state;
    }
}
