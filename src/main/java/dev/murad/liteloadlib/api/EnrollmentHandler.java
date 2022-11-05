package dev.murad.liteloadlib.api;

import com.mojang.authlib.GameProfile;
import dev.murad.liteloadlib.internal.LLLConfig;
import dev.murad.liteloadlib.internal.global.PlayerEntityChunkManager;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * Helper class for enrollable entities. Instantiate one for every entity instance
 */
@RequiredArgsConstructor
public class EnrollmentHandler {
    private static final String UUID_TAG = "EnrollmentHandlerOwner";
    private UUID uuid = null;
    private int enrollMe = -1;
    private final Entity entity;

    /**
     * Call in Entity.tick
     */
    public void tick(){
        if(enrollMe >= 0){
            if(enrollMe == 0 && !PlayerEntityChunkManager.enroll(entity, uuid)) {
              enrollMe = 100;
            } else {
                enrollMe--;
            }
        }
    }

    public boolean hasOwner(){
        return uuid != null;
    }

    public boolean mayMove(){
        if(uuid == null){
            return true;
        } else if (LLLConfig.Server.OFFLINE_LOADING.get()){
            return true;
        } else {
            return PlayerEntityChunkManager.get((ServerLevel) entity.level, uuid).isActive() && enrollMe < 0;
        }
    }

    public void enroll(UUID uuid){
        if(PlayerEntityChunkManager.enrollIfAllowed(entity, uuid)){
            this.uuid = uuid;
        }
    }

    /**
     *  Call when saving entity NBT
     */
    public void save(CompoundTag tag){
        if(uuid != null) {
            tag.putUUID(UUID_TAG, uuid);
        }
    }

    /**
     *  Call when loading entity NBT
     */
    public void load(CompoundTag tag){
        if(tag.contains(UUID_TAG)) {
            uuid = tag.getUUID(UUID_TAG);
            enrollMe = 5;
        }
    }

    public Optional<String> getPlayerName(){
        if(uuid == null)
            return Optional.empty();
        else return ((ServerLevel) entity.level).getServer().getProfileCache().get(uuid).map(GameProfile::getName);
    }
}
