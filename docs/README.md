# Vermintide Spawn Simulator Plugin

A comprehensive Hytale server plugin for automated enemy spawning with configurable spawn director systems, event overrides, and sophisticated trigger mechanisms.

## Overview

This plugin provides:

- **Spawn Director System** - Automated multi-wave horde spawning with configurable timers and entity caps
- **Event System** - Override spawn behavior with custom events triggered by time, block interactions, or boss kills
- **Sub-Events** - Chain complex spawn sequences with multiple triggers and actions
- **World Management** - Create spawn zones, define trigger areas, and reset world state
- **Full Command Suite** - 16 commands for controlling all aspects of the plugin

## Documentation Structure

### 📋 [EVENTS_JSON_FORMAT.md](EVENTS_JSON_FORMAT.md)
Complete reference for configuring the `events.json` file. Covers:
- Event configuration structure (15 core fields)
- All 5 end condition types (TIMER, TRIGGER_ZONE, BLOCK_BREAK, ENTITY_KILLED, BLOCK_INTERACTION)
- Sub-events system (4 trigger types, 6 action types)
- Full working examples with best practices
- Troubleshooting guide

**Use this when:** Configuring custom events or setting up complex spawn scenarios.

### 🎮 [COMMANDS.md](COMMANDS.md)
Complete reference for all 16 plugin commands. Organized by category:
- **Event Management** - Start, stop, list events
- **Spawn Timers** - Pause, restart, unpause horde and special timers
- **Configuration** - Create spawn groups and trigger zones
- **Utilities** - Manual spawn triggering, plugin info

**Use this when:** Running commands or troubleshooting plugin behavior.

## Quick Start

### Basic Setup

1. **Create a spawn group:**
   ```
   /create-spawn 0 100 64 200 10
   ```
   This creates a spawn area at coordinates (100, 64, 200) with a 10-block radius.

2. **Test spawning:**
   ```
   /trigger-spawn 0
   ```
   Manually spawn NPCs at that location.

3. **Create a trigger zone (optional):**
   ```
   /create-trigger-zone boss_arena 100 60 200 150 70 250
   ```
   This defines a rectangular area that can trigger events when players enter.

4. **View available events:**
   ```
   /list-events
   ```
   See all configured events and their status.

5. **Start an event:**
   ```
   /start-event event_id
   ```
   Replace `event_id` with an actual event ID from `/list-events`.

### Basic Event Configuration

Events are configured in `events.json`. Basic structure:

```json
{
  "eventId": "my_event",
  "repeatable": false,
  "horde_timer_1": 30000,
  "horde_timer_2": 35000,
  "horde_timer_3": 40000,
  "special_timer": 60000,
  "max_mobs_total": 200,
  "max_special_mobs": 4,
  "endConditionType": "TIMER",
  "endConditionData": {
    "durationMs": 120000
  }
}
```

This creates an event that:
- Runs for 2 minutes (120,000 ms)
- Spawns regular hordes every 30-40 seconds
- Spawns special mobs every 60 seconds
- Ends automatically after 2 minutes

See [EVENTS_JSON_FORMAT.md](EVENTS_JSON_FORMAT.md) for all available fields.

## Core Concepts

### Spawn Director
The plugin continuously manages NPC spawning. The director tracks:
- **Horde Timer** - Controls regular NPC spawning (3 separate cycles)
- **Special Timer** - Controls boss/elite NPC spawning
- **Entity Caps** - Max total NPCs (default 200) and max special NPCs (default 4)

When timers elapse, NPCs spawn at configured locations up to the entity caps.

### Events
Events temporarily override spawn director values. An event:
- Has custom timers/caps that replace spawn director values
- Can be triggered by time, block interactions, zone entry, or boss kills
- Has optional sub-events that execute during the event
- Can be one-time only or repeatable

Only one event can run at a time. Starting a new event stops the current one.

### Sub-Events
Sub-events are actions executed during an event. Each sub-event:
- Has a **trigger** (TIMER, BLOCK_INTERACTION, ENTITY_KILLED, or SUB_EVENT_COMPLETION)
- Has **actions** that execute when the trigger fires (spawn NPCs, remove blocks, execute commands, etc.)
- Executes only once per event instance

Example: "After 30 seconds of event running, spawn a boss and unlock an interaction."

### Block Triggers
Events can be triggered by player interaction with specific block types:
- Player breaks/interacts with a specified block type
- Event starts automatically
- Same block type won't trigger again until `/reset` or event is repeatable
- Useful for puzzle-based event activation

## Default Configuration

The plugin ships with two example events in `events.json`:

1. **righteous_stand_first_event** - Tutorial/warm-up event
   - 4-minute duration with escalating horde spawning
   - No sub-events
   
2. **righteous_activate_cannon_lever** - Block-triggered event
   - Triggered by placing a TNT block
   - Has sub-events that spawn NPCs and execute commands

See [EVENTS_JSON_FORMAT.md](EVENTS_JSON_FORMAT.md#complete-working-example) for the full example configuration.

## Common Tasks

### Start an event from a specific block
Configure `triggerBlockTypes` in the event. When players interact with those blocks, the event starts:
```json
{
  "triggerBlockTypes": ["stone", "oak_log"],
  "endConditionType": "BLOCK_INTERACTION"
}
```

### Spawn NPCs at event start (via sub-event)
```json
{
  "subEvents": [
    {
      "trigger": {
        "type": "TIMER",
        "delay": 0
      },
      "actions": [
        {
          "type": "SPAWN_NPC",
          "npcType": "C_Shadow_Knight",
          "x": 100,
          "y": 65,
          "z": 200,
          "count": 5
        }
      ]
    }
  ]
}
```

### Remove blocks at specific times
```json
{
  "trigger": {
    "type": "TIMER",
    "delay": 15000
  },
  "actions": [
    {
      "type": "REMOVE_BLOCKS",
      "blockTypes": ["oak_wood", "oak_leaves"],
      "x": 100,
      "y": 64,
      "z": 200,
      "radius": 10
    }
  ]
}
```

### Chain sub-events
Use `TIMER` to create initial delay, then use `SUB_EVENT_COMPLETION` for dependent sub-events:
```json
{
  "subEvents": [
    {
      "id": "spawn_wave_1",
      "trigger": { "type": "TIMER", "delay": 0 },
      "actions": [ /* spawn wave 1 */ ]
    },
    {
      "id": "spawn_wave_2",
      "trigger": { "type": "SUB_EVENT_COMPLETION", "onCompletion": "spawn_wave_1" },
      "actions": [ /* spawn wave 2 */ ]
    }
  ]
}
```

## File Locations

- **Events Configuration:** `config/events.json` in your world save
- **Spawn Configuration:** Stored via `/create-spawn` commands
- **Trigger Zones:** Stored via `/create-trigger-zone` commands
- **Plugin JAR:** Placed in server plugins directory

## Supported NPC Types

Common types (configure in `events.json` actions or via `/trigger-spawn`):
- `C_Shadow_Knight` - Dark warrior class
- `C_Wraith` - Ghost-like creature
- `C_Zombie` - Basic undead
- `C_Skeleton` - Archer skeleton
- `C_Vampire` - Advanced melee threat

(Exact available types depend on your Hytale server configuration)

## Performance Considerations

- **Entity Caps** - The plugin respects max entity counts to prevent lag
- **Spawn Radius** - Larger spawn radii create more spread-out NPCs
- **Event Duration** - Shorter events consume fewer ticks
- **Sub-Event Quantity** - Complex events with many sub-events may impact tick time

Recommendation: Keep total entities <250 and events <10 minutes for optimal performance.

## Troubleshooting

### Event won't start
- Check event ID with `/list-events`
- Verify the event is defined in `events.json`
- Check server console for errors

### Too many NPCs spawning
- Use `/horde-timer-pause` to pause spawning
- Reduce `max_mobs_total` or `max_special_mobs` in event config
- Use `/reset` to clear the world

### Block trigger not working
- Verify block type in `triggerBlockTypes` is correct
- Use `/reset` to reset trigger states
- Check that event ID is accurate

### Commands not working
- Verify you have operator (op) permissions
- Check command syntax matches documentation
- See [COMMANDS.md](COMMANDS.md) for complete reference

### Need to start fresh
- Run `/reset` to clean up NPCs, clear triggers, and return players to spawn

## Advanced Usage

### Creating Multi-Wave Events
Use sub-events with `TIMER` triggers to spawn multiple waves:
```json
{
  "endConditionType": "TIMER",
  "endConditionData": { "durationMs": 300000 },
  "subEvents": [
    { "id": "wave1", "trigger": { "type": "TIMER", "delay": 0 }, ... },
    { "id": "wave2", "trigger": { "type": "TIMER", "delay": 60000 }, ... },
    { "id": "wave3", "trigger": { "type": "TIMER", "delay": 120000 }, ... }
  ]
}
```

### Boss Fights
Use `ENTITY_KILLED` trigger to spawn progressively harder enemies:
```json
{
  "endConditionType": "ENTITY_KILLED",
  "subEvents": [
    {
      "trigger": { "type": "ENTITY_KILLED", "threshold": 0 },
      "actions": [ { "type": "SPAWN_NPC", "npcType": "boss_1" } ]
    },
    {
      "trigger": { "type": "ENTITY_KILLED", "threshold": 1 },
      "actions": [ { "type": "SPAWN_NPC", "npcType": "boss_2_improved" } ]
    }
  ]
}
```

### Dynamic World Modification
Use sub-event actions to alter the arena during events:
```json
{
  "actions": [
    { "type": "REMOVE_BLOCKS", "blockTypes": ["barrier_block"], ... },
    { "type": "ADD_BLOCKS", "blockType": "stone", ... },
    { "type": "EXECUTE_COMMAND", "command": "effect give @a resistance 10 2" }
  ]
}
```

## Support & Resources

- **Configuration Help:** See [EVENTS_JSON_FORMAT.md](EVENTS_JSON_FORMAT.md)
- **Command Reference:** See [COMMANDS.md](COMMANDS.md)
- **Block Types:** Check Hytale documentation for available block IDs
- **NPC Types:** Check your server configuration for available entity types

---

**Version:** See `/first` command for current plugin version.
