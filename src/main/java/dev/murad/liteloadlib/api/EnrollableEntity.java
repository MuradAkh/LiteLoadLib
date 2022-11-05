package dev.murad.liteloadlib.api;

import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.UUID;


/**
 *  Implement for entities that can be registered to a player.
 */
public interface EnrollableEntity {


    /**
     * @return entity's enrollment handler
     */
    EnrollmentHandler getEnrollmentHandler();

    /**
     *  Call the EnrollmentHandler to set the owner
     * @param uuid UUID of owner-player
     */
    default void enroll(UUID uuid) {
        getEnrollmentHandler().enroll(uuid);
    }

    /**
     * @return other entities that should be loaded with this entity, excluding the passengers and part entities. Use for train cars etc.
     */
    default List<Entity> getExtraEntities() {
        return List.of();
    }
}
