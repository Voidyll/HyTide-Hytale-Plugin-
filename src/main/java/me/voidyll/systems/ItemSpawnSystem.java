package me.voidyll.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.voidyll.data.ItemSpawnDataManager;
import me.voidyll.data.ItemSpawnMarkerData;
import me.voidyll.utils.WorldUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * System for managing random item spawns at markers.
 */
public class ItemSpawnSystem {

    private static final String[] ITEM_POOL = {
        "HyTide_Potion_Health_Large",
        "HyTide_Weapon_Bomb",
        "HyTide_Weapon_Arrow_Crude"
    };

    private static final int[] ITEM_QUANTITIES = {
        1,  // Potion_Health_Large
        1,  // Weapon_Bomb
        20, // Weapon_Arrow_Crude
        1   // Potion_Stamina_Greater
    };

    private static final double SPAWN_CHANCE = 1.0 / 3.0; // 33% chance

    private final ItemSpawnDataManager dataManager;
    private final Random random = new Random();

    public ItemSpawnSystem(ItemSpawnDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Remove all dropped items from all worlds.
     * Must be called from the world thread.
     */
    public void removeAllDroppedItems() {
        World world = WorldUtil.getGameWorld();
        if (world == null) {
            return;
        }

        world.execute(() -> {
            EntityStore entityStore = world.getEntityStore();
            if (entityStore == null) {
                return;
            }

            Store<EntityStore> worldStore = entityStore.getStore();
            List<Ref<EntityStore>> itemsToRemove = new ArrayList<>();

            // Query all entities with ItemComponent
            worldStore.forEachChunk(
                ItemComponent.getComponentType(),
                (ArchetypeChunk<EntityStore> chunk, CommandBuffer<EntityStore> buffer) -> {
                    for (int i = 0; i < chunk.size(); i++) {
                        Ref<EntityStore> itemRef = chunk.getReferenceTo(i);
                        itemsToRemove.add(itemRef);
                    }
                }
            );

            // Remove all found items
            worldStore.forEachChunk(
                ItemComponent.getComponentType(),
                (ArchetypeChunk<EntityStore> chunk, CommandBuffer<EntityStore> buffer) -> {
                    for (int i = 0; i < chunk.size(); i++) {
                        Ref<EntityStore> itemRef = chunk.getReferenceTo(i);
                        buffer.tryRemoveEntity(itemRef, RemoveReason.REMOVE);
                    }
                }
            );
        });
    }

    /**
     * Spawn random items at all markers based on spawn rules.
     * Must be called from the world thread.
     */
    public void spawnItemsAtMarkers() {
        List<ItemSpawnMarkerData> markers = dataManager.getMarkers();
        
        for (ItemSpawnMarkerData marker : markers) {
            // Roll for spawn chance (1 in 3)
            if (random.nextDouble() > SPAWN_CHANCE) {
                continue; // This marker doesn't spawn an item this time
            }

            // Randomly select an item from the pool
            int itemIndex = random.nextInt(ITEM_POOL.length);
            String itemId = ITEM_POOL[itemIndex];
            int quantity = ITEM_QUANTITIES[itemIndex];

            // Get the world and spawn the item
            World world = Universe.get().getWorld(marker.getWorldName());
            if (world == null) {
                world = WorldUtil.getGameWorld();
            }

            if (world == null) {
                continue;
            }

            final World finalWorld = world;
            final String finalItemId = itemId;
            final int finalQuantity = quantity;
            final Vector3d position = new Vector3d(marker.getX(), marker.getY(), marker.getZ());

            // Execute on world thread
            finalWorld.execute(() -> {
                spawnItemAtPosition(finalWorld, finalItemId, finalQuantity, position);
            });
        }
    }

    /**
     * Spawn a single item at a specific position.
     * Must be called from the world thread.
     */
    private void spawnItemAtPosition(World world, String itemId, int quantity, Vector3d position) {
        EntityStore entityStore = world.getEntityStore();
        if (entityStore == null) {
            return;
        }

        Store<EntityStore> worldStore = entityStore.getStore();

        try {
            // Create ItemStack
            ItemStack itemStack = new ItemStack(itemId, quantity);

            // Generate item drop entity
            Holder<EntityStore> itemEntity = ItemComponent.generateItemDrop(
                worldStore,
                itemStack,
                position,
                Vector3f.ZERO,
                0f,
                0.5f,
                0f
            );

            // Add the item entity to the world
            worldStore.addEntity(itemEntity, AddReason.SPAWN);

        } catch (Exception e) {
            System.err.println("Failed to spawn item " + itemId + " at position " + position + ": " + e.getMessage());
        }
    }
}
