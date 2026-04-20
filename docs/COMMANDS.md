# Plugin Commands Documentation

This document describes all commands available in the Vermintide Spawn Simulator plugin.

## Command Categories

- [Event Management](#event-management)
- [Spawn Timers](#spawn-timers)
- [Configuration](#configuration)
- [Utilities](#utilities)

---

## Event Management

### /start-event

Starts a specific event by ID. If another event is already running, it will be stopped first.

**Usage:** `/start-event <eventId>`

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `eventId` | string | ✓ | The ID of the event to start (e.g., `righteous_stand_first_event`). Use `/list-events` to see available event IDs. |

**Examples:**
```
/start-event righteous_stand_first_event
/start-event boss_arena_wave
```

**Response:**
- Success: `Starting event: [eventId]`
- Error: `Error: Event '[eventId]' not found`

**Notes:**
- Only one event can run at a time. Starting a new event stops the current one.
- Event values (horde timers, special timer) are immediately applied.
- Players will see event notifications.

---

### /stop-event

Stops the currently running event and restores default spawn timers.

**Usage:** `/stop-event`

**Parameters:** None

**Examples:**
```
/stop-event
```

**Response:**
- Success: `Event '[eventId]' has ended!` (broadcast to all players)
- Info: No message if no event is running

**Notes:**
- Restores spawn director to default timer values immediately.
- Any active sub-events are canceled.
- Cleanup logic in the event is executed.

---

### /list-events

Lists all available events with their current status.

**Usage:** `/list-events`

**Parameters:** None

**Examples:**
```
/list-events
```

**Response:**
```
=== Events ===
 righteous_stand_first_event [ONGOING - 45s]
 righteous_activate_cannon_lever [Started]
 boss_arena_wave [Not Started]
```

**Status Meanings:**
- `[ONGOING - Xs]` - Event is currently running, showing elapsed seconds
- `[Started]` - Event has run at least once in this session
- `[Not Started]` - Event has never been triggered in this session

---

## Spawn Timers

### /horde-timer-pause

Pauses the horde spawn timer. No new hordes will spawn while paused.

**Usage:** `/horde-timer-pause`

**Parameters:** None

**Examples:**
```
/horde-timer-pause
```

**Response:**
- Success: `Horde timer paused` (broadcast to players)

**Notes:**
- Can be resumed with `/horde-timer-unpause`.
- Active horde spawns in progress continue normally.
- Useful for managing spawn rates during events.

---

### /horde-timer-unpause

Resumes the paused horde spawn timer.

**Usage:** `/horde-timer-unpause`

**Parameters:** None

**Examples:**
```
/horde-timer-unpause
```

**Response:**
- Success: `Horde timer unpaused` (broadcast to players)

**Notes:**
- Only works if timer is currently paused.
- Timer resumes from its previous state.

---

### /horde-timer-restart

Restarts the horde spawn timer, resetting elapsed time to zero.

**Usage:** `/horde-timer-restart`

**Parameters:** None

**Examples:**
```
/horde-timer-restart
```

**Response:**
- Success: `Horde timer restarted` (broadcast to players)

**Notes:**
- Clears any progress toward the next horde spawn.
- Useful for synchronizing spawns during event resets.
- Any running event's horde timers are reset to their event-specific timings.

---

### /special-timer-pause

Pauses the special (difficult) NPC spawn timer.

**Usage:** `/special-timer-pause`

**Parameters:** None

**Examples:**
```
/special-timer-pause
```

**Response:**
- Success: `Special timer paused` (broadcast to players)

**Notes:**
- Can be resumed with `/special-timer-unpause`.
- Similar to horde timer but affects special/elite NPC spawns.

---

### /special-timer-unpause

Resumes the paused special NPC spawn timer.

**Usage:** `/special-timer-unpause`

**Parameters:** None

**Examples:**
```
/special-timer-unpause
```

**Response:**
- Success: `Special timer unpaused` (broadcast to players)

---

### /special-timer-restart

Restarts the special NPC spawn timer.

**Usage:** `/special-timer-restart`

**Parameters:** None

**Examples:**
```
/special-timer-restart
```

**Response:**
- Success: `Special timer restarted` (broadcast to players)

**Notes:**
- Resets elapsed time to zero for special NPC spawns.
- Resets to event-specific timings if an event is running.

---

## Configuration

### /reset

Performs a complete reset of the world state. Cleans NPCs, resets timers, clears player states, and returns them to spawn.

**Usage:** `/reset`

**Parameters:** None

**Examples:**
```
/reset
```

**Response:**
```
Resetting world...
NPC clean complete.
Reset complete. All active groups and trigger states cleared.
You have been reset to world spawn. Active group set to 0.
```

**Actions Performed:**
1. Cleans all NPCs from the world
2. Clears all active spawn groups
3. Resets trigger states
4. Stops current event (if running) and clears event trigger block states
5. Resets block trigger states so events can be triggered again
6. Restarts and unpauses horde/special timers
7. Teleports all players to world spawn
8. Sets all players' active group to 0

**Notes:**
- This is a comprehensive reset. Use when you want a clean slate.
- All event data resets; repeatable events can trigger again.
- Block-triggered events can be triggered again from scratch.
- All players are affected.

---

## Configuration & World Setup

### /create-spawn

Creates or modifies a spawn group configuration.

**Usage:** `/create-spawn <groupNumber> <x> <y> <z> <radius> [npcType]`

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `groupNumber` | integer | ✓ | The spawn group ID (0-based). |
| `x` | integer | ✓ | X coordinate of spawn center. |
| `y` | integer | ✓ | Y coordinate of spawn center. |
| `z` | integer | ✓ | Z coordinate of spawn center. |
| `radius` | integer | ✓ | Radius in blocks around center where NPCs can spawn. |
| `npcType` | string | ✗ | Optional NPC type to spawn. If omitted, will use configured types. |

**Examples:**
```
/create-spawn 0 100 64 200 10
/create-spawn 1 150 64 250 15 C_Shadow_Knight
```

**Response:**
- Success: `Spawn group [groupNumber] created/updated at (x, y, z) with radius [radius]`

**Notes:**
- Group numbers should be sequential starting from 0.
- Larger radius = larger spawn area.
- NPC type is optional; specific NPC types can be configured separately.

---

### /create-trigger-zone

Creates a trigger zone. When players enter, specific spawn behaviors can be triggered.

**Usage:** `/create-trigger-zone <zoneName> <x1> <y1> <z1> <x2> <y2> <z2>`

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `zoneName` | string | ✓ | Unique name for this trigger zone. Use snake_case (e.g., `boss_arena`). |
| `x1`, `y1`, `z1` | integer | ✓ | First corner coordinates of the zone box. |
| `x2`, `y2`, `z2` | integer | ✓ | Opposite corner coordinates of the zone box. |

**Examples:**
```
/create-trigger-zone boss_arena 100 60 200 150 70 250
/create-trigger-zone arena_entrance 80 65 180 120 68 220
```

**Response:**
- Success: `Trigger zone '[zoneName]' created from (x1, y1, z1) to (x2, y2, z2)`

**Notes:**
- Zones are axis-aligned rectangular boxes.
- Coordinates define opposite corners (not required to be min/max).
- Zone names are case-sensitive. Use descriptive names.
- Used in event configurations with `TRIGGER_ZONE` end condition type.

---

### /list-trigger-zones

Lists all created trigger zones with their coordinates and status.

**Usage:** `/list-trigger-zones`

**Parameters:** None

**Examples:**
```
/list-trigger-zones
```

**Response:**
```
=== Trigger Zones ===
 boss_arena: (100, 60, 200) to (150, 70, 250) [Active]
 arena_entrance: (80, 65, 180) to (120, 68, 220) [Active]
```

**Notes:**
- Shows all defined zones and their bounding boxes.
- Useful for verifying zone configurations.

---

## Utilities

### /open-spawns

Opens the spawn configuration menu or displays spawn information.

**Usage:** `/open-spawns`

**Parameters:** None

**Examples:**
```
/open-spawns
```

**Response:**
- Displays spawn group information and menu options (varies by implementation).

**Notes:**
- Specific behavior depends on the GUI implementation.
- Typically shows available spawn groups and allows configuration.

---

### /trigger-spawn

Manually triggers a specific spawn.

**Usage:** `/trigger-spawn <groupNumber> [npcType] [count]`

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `groupNumber` | integer | ✓ | The spawn group to trigger (from `/open-spawns` or config). |
| `npcType` | string | ✗ | Optional NPC type to spawn. Overrides group default. |
| `count` | integer | ✗ | Number of NPCs to spawn. Default is the group's configured count. |

**Examples:**
```
/trigger-spawn 0
/trigger-spawn 1 C_Shadow_Knight 5
/trigger-spawn 0 C_Wraith 3
```

**Response:**
- Success: `Spawned [count] x [npcType] at spawn group [groupNumber]`
- Error: `Spawn group [groupNumber] not found`

**Notes:**
- Useful for testing or manually controlling spawns.
- Respects the spawn radius and group configuration.
- Overriding NPC type still uses the group's location/radius.

---

### /first

Shows plugin information and version details.

**Usage:** `/first`

**Parameters:** None

**Examples:**
```
/first
```

**Response:**
- Displays plugin name, version, and other metadata.

**Notes:**
- Handy for verifying the plugin is loaded and working.

---

## Tips and Tricks

| Task | Command |
|------|---------|
| See all events | `/list-events` |
| Start an event | `/start-event [eventId]` |
| Stop an event | `/stop-event` |
| Slow down spawns | `/horde-timer-pause` or increase event timers in `events.json` |
| Speed up spawns | `/horde-timer-restart` or decrease event timers |
| Freeze the world | `/horde-timer-pause` + `/special-timer-pause` |
| Clean up and restart | `/reset` |
| Create a spawn area | `/create-spawn [number] [x] [y] [z] [radius]` |
| Define an arena | `/create-trigger-zone [name] [x1] [y1] [z1] [x2] [y2] [z2]` |
| Test a spawn | `/trigger-spawn [groupNumber] [npcType] [count]` |

---

## Command Permissions

All commands typically require operator (`op`) level permissions by default. Contact your server administrator to adjust permissions if needed.

---

## Troubleshooting Commands

| Issue | Try |
|-------|-----|
| Event won't start | `/list-events` to verify event ID, check console for errors |
| Too many NPCs | `/horde-timer-pause` or `/stop-event` |
| World is broken | `/reset` to perform full cleanup |
| Spawns not working | `/open-spawns` to verify groups are created, then `/trigger-spawn` to test |
| Need to resync | `/horde-timer-restart` + `/special-timer-restart` |
