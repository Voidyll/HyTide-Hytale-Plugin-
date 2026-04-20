package me.voidyll.systems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import me.voidyll.data.ActivatedTriggersManager;
import me.voidyll.data.ActiveSpawnGroupManager;
import me.voidyll.data.TriggerZoneData;
import me.voidyll.data.TriggerZoneManager;
import me.voidyll.commands.TriggerSpawnCommand;

public class PlayerEnterTrigger {

	private final TriggerZoneManager zoneManager;
	private final ActiveSpawnGroupManager groupManager;
	private final ActivatedTriggersManager activatedTriggersManager;
	private final TriggerSpawnCommand triggerSpawnCommand;
	private final Map<String, Set<String>> activeZonesByPlayerUuid = new HashMap<>();

	public PlayerEnterTrigger(TriggerZoneManager zoneManager, ActiveSpawnGroupManager groupManager, 
	                          ActivatedTriggersManager activatedTriggersManager, TriggerSpawnCommand triggerSpawnCommand) {
		this.zoneManager = zoneManager;
		this.groupManager = groupManager;
		this.activatedTriggersManager = activatedTriggersManager;
		this.triggerSpawnCommand = triggerSpawnCommand;
	}

	public void registerSystems(ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
		entityStoreRegistry.registerSystem(new ProximityCheckTickingSystem());
	}

	private class ProximityCheckTickingSystem extends EntityTickingSystem<EntityStore> {

		@Override
		public Query<EntityStore> getQuery() {
			return PlayerRef.getComponentType();
		}

		@Override
		public void tick(float delta, int tick, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store,
				CommandBuffer<EntityStore> commandBuffer) {
			for (int i = 0; i < chunk.size(); i++) {
				Ref<EntityStore> ref = chunk.getReferenceTo(i);
				Player player = store.getComponent(ref, Player.getComponentType());
				if (player == null) {
					continue;
				}

				TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
				if (transform == null) {
					continue;
				}

				Vector3d position = transform.getPosition();
				checkPlayerZones(player, position);
			}
		}
	}

	private void checkPlayerZones(Player player, Vector3d position) {
		World playerWorld = player.getWorld();
		if (playerWorld == null) {
			return;
		}

		String playerUuid = player.getUuid().toString();
		Set<String> activeZones = activeZonesByPlayerUuid.computeIfAbsent(playerUuid, uuid -> new HashSet<>());

		List<TriggerZoneData> zones = zoneManager.loadZones();
		for (TriggerZoneData zone : zones) {
			if (!playerWorld.getName().equalsIgnoreCase(zone.getWorldName())) {
				continue;
			}

			double dx = position.x - zone.getX();
			double dy = position.y - zone.getY();
			double dz = position.z - zone.getZ();
			double distanceSquared = dx * dx + dy * dy + dz * dz;

			boolean inside = distanceSquared <= (zone.getRadius() * zone.getRadius());

			if (inside && !activeZones.contains(zone.getName())) {
				activeZones.add(zone.getName());
				handleTriggerEnter(player, zone, playerWorld);
			} else if (!inside && activeZones.contains(zone.getName())) {
				activeZones.remove(zone.getName());
				handleTriggerExit(player, zone);
			}
		}
	}

	private void handleTriggerEnter(Player player, TriggerZoneData zone, World world) {
		String playerUuid = player.getUuid().toString();
		String type = zone.getType() != null ? zone.getType().toLowerCase() : "checkpoint";

		if (type.equals("checkpoint")) {
			// Checkpoint type: set active group
			groupManager.setActiveGroup(playerUuid, zone.getGroupNumber());
			if (DebugMessageSettings.areDebugMessagesEnabled()) {
				player.sendMessage(Message.raw("Entered trigger zone: %s (Group %d now active)"
						.formatted(zone.getName(), zone.getGroupNumber())));
				player.sendMessage(Message.raw("[DEBUG] Active group set to: %d for player %s"
						.formatted(zone.getGroupNumber(), playerUuid)));
			}
		} else if (type.equals("ambient")) {
			// Ambient type: trigger ambient spawns once
			if (activatedTriggersManager.isUsed(zone.getName())) {
				if (DebugMessageSettings.areDebugMessagesEnabled()) {
					player.sendMessage(Message.raw("Entered trigger zone: %s (already activated)"
							.formatted(zone.getName())));
				}
				return;
			}

			// Mark as used
			activatedTriggersManager.setUsed(zone.getName());

			// Get player reference for targeting
			Ref<EntityStore> playerRef = world.getEntityRef(player.getUuid());

			// Trigger ambient spawns for this group
			java.util.List<Integer> groupNumbers = java.util.Collections.singletonList(zone.getGroupNumber());
			triggerSpawnCommand.spawnForGroups(groupNumbers, "ambient", playerRef);

			if (DebugMessageSettings.areDebugMessagesEnabled()) {
				player.sendMessage(Message.raw("Entered trigger zone: %s (Ambient spawns triggered!)"
						.formatted(zone.getName())));
			}
		} else if (type.equals("boss") || type.equals("patrol")) {
			// Boss/Patrol type: trigger spawns if not disabled
			if (activatedTriggersManager.isDisabled(zone.getName())) {
				if (DebugMessageSettings.areDebugMessagesEnabled()) {
					player.sendMessage(Message.raw("Entered trigger zone: %s (disabled)"
							.formatted(zone.getName())));
				}
				return;
			}

			if (activatedTriggersManager.isUsed(zone.getName())) {
				if (DebugMessageSettings.areDebugMessagesEnabled()) {
					player.sendMessage(Message.raw("Entered trigger zone: %s (already triggered)"
							.formatted(zone.getName())));
				}
				return;
			}

			// Mark as used
			activatedTriggersManager.setUsed(zone.getName());

			// Get player reference for targeting
			Ref<EntityStore> playerRef = world.getEntityRef(player.getUuid());

			// Trigger spawns for this group with optional identifier
			java.util.List<Integer> groupNumbers = java.util.Collections.singletonList(zone.getGroupNumber());
			String identifier = zone.getIdentifier();
			triggerSpawnCommand.spawnForGroups(groupNumbers, type, playerRef, identifier);

			if (DebugMessageSettings.areDebugMessagesEnabled()) {
				player.sendMessage(Message.raw("Entered trigger zone: %s (%s spawns triggered!)"
						.formatted(zone.getName(), type)));
			}
		}
	}

	private void handleTriggerExit(Player player, TriggerZoneData zone) {
		String type = zone.getType() != null ? zone.getType().toLowerCase() : "checkpoint";

		if (type.equals("checkpoint")) {
			if (DebugMessageSettings.areDebugMessagesEnabled()) {
				player.sendMessage(Message.raw("Left trigger zone: %s (Active group unchanged)"
						.formatted(zone.getName())));
			}
		}
		// Ambient triggers don't send exit messages
	}
}
