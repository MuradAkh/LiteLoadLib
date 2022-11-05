package dev.murad.liteloadlib.api;

import dev.murad.liteloadlib.internal.network.EnrollVehiclePacket;
import dev.murad.liteloadlib.internal.network.EnrolledEntityPacketHandler;
import dev.murad.liteloadlib.internal.network.client.EntityTrackerPacketHandler;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class LLLClientAPI {

    /**
     * @return Tracked entities synced to client when holding a ViewerItem
     */
    public static List<EntityPosition> getEntitiesToRender(){
        return EntityTrackerPacketHandler.toRender;
    }


    /**
     * @return dimension of last tracked entities
     */
    public static String getTrackedEntitiesDimension(){
        return EntityTrackerPacketHandler.toRenderDimension;
    }

    /**
     * Send a packet to server instructing to enroll an entity to the client's player
     * Use this in your entity's UI
     * @param entity Entity to enroll
     */
    public static <T extends Entity & EnrollableEntity> void sendEntityEnrollmentPacket(T entity){
        EnrolledEntityPacketHandler.INSTANCE.sendToServer(new EnrollVehiclePacket(entity.getId()));
    }
}
