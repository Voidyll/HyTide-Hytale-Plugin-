package me.voidyll.systems;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Vector3i;

import me.voidyll.data.BlockInteractTriggerManager;


public class PlayerInteractTrigger {

    private final BlockInteractTriggerManager blockInteractTriggerManager;
    private final EventHandler eventHandler;

    public PlayerInteractTrigger(BlockInteractTriggerManager blockInteractTriggerManager, EventHandler eventHandler) {
        this.blockInteractTriggerManager = blockInteractTriggerManager;
        this.eventHandler = eventHandler;
        
        // Block types are resolved dynamically via EventHandler
    }

    public void registerSystems(ComponentRegistryProxy<EntityStore> entityStoreRegistry) {
        entityStoreRegistry.registerSystem(new UseBlockEventSystem(UseBlockEvent.Pre.class));
        entityStoreRegistry.registerSystem(new BreakBlockEventSystem(BreakBlockEvent.class));
    }

    private class UseBlockEventSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

        public UseBlockEventSystem(@NonNullDecl Class<UseBlockEvent.Pre> eventType) {
            super(eventType);
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                CommandBuffer<EntityStore> commandBuffer, UseBlockEvent.Pre event) {
            Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(index);
            Player player = store.getComponent(entityStoreRef, Player.getComponentType());
            if (player == null) {
                return;
            }

            if (event.getBlockType() == null) {
                return;
            }

            String blockTypeId = event.getBlockType().getId();
            Vector3i targetBlock = event.getTargetBlock();
            if (DebugMessageSettings.areDebugMessagesEnabled()) {
                player.sendMessage(Message.raw("You interacted with: %s at (%d, %d, %d)".formatted(blockTypeId, targetBlock.x, targetBlock.y, targetBlock.z)));
            }
            if (!eventHandler.isTriggerCoordinate(targetBlock.x, targetBlock.y, targetBlock.z)) {
                return;
            }

            // Pass coordinates to EventHandler which handles both event triggering and end conditions
            eventHandler.handleBlockInteraction(targetBlock.x, targetBlock.y, targetBlock.z, blockTypeId);
        }

        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }

    private static class BreakBlockEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

        public BreakBlockEventSystem(@NonNullDecl Class<BreakBlockEvent> eventType) {
            super(eventType);
        }

        @Override
        public void handle(int index, ArchetypeChunk<EntityStore> archetypeChunk, Store<EntityStore> store,
                CommandBuffer<EntityStore> commandBuffer, BreakBlockEvent event) {
            Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(index);
            Player player = store.getComponent(entityStoreRef, Player.getComponentType());
            if (player == null) {
                return;
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return PlayerRef.getComponentType();
        }
    }
}
