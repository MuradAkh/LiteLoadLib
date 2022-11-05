package dev.murad.liteloadlib.internal.network;

import dev.murad.liteloadlib.internal.LiteLoadLib;
import dev.murad.liteloadlib.api.EnrollableEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public final class EnrolledEntityPacketHandler {
    public static final ResourceLocation LOCATION = new ResourceLocation(LiteLoadLib.MOD_ID, "registration_channel");
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            LOCATION,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;
    public static void register() {
        // int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer
        INSTANCE.registerMessage(id++, EnrollVehiclePacket.class, EnrollVehiclePacket::encode, EnrollVehiclePacket::new, EnrolledEntityPacketHandler::handleEnrollVehicle);
    }

    public static void handleEnrollVehicle(EnrollVehiclePacket operation, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Optional.of(ctx.get()).map(NetworkEvent.Context::getSender).ifPresent(serverPlayer -> {
                var loco = serverPlayer.level.getEntity(operation.locoId);
                if(loco != null && loco.distanceTo(serverPlayer) < 6 && loco instanceof EnrollableEntity l){
                    l.enroll(serverPlayer.getUUID());
                }
            });

        });

        ctx.get().setPacketHandled(true);
    }
}
