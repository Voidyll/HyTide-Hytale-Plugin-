# Spawn Director System Implementation Summary

## Overview
The Spawn Director System has been successfully implemented to manage automated spawning of horde and special enemies with configurable timers and entity caps.

## Key Features

### Horde Spawning
- **Timer**: Randomized between 1-2 minutes (configurable)
- **Wave System**: Triggers 3 waves spaced 30 seconds apart
- **Entity Cap**: Default 200 total entities (prevents spawn if exceeded)
- **Behavior**: 
  - Timer counts down
  - When it hits 0, checks if entity count < entity cap
  - If below cap, immediately spawns first wave, then 2 more waves at 30s intervals
  - All 3 waves spawn regardless of entity count once started
  - After 3rd wave, timer rerolls to new random value
  - Spawns for all active player groups

### Special Spawning
- **Timer**: Fixed 30 seconds (configurable)
- **Entity Cap**: Default 4 special enemies (prevents spawn if exceeded)
- **Tracked Specials**:
  - C_Eye_Void
  - C_Goblin_Lobber
  - C_Toad_Rhino_Magma
  - C_Skeleton_Archer
  - C_Skeleton_Archmage
- **Behavior**:
  - Timer counts down
  - When it hits 0, checks if special count < special cap
  - If below cap, spawns special and restarts timer
  - Spawns for all active player groups

### Entity Tracking
- Uses existing `StuckNPCCleanupSystem.getTotalNpcCount()` for total NPC count
- Custom tracking for special enemies by role name
- Separate cap enforcement for total enemies vs specials

## Commands

### Horde Timer Control
- `/horde-timer-pause` - Pauses the horde timer
- `/horde-timer-unpause` - Resumes the horde timer
- `/horde-timer-restart` - Restarts with new random value (1-2 min)

### Special Timer Control
- `/special-timer-pause` - Pauses the special timer
- `/special-timer-unpause` - Resumes the special timer
- `/special-timer-restart` - Restarts to 30 seconds

### Integration
- `/reset` command now restarts both timers automatically

## Files Created/Modified

### New Files
1. `SpawnDirectorSystem.java` - Core spawn director logic
2. `HordeTimerPauseCommand.java` - Horde timer pause command
3. `HordeTimerUnpauseCommand.java` - Horde timer unpause command
4. `HordeTimerRestartCommand.java` - Horde timer restart command
5. `SpecialTimerPauseCommand.java` - Special timer pause command
6. `SpecialTimerUnpauseCommand.java` - Special timer unpause command
7. `SpecialTimerRestartCommand.java` - Special timer restart command

### Modified Files
1. `StuckNPCCleanupSystem.java` - Added `getTotalNpcCount()` static method
2. `ActiveSpawnGroupManager.java` - Added `getAllActiveGroups()` method
3. `RoleConfigManager.java` - Updated special roles list
4. `ResetCommand.java` - Added spawn director integration
5. `FirstPlugin.java` - Registered spawn director and commands

## Configuration

### Adjustable Parameters (via API)
- `setHordeTimerRange(minMs, maxMs)` - Change horde timer range
- `setSpecialTimerMs(ms)` - Change special timer duration
- `setEntityCap(cap)` - Change total entity cap
- `setSpecialCap(cap)` - Change special entity cap

### Default Values
```java
DEFAULT_ENTITY_CAP = 200;
DEFAULT_SPECIAL_CAP = 4;
DEFAULT_HORDE_TIMER_MIN_MS = 60000; // 1 minute
DEFAULT_HORDE_TIMER_MAX_MS = 120000; // 2 minutes
DEFAULT_SPECIAL_TIMER_MS = 30000; // 30 seconds
HORDE_WAVE_INTERVAL_MS = 30000; // 30 seconds between waves
HORDE_WAVE_COUNT = 3;
```

## Edge Cases Handled
1. **No spawn markers for group**: System attempts spawn, nothing spawns, timer restarts normally
2. **Entity cap exceeded**: Timer waits at 0ms until entity count drops below cap
3. **No active groups**: Spawn attempts do nothing, timers continue cycling
4. **Reset command**: Both timers restart (horde gets new random value, special resets to 30s)
5. **Wave interruption**: Once horde waves start, all 3 waves complete regardless of entity count

## Build Status
✅ **BUILD SUCCESSFUL** - All changes compiled without errors
