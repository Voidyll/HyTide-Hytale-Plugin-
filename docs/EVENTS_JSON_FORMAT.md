# Events JSON Format Documentation

This document describes the complete format for the `events.json` configuration file used by the Vermintide Spawn Simulator plugin.

## Overview

The `events.json` file consists of an array of event objects. Each event can be triggered manually via commands, by interacting with specific blocks, by game conditions (entity kills, time, etc.), or automatically when another event ends (if repeatable).

## Root Structure

```json
[
  { event object 1 },
  { event object 2 },
  ...
]
```

---

## Event Object Fields

### Core Event Properties

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `eventId` | string | ✓ | N/A | Unique identifier for the event. Used to reference the event in commands and configurations. Must not contain spaces. |
| `repeatable` | boolean | ✗ | false | Whether this event can be triggered multiple times. If `false`, it can only trigger once per session. |
| `triggerBlockTypes` | array of strings | ✗ | empty | Block types that trigger this event when interacted with. The event starts when ANY of these blocks is interacted with (one-time per session). |
| `hordeTimerMinMs` | integer | ✗ | 10000 | Minimum milliseconds between horde spawns during this event (in milliseconds). |
| `hordeTimerMaxMs` | integer | ✗ | 15000 | Maximum milliseconds between horde spawns during this event (randomized between min and max). |
| `hordeWaveIntervalMs` | integer | ✗ | 20000 | Milliseconds between each wave within a horde spawn cycle. |
| `hordeWaveCount` | integer | ✗ | 3 | Number of waves per horde spawn cycle. |
| `specialTimerMs` | integer | ✗ | 30000 | Milliseconds between special (difficult) NPC spawns during this event. |
| `endConditionType` | string | ✓ | N/A | How the event ends. See **End Condition Types** section below. |
| `endConditionData` | object | ✓ | N/A | Data specific to the end condition type. Structure varies by type. |
| `subEvents` | array of SubEvent objects | ✗ | empty | Sub-events that trigger at specific times/conditions during this event. |

---

## End Condition Types

### TIMER

Event ends after a specified duration.

```json
"endConditionType": "timer",
"endConditionData": {
  "durationMs": 180000
}
```

| Field | Type | Description |
|-------|------|-------------|
| `durationMs` | integer | Duration in milliseconds before the event automatically ends. |

---

### ENTITY_KILLED

Event ends after a specified number of specific entity types are killed.

```json
"endConditionType": "entity_killed",
"endConditionData": {
  "entities": {
    "C_Shadow_Knight": 2,
    "C_Rex_Cave": 1
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `entities` | object | Key-value pairs where key is entity role name and value is quantity required. Event ends when ALL entities have been killed. |

**Example:** Event ends after 2 Shadow Knights AND 1 Rex Cave are killed.

---

### TRIGGER_ZONE

Event ends when a player enters a specific trigger zone.

```json
"endConditionType": "trigger_zone",
"endConditionData": {
  "zoneName": "boss_arena_exit"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `zoneName` | string | Name of the trigger zone that ends the event when activated. |

---

### BLOCK_BREAK

Event ends after a specific number of blocks of a certain type are broken.

```json
"endConditionType": "block_break",
"endConditionData": {
  "blockType": "wooden_door",
  "count": 5
}
```

| Field | Type | Description |
|-------|------|-------------|
| `blockType` | string | Type of block to track. |
| `count` | integer | Number of blocks that must be broken to end the event. |

---

### BLOCK_INTERACTION

Event ends when a specific block type is interacted with.

```json
"endConditionType": "block_interaction",
"endConditionData": {
  "blockType": "lever_final"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `blockType` | string | Block type that ends the event when interacted with. |

---

## Sub-Events

Sub-events are smaller one-off triggers that execute specific actions during a main event. They execute exactly once per event instance.

### SubEvent Object

```json
{
  "id": "sub_event_unique_id",
  "trigger": { trigger object },
  "actions": [ action object, action object, ... ]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | ✓ | Unique identifier for this sub-event within the event. |
| `trigger` | object | ✓ | Trigger configuration. See **Sub-Event Trigger Types** below. |
| `actions` | array | ✓ | Array of actions to execute (all execute on the same tick). |

---

## Sub-Event Trigger Types

### TIMER

Triggered after a specific delay from event start.

```json
"trigger": {
  "triggerType": "timer",
  "triggerData": {
    "delayMs": 30000
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `delayMs` | integer | Milliseconds after event start to trigger this sub-event. |

---

### BLOCK_INTERACTION

Triggered when any of specified block types are interacted with.

```json
"trigger": {
  "triggerType": "block_interaction",
  "triggerData": {
    "blockTypes": [
      "Furniture_Pressure_Plate",
      "lever_wall"
    ]
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `blockTypes` | array of strings | Block types that trigger this sub-event. Event triggers on ANY of these types. |

---

### ENTITY_KILLED

Triggered after a certain number of entities are killed since event start.

```json
"trigger": {
  "triggerType": "entity_killed",
  "triggerData": {
    "count": 10
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `count` | integer | Number of entities that must be killed to trigger this sub-event. Count resets per event instance. |

---

### SUB_EVENT_COMPLETION

Triggered when another sub-event completes.

```json
"trigger": {
  "triggerType": "sub_event_completion",
  "triggerData": {
    "subEventId": "parent_sub_event_id"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `subEventId` | string | ID of the parent sub-event that must complete first. |

---

## Sub-Event Action Types

### EXECUTE_COMMAND

Executes a custom command.

```json
{
  "actionType": "execute_command",
  "actionData": {
    "command": "say The barrier has fallen!"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `command` | string | The command string to execute (without leading `/`). |

---

### REMOVE_BLOCKS

Removes blocks at specified coordinates.

```json
{
  "actionType": "remove_blocks",
  "actionData": {
    "coordinates": [
      [100, 64, 200],
      [101, 64, 200],
      [102, 64, 200]
    ]
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `coordinates` | array of [x, y, z] | Coordinates of blocks to remove. Any block type at these coordinates is removed. |

---

### ADD_BLOCKS

Adds blocks at specified coordinates.

```json
{
  "actionType": "add_blocks",
  "actionData": {
    "coordinates": [
      [100, 64, 200],
      [101, 64, 200]
    ],
    "blockType": "wooden_wall"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `coordinates` | array of [x, y, z] | Coordinates where blocks should be placed. |
| `blockType` | string | Block type to place at these coordinates. |

---

### SPAWN_NPC

Spawns an NPC at a specific location.

```json
{
  "actionType": "spawn_npc",
  "actionData": {
    "npcType": "C_Shadow_Knight",
    "x": 150,
    "y": 65,
    "z": 250
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `npcType` | string | Entity role/type of NPC to spawn. |
| `x` | integer | X coordinate for spawn location. |
| `y` | integer | Y coordinate for spawn location. |
| `z` | integer | Z coordinate for spawn location. |

---

### DESPAWN_ALL_NPCS

Despawns all NPCs currently in the world.

```json
{
  "actionType": "despawn_all_npcs",
  "actionData": {}
}
```

No additional data required.

---

### UNLOCK_INTERACTION

Unlocks a block type for interaction.

```json
{
  "actionType": "unlock_interaction",
  "actionData": {
    "blockType": "locked_door"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `blockType` | string | Block type to unlock for interactions. |

---

### SET_BLOCK_STATE

Sets the state of a block at specific coordinates. Commonly used for controlling doors, levers, or other blocks with multiple states.

**Note:** Door states set via this action are automatically tracked. When the `/reset` command is used, all tracked doors will be reset to their opposite state (e.g., `OpenDoorIn` → `CloseDoorIn`).

```json
{
  "actionType": "set_block_state",
  "actionData": {
    "x": 100,
    "y": 64,
    "z": 200,
    "state": "OpenDoorIn"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `x` | integer | X coordinate of the block. |
| `y` | integer | Y coordinate of the block. |
| `z` | integer | Z coordinate of the block. |
| `state` | string | The state name to set the block to. For doors: `OpenDoorIn`, `CloseDoorIn`, `OpenDoorOut`, `CloseDoorOut`, or `DoorBlocked`. |

**Common Door States:**
- `OpenDoorIn` - Door opened inward
- `CloseDoorIn` - Door closed (opens inward)
- `OpenDoorOut` - Door opened outward
- `CloseDoorOut` - Door closed (opens outward)
- `DoorBlocked` - Door cannot be opened

---

## Example: Complete Event with Sub-Events

```json
{
  "eventId": "boss_arena_wave",
  "repeatable": false,
  "triggerBlockTypes": [
    "Furniture_Boss_Altar"
  ],
  "hordeTimerMinMs": 8000,
  "hordeTimerMaxMs": 12000,
  "hordeWaveIntervalMs": 15000,
  "hordeWaveCount": 5,
  "specialTimerMs": 20000,
  "endConditionType": "entity_killed",
  "endConditionData": {
    "entities": {
      "C_Shadow_Knight": 3,
      "C_Wraith": 2
    }
  },
  "subEvents": [
    {
      "id": "opening_gates_close",
      "trigger": {
        "triggerType": "timer",
        "triggerData": {
          "delayMs": 10000
        }
      },
      "actions": [
        {
          "actionType": "execute_command",
          "actionData": {
            "command": "say Gates closing!"
          }
        },
        {
          "actionType": "remove_blocks",
          "actionData": {
            "coordinates": [
              [100, 65, 200],
              [101, 65, 200],
              [102, 65, 200]
            ]
          }
        }
      ]
    },
    {
      "id": "reinforce_after_kills",
      "trigger": {
        "triggerType": "entity_killed",
        "triggerData": {
          "count": 5
        }
      },
      "actions": [
        {
          "actionType": "spawn_npc",
          "actionData": {
            "npcType": "C_Rex_Cave",
            "x": 105,
            "y": 64,
            "z": 205
          }
        }
      ]
    },
    {
      "id": "unlock_escape_route",
      "trigger": {
        "triggerType": "sub_event_completion",
        "triggerData": {
          "subEventId": "reinforce_after_kills"
        }
      },
      "actions": [
        {
          "actionType": "unlock_interaction",
          "actionData": {
            "blockType": "escape_portal"
          }
        },
        {
          "actionType": "execute_command",
          "actionData": {
            "command": "say Escape route unlocked!"
          }
        }
      ]
    }
  ]
}
```

---

## Example: Door Control Event

This example demonstrates using the `SET_BLOCK_STATE` action to control doors during an event. The doors will automatically reset to their closed states when the `/reset` command is used.

```json
{
  "eventId": "castle_siege",
  "repeatable": false,
  "triggerBlockTypes": [
    "Furniture_Village_Brazier"
  ],
  "hordeTimerMinMs": 10000,
  "hordeTimerMaxMs": 15000,
  "hordeWaveIntervalMs": 20000,
  "hordeWaveCount": 5,
  "specialTimerMs": 30000,
  "endConditionType": "timer",
  "endConditionData": {
    "durationMs": 180000
  },
  "subEvents": [
    {
      "id": "close_main_gate",
      "trigger": {
        "triggerType": "timer",
        "triggerData": {
          "delayMs": 5000
        }
      },
      "actions": [
        {
          "actionType": "set_block_state",
          "actionData": {
            "x": 150,
            "y": 64,
            "z": 200,
            "state": "CloseDoorIn"
          }
        },
        {
          "actionType": "execute_command",
          "actionData": {
            "command": "say The main gate has sealed shut!"
          }
        }
      ]
    },
    {
      "id": "open_escape_door",
      "trigger": {
        "triggerType": "entity_killed",
        "triggerData": {
          "count": 10
        }
      },
      "actions": [
        {
          "actionType": "set_block_state",
          "actionData": {
            "x": 175,
            "y": 65,
            "z": 225,
            "state": "OpenDoorOut"
          }
        },
        {
          "actionType": "execute_command",
          "actionData": {
            "command": "say The escape route is now open!"
          }
        }
      ]
    }
  ]
}
```

---

## Notes and Best Practices

- **Block Type Names:** Use block type IDs from your server (e.g., `Furniture_Village_Brazier`). Test in-game or check server logs for exact names.
- **Coordinates:** Format is `[x, y, z]` as integers. These are world coordinates.
- **Entity Roles:** Use entity role names defined in your `entity_roles.json` configuration.
- **Event IDs:** Must be unique. Use snake_case naming (e.g., `boss_arena_wave`).
- **Sub-Event IDs:** Must be unique within their parent event. Use descriptive names.
- **Milliseconds:** All time values are in milliseconds (1 second = 1000 ms).
- **Trigger Block Types:** Event starts on first interaction with any block in the list (one-time per session across all listed blocks).
- **One-Time Execution:** Sub-events execute exactly once per event instance, regardless of trigger type.
- **Action Execution:** All actions in a sub-event execute on the same tick they're triggered.
- **Error Handling:** If an action fails, it logs an error but doesn't prevent other actions from executing.
- **Door State Tracking:** All doors controlled by `SET_BLOCK_STATE` actions are automatically tracked. Using the `/reset` command will reset them to their opposite state (Open ↔ Close).
- **Door State Opposites:** The following state pairs are reversed on reset:
  - `OpenDoorIn` ↔ `CloseDoorIn`
  - `OpenDoorOut` ↔ `CloseDoorOut`
  - `DoorBlocked` remains `DoorBlocked` (no opposite state)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Event doesn't start | Check event ID spelling, verify it exists in `/list-events` |
| Sub-event doesn't trigger | Verify trigger type and data format, check console for errors |
| Blocks not removed/added | Check coordinate format and block type names in logs |
| Command not executing | Verify command syntax (no leading `/`), check server permissions |
| Event runs twice | Check that `repeatable` is set to `false` if one-time only is desired |

---

## File Validation

The `events.json` file is automatically validated when loaded. Invalid JSON or missing required fields will cause:
1. Console error message with details
2. Fallback to default events configuration
3. Potential loss of custom event data

Always validate JSON syntax before deploying to production!
