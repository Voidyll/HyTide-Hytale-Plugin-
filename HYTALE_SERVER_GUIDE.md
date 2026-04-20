# Hytale Server & Log Files Location Guide

## 1. Server Location

### Main Installation Directory
**Path:** `C:\Users\bswea\AppData\Roaming\Hytale\install\release\package\game\`

This is where the Hytale game server is installed, including:
- **Latest Build:** `latest/` (symlink or latest version)
- **Specific Build:** `build-4/` (versioned builds)

### Server Executable Files
| File | Location | Purpose |
|------|----------|---------|
| **HytaleServer.jar** | `C:\Users\bswea\AppData\Roaming\Hytale\install\release\package\game\latest\Server\HytaleServer.jar` | Main server JAR (compiled Java) |
| **HytaleServer.aot** | `C:\Users\bswea\AppData\Roaming\Hytale\install\release\package\game\latest\Server\HytaleServer.aot` | AOT (Ahead-Of-Time) compiled binary |
| **Assets.zip** | `C:\Users\bswea\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip` | Server assets archive |

### Java Runtime
**Path:** `C:\Users\bswea\AppData\Roaming\Hytale\install\release\package\jre\`
- Contains the embedded Java Runtime Environment for running the server

---

## 2. Log Files Location

### Server Logs by World
Logs are stored in separate directories for each server world:

```
C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\
├── New World/
│   └── logs/              # Server logs for "New World"
├── Righteous Stand/
│   └── logs/              # Server logs for "Righteous Stand"
└── Modding Test World/
    └── logs/              # Server logs for "Modding Test World"
```

### Log File Naming Convention
- **Format:** `YYYY-MM-DD_HH-MM-SS_server.log`
- **Example:** `2026-01-24_20-32-13_server.log`
- **Lock files:** `2026-01-24_20-32-13_server.log.lck` (indicates active server instance)

### Latest Server Logs (Righteous Stand World)
Most recent server instance log:
```
C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\Righteous Stand\logs\2026-01-24_20-32-13_server.log
```

### Client Logs (Game Client)
**Path:** `C:\Users\bswea\AppData\Roaming\Hytale\UserData\Logs\`
- Format: `YYYY-MM-DD_HH-MM-SS_client.log`
- Example: `2026-01-24_17-19-47_client.log`

### Editor Logs
**Path:** `C:\Users\bswea\AppData\Roaming\Hytale\EditorUserData\Logs\`
- Example: `2026-01-22_15-24-28_editor.log`

---

## 3. Finding Asset Pack & Role Information in Logs

### What to Look For in Server Logs

#### Server Startup Information
```
[2026/01/25 02:32:14   INFO]  [HytaleServer] Booting up HytaleServer - Version: 2026.01.24-6e2d4fc36
[2026/01/25 02:32:14   INFO]  [PluginManager] Loading pending core plugins!
[2026/01/25 02:32:14   INFO]  [PluginManager] - Hytale:AssetModule
[2026/01/25 02:32:14   INFO]  [PluginManager] - Hytale:CommonAssetModule
```

#### Asset Pack Loading
Look for lines containing:
- `[AssetModule]` - Core asset loading
- `[CommonAssetModule]` - Common assets
- `[PluginManager]` - Plugin/mod loading status
- Search pattern: `Asset|assetpack|plugin|name`

#### NPC/Role Information
```
[2026/01/25 02:32:29 SEVERE] [NPC|P] Reloading nonexistent role null!
[2026/01/25 02:32:29  INFO] [CommandManager] Voidyll executed command: trigger-spawn 1
```
- Look for `[NPC|P]` entries for role/NPC status
- Look for `[CommandManager]` for command execution logs

#### World & Player Information
```
[2026/01/25 02:32:20   INFO] [Universe|P] Adding player 'Voidyll (932ce41e-a157-428b-8850-aca55b1f23f5)
[2026/01/25 02:32:21   INFO] [World|default] Adding player 'Voidyll' to world 'default'
```

#### Asset Sending to Players
```
[2026/01/25 02:32:20 FINEST] [LoginTiming] Entering stage 'setup:assets-request' took 27ms
[2026/01/25 02:32:20   FINE] [LoginTiming] Send Config Assets took 201ms
```

---

## 4. Accessing Server Console

### Direct Server Access
1. **Launch the Hytale game client**
2. **Select "Righteous Stand"** (or your world name) from the saves list
3. **Server starts automatically** in the background
4. **Console appears** in the Hytale game client (typically accessible via in-game commands)

### Via Log Files
- Monitor the log file in real-time using:
  ```powershell
  Get-Content -Path "C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\Righteous Stand\logs\2026-01-24_20-32-13_server.log" -Wait
  ```

### Server Configuration
**Location:** `C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\[WorldName]\config.json`
- Example: `Righteous Stand\config.json`
- Contains loaded mods and world configuration

---

## 5. Startup Scripts & Commands

### Current Setup (Embedded)
- **No batch/shell scripts needed** - Server runs embedded within the Hytale game
- Server launches automatically when you:
  1. Open Hytale game client
  2. Select a world from your saves
  3. Game client spawns the server process internally

### Server Startup Process
```
1. Hytale Launcher → game client
2. Game client → load HytaleServer.jar (or .aot)
3. Server → load config from world's config.json
4. Server → load mods from world's mods/ directory
5. Server → start listening for connections
6. Client → connects to local server (127.0.0.1:port)
```

### Plugin/Mod Loading
**Your Plugin Location:**
```
C:\Users\bswea\AppData\Roaming\Hytale\UserData\Mods\FirstHytalePlugin.jar
```

**Per-World Mods:**
```
C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\Righteous Stand\mods\
├── group_Vermintide Spawn Simulator/
│   └── spawn_markers.json
└── Hytale_Shop/
    └── barter_shop_state.json
```

---

## 6. Manifest Configuration

### Your Plugin Manifest
**Location:** `C:\Users\bswea\Projects\Hytale Plugin 1 (Vermintide Spawns)\src\main\resources\manifest.json`

```json
{
  "Group": "group",
  "Name": "Vermintide Spawn Simulator",
  "Version": "1.0.0",
  "Main": "me.voidyll.FirstPlugin",
  "IncludesAssetPack": true,
  "ServerVersion": "*"
}
```

### Asset Pack Information
- **IncludesAssetPack:** `true` = Your plugin includes assets
- **Group:** `group` - Used as namespace prefix (visible as `group_Vermintide Spawn Simulator` in mods)
- **Name:** `Vermintide Spawn Simulator` - Display name
- **Main:** `me.voidyll.FirstPlugin` - Entry point class

---

## 7. Recent Commands & Errors

### Last Command Executed
```
[2026/01/25 02:32:29   INFO] [CommandManager] Voidyll executed command: trigger-spawn 1
```

### Role/NPC Issues
```
[2026/01/25 02:32:29 SEVERE] [NPC|P] Reloading nonexistent role null!
```
- **Issue:** Trying to reload a role that doesn't exist
- **Solution:** Check spawn markers JSON for valid role references

### Missing Assets
```
[2026/01/25 02:32:20   WARN] [Hytale] Missing interaction Effect
```
- Logged when server can't find expected assets
- Check asset pack includes all referenced assets

---

## 8. Key Directories Summary

| Purpose | Path |
|---------|------|
| **Server Installation** | `C:\Users\bswea\AppData\Roaming\Hytale\install\release\package\game\` |
| **Server Logs** | `C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\[World]\logs\` |
| **Client Logs** | `C:\Users\bswea\AppData\Roaming\Hytale\UserData\Logs\` |
| **Your Plugin JAR** | `C:\Users\bswea\AppData\Roaming\Hytale\UserData\Mods\FirstHytalePlugin.jar` |
| **World Mods** | `C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\[World]\mods\` |
| **World Config** | `C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\[World]\config.json` |
| **Build Output** | `C:\Users\bswea\Projects\Hytale Plugin 1 (Vermintide Spawns)\build\libs\` |

---

## 9. Commands for Monitoring

### Watch Server Logs in Real-Time (PowerShell)
```powershell
Get-Content -Path "C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\Righteous Stand\logs\2026-01-24_20-32-13_server.log" -Wait
```

### Search for Asset Pack Mentions
```powershell
Select-String -Path "C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\Righteous Stand\logs\*.log" -Pattern "Asset|assetpack|plugin"
```

### Find Latest Log File
```powershell
Get-ChildItem -Path "C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\Righteous Stand\logs\" -Name "*.log" | Sort-Object -Descending | Select-Object -First 1
```

### List All Loaded Mods
```powershell
Get-ChildItem -Path "C:\Users\bswea\AppData\Roaming\Hytale\UserData\Saves\Righteous Stand\mods\" -Recurse
```

---

## 10. Building & Deploying Your Plugin

### Build Command
```bash
./gradlew clean build
```
- **Output:** `build/libs/FirstHytalePlugin.jar`
- **Located at:** `C:\Users\bswea\Projects\Hytale Plugin 1 (Vermintide Spawns)\build\libs\`

### Deployment to Server
1. Build your plugin JAR
2. Copy to: `C:\Users\bswea\AppData\Roaming\Hytale\UserData\Mods\FirstHytalePlugin.jar`
3. **Restart the server** (close and reopen the world in Hytale)
4. Check logs for `[PluginManager] - group:Vermintide Spawn Simulator` loading message

### Testing Command
Your test command from the logs:
```
/trigger-spawn 1
```
This is registered in your plugin and executed by the CommandManager.

---

## Notes

- **Server runs embedded:** No external startup scripts needed
- **Logs rotate by timestamp:** Each server start creates a new log
- **Asset packs are loaded automatically:** The `IncludesAssetPack: true` flag tells the server to load your assets
- **Roles/NPCs referenced in logs:** Check your asset pack definitions match the referenced role names
- **Plugin namespace:** Your mods appear with `group_` prefix (e.g., `group_Vermintide Spawn Simulator`)
