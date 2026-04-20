# Hytale Command Execution - Quick Reference Card

## 🎯 The Single Most Important Code Pattern

```java
// Get CommandManager
CommandManager cmdManager = HytaleServer.get().getCommandManager();

// Execute command
CompletableFuture<Void> future = cmdManager.handleCommand(
    ConsoleSender.INSTANCE,  // Has all permissions
    "npc spawn Rat"           // Your command string
);

// Wait for it (blocking)
future.join();
```

---

## 📦 Required Imports (Copy-Paste)

```java
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.HytaleServer;
import java.util.concurrent.CompletableFuture;
```

---

## 🚀 5 Most Common Patterns

### 1. Single Command (Simplest)
```java
CommandManager.get().handleCommand(
    ConsoleSender.INSTANCE, 
    "npc spawn Rat"
).join();
```

### 2. Spawn Multiple (In Loop)
```java
for (int i = 0; i < 5; i++) {
    CommandManager.get().handleCommand(
        ConsoleSender.INSTANCE,
        "npc spawn Rat"
    ).join();
}
```

### 3. Multiple with Feedback
```java
CommandManager cmdMgr = HytaleServer.get().getCommandManager();
int success = 0;

for (int i = 0; i < 5; i++) {
    try {
        cmdMgr.handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat").join();
        success++;
        context.sendMessage(Message.raw("✓ Spawned " + (i+1) + "/5"));
    } catch (Exception e) {
        context.sendMessage(Message.raw("✗ Failed: " + e.getMessage()));
    }
}
```

### 4. Async (Non-Blocking)
```java
CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat")
    .thenRun(() -> System.out.println("Done!"));
```

### 5. On World Thread (Safest)
```java
world.execute(() -> {
    CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat").join();
});
```

---

## ⚡ API Methods You Need

```java
// Main execution - pick one:
CommandManager.get()
    .handleCommand(CommandSender sender, String command)
    .handleCommand(PlayerRef playerRef, String command)

// Getting the manager:
HytaleServer.get().getCommandManager()
CommandManager.get()  // Singleton

// Getting command sender:
ConsoleSender.INSTANCE  // Always has permissions
```

---

## 🧵 Threading Questions

| Scenario | Answer | Example |
|----------|--------|---------|
| **Execute from command?** | Yes, safe ✓ | `executeSync()` method |
| **Need world access?** | Wrap in `world.execute()` ⚠️ | `world.execute(() -> { command })` |
| **Multiple spawns?** | Loop + `.join()` each | See pattern #2 above |
| **Should I block?** | Yes for spawning ✓ | Always use `.join()` |

---

## 📝 Copy-Paste Template

```java
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.HytaleServer;

// In your method:
try {
    CommandManager cmdMgr = HytaleServer.get().getCommandManager();
    
    // TODO: Replace with your command
    String command = "npc spawn Rat";
    
    cmdMgr.handleCommand(ConsoleSender.INSTANCE, command).join();
    context.sendMessage(Message.raw("§a✓ Command executed"));
    
} catch (Exception e) {
    context.sendMessage(Message.raw("§c✗ Error: " + e.getMessage()));
}
```

---

## 🔍 What Each Part Does

```java
HytaleServer           // Singleton server instance
    .get()             // Get the current server
    .getCommandManager() // Get the command system

CommandManager         // The command execution system
    .handleCommand(    // Execute a command
        ConsoleSender.INSTANCE,  // Who's running it (console = admin)
        "npc spawn Rat"          // The command string
    )                  // Returns: CompletableFuture<Void>
    .join();           // Wait for completion (blocks until done)
```

---

## ✅ Complete Example: Spawn 5 Rats

```java
public void spawnRats(CommandContext context) {
    try {
        CommandManager cmdMgr = HytaleServer.get().getCommandManager();
        
        for (int i = 0; i < 5; i++) {
            cmdMgr.handleCommand(ConsoleSender.INSTANCE, "npc spawn Rat").join();
            context.sendMessage(Message.raw("§a[" + (i+1) + "/5] Rat spawned"));
        }
        
        context.sendMessage(Message.raw("§aAll 5 rats spawned successfully!"));
        
    } catch (Exception e) {
        context.sendMessage(Message.raw("§cError: " + e.getMessage()));
    }
}
```

---

## 📚 Files in This Project

| File | Purpose |
|------|---------|
| [COMMAND_EXECUTION_RESEARCH.md](COMMAND_EXECUTION_RESEARCH.md) | In-depth research findings |
| [COMMAND_EXECUTION_SUMMARY.md](COMMAND_EXECUTION_SUMMARY.md) | Complete guide with patterns |
| **THIS FILE** | Quick reference (you are here) |
| [src/main/java/me/voidyll/utils/CommandExecutor.java](Hytale%20Plugin%201%20%28Vermintide%20Spawns%29/src/main/java/me/voidyll/utils/CommandExecutor.java) | Reusable utility class |
| [src/main/java/me/voidyll/commands/SpawnMobCommand.java](Hytale%20Plugin%201%20%28Vermintide%20Spawns%29/src/main/java/me/voidyll/commands/SpawnMobCommand.java) | Example implementation |

---

## 🎓 Learning Path

1. **Start here**: This quick reference (5 min read)
2. **Copy the template**: Use the template above (2 min)
3. **Deeper dive**: Read [COMMAND_EXECUTION_SUMMARY.md](COMMAND_EXECUTION_SUMMARY.md) (15 min)
4. **Full understanding**: Check [COMMAND_EXECUTION_RESEARCH.md](COMMAND_EXECUTION_RESEARCH.md) (30 min)
5. **Ready to code**: Use [CommandExecutor.java](Hytale%20Plugin%201%20%28Vermintide%20Spawns%29/src/main/java/me/voidyll/utils/CommandExecutor.java) utility class

---

## 💡 Pro Tips

✓ **Always use `ConsoleSender.INSTANCE`** - It has all permissions  
✓ **Always use `.join()`** - Ensures spawning completes  
✓ **Catch exceptions** - Commands can fail  
✓ **Give user feedback** - Send messages about success/failure  
✓ **Use `world.execute()` if spawning**- Safer for entity operations  
✓ **Cache CommandManager** - Don't call `get()` repeatedly  

---

## ❌ Common Mistakes

❌ Not calling `.join()` - Command won't finish  
❌ Not catching exceptions - Crashes silent  
❌ Using player sender - Might not have permissions  
❌ Running on wrong thread - Can cause race conditions  
❌ Not validating input - Commands might fail unpredictably  

---

## 🔗 API Reference

```
CommandManager.handleCommand(CommandSender, String) 
    → CompletableFuture<Void>

HytaleServer.get() 
    → HytaleServer

HytaleServer.getCommandManager() 
    → CommandManager

CommandManager.get() 
    → CommandManager (static)

ConsoleSender.INSTANCE 
    → ConsoleSender (singleton)
```

---

## 📞 Still Need Help?

- Check **COMMAND_EXECUTION_SUMMARY.md** for patterns
- Look at **SpawnMobCommand.java** for real example
- Use **CommandExecutor.java** utility class
- Review the existing **TriggerSpawnCommand.java** in your codebase

All files are located in this project!
