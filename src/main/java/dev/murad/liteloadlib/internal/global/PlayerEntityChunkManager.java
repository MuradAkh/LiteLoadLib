package dev.murad.liteloadlib.internal.global;

import dev.murad.liteloadlib.internal.LLLConfig;
import dev.murad.liteloadlib.api.EntityPosition;
import dev.murad.liteloadlib.internal.network.client.EntityTrackerPacketHandler;
import dev.murad.liteloadlib.internal.network.client.EntityTrackerClientPacket;
import dev.murad.liteloadlib.api.EnrollableEntity;
import dev.murad.liteloadlib.api.ViewerItem;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.stream.Collectors;


public class PlayerEntityChunkManager extends SavedData {
    private final static TicketType<UUID> TRAVEL_TICKET = TicketType.create("liteloadlib:travelticket", UUID::compareTo);
    private final static TicketType<UUID> LOAD_TICKET = TicketType.create("liteloadlib:loadticket", UUID::compareTo, 500);
    private final Set<Entity> enrolled = new HashSet<>();
    private final Set<ChunkPos> tickets = new HashSet<>();
    private final Set<ChunkPos> toLoad = new HashSet<>();
    private final int loadLevel = LLLConfig.Server.CHUNK_LOADING_LEVEL.get();
    private boolean changed = false;
    private boolean active = false;
    @Getter
    private int numVehicles = 0;
    @Getter
    private final UUID uuid;
    @Getter
    private final ServerLevel level;

    public static PlayerEntityChunkManager get(ServerLevel level, UUID uuid){
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent((tag) -> new PlayerEntityChunkManager(tag, level, uuid), () -> new PlayerEntityChunkManager(level, uuid), "liteloadlib:chunkmanager-" + uuid.toString());
    }

    public static Optional<PlayerEntityChunkManager> getSaved(ServerLevel level, UUID uuid){
        DimensionDataStorage storage = level.getDataStorage();
        return Optional.ofNullable(storage.get((tag) -> new PlayerEntityChunkManager(tag, level, uuid),"liteloadlib:chunkmanager-" + uuid.toString()));
    }



    public static boolean enroll(Entity entity, UUID uuid){
        if(!entity.level.isClientSide) {
            var manager = PlayerEntityChunkManager.get((ServerLevel) entity.level, uuid);
            if(!manager.active){
                return false;
            }
            manager.enrolled.add(entity);
            manager.changed = true;
            return true;
        }
        return false;
    }

    public static boolean enrollIfAllowed(Entity entity, UUID uuid){
        if(!entity.level.isClientSide) {
            var manager = PlayerEntityChunkManager.get((ServerLevel) entity.level, uuid);
            Player player = manager.level.getPlayerByUUID(uuid);
            if(player == null){
                return false;
            }
            int max = LLLConfig.Server.MAX_REGISTRERED_VEHICLES_PER_PLAYER.get();
            int registered = EntityChunkManagerManager.get(manager.level.getServer()).countVehicles(uuid) + 1;
            if(registered > max){
                player.sendSystemMessage(Component.translatable("global.liteloadlib.registration.register_success", max));
                return false;
            } else {
                player.sendSystemMessage(Component.translatable("global.liteloadlib.registration.register_fail", registered, max));
                manager.enrolled.add(entity);
                manager.changed = true;
                return true;
            }
        }
        return false;
    }

    public void deactivate(){
        updateToLoad();
        numVehicles = enrolled.size();
        enrolled.clear();
        tickets.forEach(chunkPos -> level.getChunkSource().removeRegionTicket(TRAVEL_TICKET, chunkPos, loadLevel, uuid));
        tickets.clear();
        active = false;
    }

    public void activate(){
        active = true;
        level.getServer().execute(() -> {
            toLoad.forEach(chunkPos -> level.getChunkSource().addRegionTicket(LOAD_TICKET, chunkPos, 2, uuid));
        });
    }

    private List<Entity> getAllSubjectEntities(Entity entity){
        List<Entity> subjects = new ArrayList<>();
        subjects.add(entity);
        if(entity instanceof EnrollableEntity l){ // need to refactor this somehow to be more generic
           for(var e : l.getExtraEntities()){
               subjects.add(e);
               subjects.addAll(e.getPassengers());
           }
        }

        if(entity.getParts() != null){
            subjects.addAll(List.of(entity.getParts()));
        }
        return subjects;
    }

    private void updateToLoad() {
        toLoad.clear();
        enrolled.forEach(e -> toLoad.addAll(getAllSubjectEntities(e).stream().map(Entity::chunkPosition).collect(Collectors.toSet())));
    }

    public void tick(){
        boolean changed = enrolled.removeIf(e -> !e.isAlive());
        if(!active){
            return;
        }

        enrolled.forEach(entityHead -> getAllSubjectEntities(entityHead)
                .stream()
                .filter(entity -> !((ServerLevel) entity.level).isPositionEntityTicking(entity.blockPosition()))
                .forEach(Entity::tick));

        Player player = level.getPlayerByUUID(uuid);
        if(player instanceof ServerPlayer serverPlayer && serverPlayer.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ViewerItem) {
            EntityTrackerPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), EntityTrackerClientPacket.of(getEntityPositions(), level.dimension().toString()));
        }

        if(this.changed || changed || enrolled.stream()
                .map(e -> !e.chunkPosition().equals(new ChunkPos(new BlockPos(e.xOld, e.yOld, e.zOld))))
                .reduce(Boolean.FALSE, Boolean::logicalOr)){
            this.changed = false;
            level.getServer().execute(this::onChanged);

        }
    }

    public List<EntityPosition> getEntityPositions(){
        return enrolled.stream().map(entity ->
                new EntityPosition(entity.getType().toString(), entity.getId(), entity.position(), new Vec3(entity.xOld, entity.yOld, entity.zOld)))
                .collect(Collectors.toList());
    }

    private void onChanged() {
        Set<ChunkPos> required = new HashSet<>();
        numVehicles = enrolled.size();
        if(LLLConfig.Server.DISABLE_CHUNK_MANAGEMENT.get()){
            removeUnneededTickets(required);
            return;
        }
        enrolled.stream().map(this::computeRequiredTickets).forEach(required::addAll);
        removeUnneededTickets(required);
        addNeededTickets(required);
        updateToLoad();
        setDirty();
    }

    private Set<ChunkPos> computeRequiredTickets(Entity entity) {
        var set = new HashSet<ChunkPos>();
        getAllSubjectEntities(entity).stream()
                .map(Entity::chunkPosition)
                .map(pos -> ChunkPos.rangeClosed(pos, 1))
                .forEach(pos -> set.addAll(pos.collect(Collectors.toList())));

        return set;
    }

    private void removeUnneededTickets(Set<ChunkPos> required){
        Set.copyOf(tickets)
                .stream()
                .filter(pos -> !required.contains(pos))
                .forEach(chunkPos -> {
                    level.getChunkSource().removeRegionTicket(TRAVEL_TICKET, chunkPos, loadLevel, uuid);
                    tickets.remove(chunkPos);
                });
    }

    private void addNeededTickets(Set<ChunkPos> required){
        required
            .stream()
            .filter(pos -> !tickets.contains(pos))
                .collect(Collectors.toSet()) // avoid mutation on the go
                .forEach(chunkPos -> {
                    level.getChunkSource().addRegionTicket(TRAVEL_TICKET, chunkPos, loadLevel, uuid);
                    tickets.add(chunkPos);
                });
    }


    PlayerEntityChunkManager(ServerLevel level, UUID uuid){
        this.level = level;
        this.uuid = uuid;
        EntityChunkManagerManager.get(level.getServer()).enroll(this);
        // active when creating a new one
        active = true;
        setDirty();
    }

    PlayerEntityChunkManager(CompoundTag tag, ServerLevel level, UUID uuid){
        this.level = level;
        this.uuid = uuid;
        numVehicles = tag.getInt("numVehicles");
        Arrays.stream(tag.getLongArray("chunksToLoad")).forEach(chunk -> toLoad.add(new ChunkPos(chunk)));
        EntityChunkManagerManager.get(level.getServer()).enroll(this);
        if(LLLConfig.Server.OFFLINE_LOADING.get()){
            activate();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("numVehicles", numVehicles);
        tag.putLongArray("chunksToLoad", toLoad.stream().map(ChunkPos::toLong).collect(Collectors.toList()));
        return tag;
    }

    public boolean isActive() {
        return active;
    }
}
