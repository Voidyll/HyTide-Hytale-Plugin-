package me.voidyll.utils;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.UUID;

/**
 * Utility class for resolving the correct World instance.
 *
 * In Hytale, "flat" world saves create two world instances at runtime:
 *   - "default"                     : an empty, internal default world
 *   - "instance-Default_Flat-UUID": the actual game world the player is in
 *
 * Universe.get().getDefaultWorld() always returns the "default" world, which is
 * NOT the game world in flat saves.  Using it to call world.execute() then causes
 * an IllegalStateException because entity stores belong to a different world thread.
 *
 * Use getPlayerWorld() for commands that have a CommandContext, and
 * getGameWorld() for background systems.
 */
public class WorldUtil {

    /**
     * Returns the world that the given player is currently in.
     * Falls back to the Hytale default world if the player or their world cannot be found.
     *
     * @param playerUuid UUID of the player (from context.sender().getUuid())
     */
    public static World getPlayerWorld(UUID playerUuid) {
        PlayerRef pr = Universe.get().getPlayer(playerUuid);
        if (pr != null) {
            World w = Universe.get().getWorld(pr.getWorldUuid());
            if (w != null) {
                return w;
            }
        }
        return Universe.get().getDefaultWorld();
    }

    /**
     * Returns the first world that currently has at least one player in it.
     * Falls back to the Hytale default world if no world has players.
     *
     * Suitable for background systems that have no player context.
     */
    public static World getGameWorld() {
        for (World w : Universe.get().getWorlds().values()) {
            if (w.getPlayerCount() > 0) {
                return w;
            }
        }
        return Universe.get().getDefaultWorld();
    }
}
