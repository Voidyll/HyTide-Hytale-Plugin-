package me.voidyll;

import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.voidyll.commands.AddSubEventCommand;
import me.voidyll.commands.CreateEventCommand;
import me.voidyll.commands.CreateItemSpawnCommand;
import me.voidyll.commands.CreateSpawnCommand;
import me.voidyll.commands.CreateTriggerZoneCommand;
import me.voidyll.commands.DeleteEventCommand;
import me.voidyll.commands.DeleteItemSpawnCommand;
import me.voidyll.commands.DeleteSpawnCommand;
import me.voidyll.commands.DeleteTriggerZoneCommand;
import me.voidyll.commands.HytideHelpCommand;
import me.voidyll.commands.RemoveSubEventCommand;
import me.voidyll.commands.FirstCommand;
import me.voidyll.commands.HordeTimerPauseCommand;
import me.voidyll.commands.HordeTimerRestartCommand;
import me.voidyll.commands.HordeTimerUnpauseCommand;
import me.voidyll.commands.ListItemSpawnsCommand;
import me.voidyll.commands.ListSpawnsCommand;
import me.voidyll.commands.ListTriggerZonesCommand;
import me.voidyll.commands.ListEventsCommand;
import me.voidyll.commands.ResetCommand;
import me.voidyll.commands.ShowEntityRolesDataLocationCommand;
import me.voidyll.commands.ShowEventsDataLocationCommand;
import me.voidyll.commands.ShowItemSpawnsDataLocationCommand;
import me.voidyll.commands.ShowSpawnMarkersDataLocationCommand;
import me.voidyll.commands.ShowTriggerZonesDataLocationCommand;
import me.voidyll.commands.SpecialTimerPauseCommand;
import me.voidyll.commands.SpecialTimerRestartCommand;
import me.voidyll.commands.SpecialTimerUnpauseCommand;
import me.voidyll.commands.StartEventCommand;
import me.voidyll.commands.StopEventCommand;
import me.voidyll.commands.ToggleDebugCommand;
import me.voidyll.commands.TriggerSpawnCommand;
import me.voidyll.data.ActivatedTriggersManager;
import me.voidyll.data.ActiveSpawnGroupManager;
import me.voidyll.data.BlockInteractTriggerManager;
import me.voidyll.data.ItemSpawnDataManager;
import me.voidyll.data.RoleConfigManager;
import me.voidyll.data.SpawnDataManager;
import me.voidyll.data.TriggerZoneManager;
import me.voidyll.systems.EventHandler;
import me.voidyll.systems.ItemSpawnSystem;
import me.voidyll.systems.PlayerEnterTrigger;
import me.voidyll.systems.PlayerInteractTrigger;
import me.voidyll.systems.SpawnDirectorSystem;
import me.voidyll.systems.StuckNPCCleanupSystem;
import me.voidyll.utils.AssetExtractor;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class FirstPlugin extends JavaPlugin {
    private SpawnDataManager spawnDataManager;
    private ItemSpawnDataManager itemSpawnDataManager;
    private TriggerZoneManager triggerZoneManager;
    private ActiveSpawnGroupManager activeGroupManager;
    private ActivatedTriggersManager activatedTriggersManager;
    private BlockInteractTriggerManager blockInteractTriggerManager;
    private RoleConfigManager roleConfigManager;

    public FirstPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Auto-extract asset pack from JAR on first run or version change
        AssetExtractor extractor = new AssetExtractor(
                this.getFile(),
                this.getDataDirectory(),
                this.getLogger(),
                this.getManifest().getVersion().toString()
        );
        extractor.extractIfNeeded();

        // Initialize spawn data manager
        spawnDataManager = new SpawnDataManager(this.getDataDirectory());
        itemSpawnDataManager = new ItemSpawnDataManager(this.getDataDirectory());
        triggerZoneManager = new TriggerZoneManager(this.getDataDirectory());
        activeGroupManager = new ActiveSpawnGroupManager(this.getDataDirectory());
        activatedTriggersManager = new ActivatedTriggersManager(this.getDataDirectory());
        blockInteractTriggerManager = new BlockInteractTriggerManager(this.getDataDirectory());
        roleConfigManager = new RoleConfigManager(this.getDataDirectory());

        // Initialize everything
        CommandRegistry commandRegistry = this.getCommandRegistry();
        commandRegistry.registerCommand(new FirstCommand());
        commandRegistry.registerCommand(new CreateSpawnCommand(spawnDataManager));
        commandRegistry.registerCommand(new CreateItemSpawnCommand(itemSpawnDataManager));
        commandRegistry.registerCommand(new DeleteSpawnCommand(spawnDataManager));
        commandRegistry.registerCommand(new DeleteItemSpawnCommand(itemSpawnDataManager));
        commandRegistry.registerCommand(new ListSpawnsCommand(spawnDataManager));
        commandRegistry.registerCommand(new ListItemSpawnsCommand(itemSpawnDataManager));
        commandRegistry.registerCommand(new ShowSpawnMarkersDataLocationCommand(spawnDataManager));
        commandRegistry.registerCommand(new ShowItemSpawnsDataLocationCommand(itemSpawnDataManager));
        commandRegistry.registerCommand(new ShowTriggerZonesDataLocationCommand(triggerZoneManager));
        TriggerSpawnCommand triggerSpawnCommand = new TriggerSpawnCommand(spawnDataManager, activeGroupManager, roleConfigManager);
        commandRegistry.registerCommand(triggerSpawnCommand);
        
        // Initialize spawn director system
        SpawnDirectorSystem spawnDirector = new SpawnDirectorSystem(triggerSpawnCommand, activeGroupManager, roleConfigManager);
        
        // Initialize event handler
        EventHandler eventHandler = new EventHandler(this.getDataDirectory(), spawnDirector);
        
        // Initialize item spawn system
        ItemSpawnSystem itemSpawnSystem = new ItemSpawnSystem(itemSpawnDataManager);
        
        // Register reset command with spawn director and item spawn system
        commandRegistry.registerCommand(new ResetCommand(activeGroupManager, activatedTriggersManager, blockInteractTriggerManager, triggerZoneManager, spawnDirector, eventHandler, itemSpawnSystem));
        
        // Register spawn director timer control commands
        commandRegistry.registerCommand(new HordeTimerPauseCommand(spawnDirector));
        commandRegistry.registerCommand(new HordeTimerUnpauseCommand(spawnDirector));
        commandRegistry.registerCommand(new HordeTimerRestartCommand(spawnDirector));
        commandRegistry.registerCommand(new SpecialTimerPauseCommand(spawnDirector));
        commandRegistry.registerCommand(new SpecialTimerUnpauseCommand(spawnDirector));
        commandRegistry.registerCommand(new SpecialTimerRestartCommand(spawnDirector));
        
        commandRegistry.registerCommand(new CreateTriggerZoneCommand(triggerZoneManager));
        commandRegistry.registerCommand(new DeleteTriggerZoneCommand(triggerZoneManager));
        commandRegistry.registerCommand(new ListTriggerZonesCommand(triggerZoneManager));
        
        // Register event commands
        commandRegistry.registerCommand(new StartEventCommand(eventHandler));
        commandRegistry.registerCommand(new StopEventCommand(eventHandler));
        commandRegistry.registerCommand(new ListEventsCommand(eventHandler));
        commandRegistry.registerCommand(new CreateEventCommand(eventHandler));
        commandRegistry.registerCommand(new DeleteEventCommand(eventHandler));
        commandRegistry.registerCommand(new AddSubEventCommand(eventHandler));
        commandRegistry.registerCommand(new RemoveSubEventCommand(eventHandler));
        commandRegistry.registerCommand(new ShowEventsDataLocationCommand(eventHandler));
        commandRegistry.registerCommand(new ShowEntityRolesDataLocationCommand(roleConfigManager));
        commandRegistry.registerCommand(new ToggleDebugCommand());
        commandRegistry.registerCommand(new HytideHelpCommand());

        // Register player interaction and block break systems
        new PlayerInteractTrigger(blockInteractTriggerManager, eventHandler).registerSystems(this.getEntityStoreRegistry());
        new PlayerEnterTrigger(triggerZoneManager, activeGroupManager, activatedTriggersManager, triggerSpawnCommand)
            .registerSystems(this.getEntityStoreRegistry());
        
        // Register stuck NPC cleanup system
        new StuckNPCCleanupSystem().registerSystems(this.getEntityStoreRegistry());
        
        // Register spawn director system
        spawnDirector.registerSystems(this.getEntityStoreRegistry());
        
        // Register event handler system
        eventHandler.registerSystems(this.getEntityStoreRegistry());
    }
}