package dev.murad.liteloadlib.examples;

import dev.murad.liteloadlib.api.EnrollableEntity;
import dev.murad.liteloadlib.api.EnrollmentHandler;
import dev.murad.liteloadlib.api.LLLClientAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class MyExampleEntity extends Entity implements EnrollableEntity {
    private final EnrollmentHandler enrollmentHandler;

    public MyExampleEntity(EntityType<?> type, Level level){
        super(type, level);
        enrollmentHandler = new EnrollmentHandler(this);
    }

    // if you want to enroll an entity based on a server event - ex. clicked with a specific item
    public void serverSideEnrollExample(UUID player){
        enrollmentHandler.enroll(player);
    }
    // if you want to enroll an entity based on a client event - ex. GUI button
    public void clientSideEnrollExample(){
        if(this.level.isClientSide()) {
            LLLClientAPI.sendEntityEnrollmentPacket(this);
        }
    }

    @Override
    public EnrollmentHandler getEnrollmentHandler() {
        return enrollmentHandler;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        enrollmentHandler.load(tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        enrollmentHandler.save(tag);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return null;
    }
}
